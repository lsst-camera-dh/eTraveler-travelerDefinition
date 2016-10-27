package org.lsst.camera.etraveler.backend.node;
import org.lsst.camera.etraveler.backend.exceptions.UnknownDbId;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerWarning;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author jrb
 */
public class RelationshipTaskDb implements RelationshipTask.Importer,
    RelationshipTask.ExportTarget {
  private static PreparedStatement s_tagTableQuery;
  public static void reset() {
    s_tagTableQuery = null;
  }

  private static String[] s_tagTableCols = {"MRT.name", "MRA.name", "PRT.slotForm",
                                            "PRT.multiRelationshipSlotTypeId"};

  /**
   * use this constructor when building for export from RelationshipTask 
   */
  public RelationshipTaskDb(DbConnection connect) {
    m_connect = connect;
  }
  
  /**
   *    Use this constructor when building from database representation
   * @param connect
   * @param id      This is id for a row in ProcessRelationshipTag
   * @throws SQLException
   * @throws EtravelerException 
   */
  public RelationshipTaskDb(DbConnection connect, String id) 
      throws SQLException, EtravelerException {
    m_tagId = id;
    if (s_tagTableQuery == null) {
      String where = " WHERE PRT.id=?";
      String tableSpec = "ProcessRelationshipTag PRT join MultiRelationshipAction MRA ";
      tableSpec += "on PRT.multiRelationshipActionId=MRA.id join MultiRelationshipType ";
      tableSpec += " MRT on PRT.multiRelationshipTypeId=MRT.id";
      s_tagTableQuery = connect.prepareQuery(tableSpec, s_tagTableCols, where);
 
      if (s_tagTableQuery == null) {
        throw new SQLException("DbConnection.prepareQuery failure");
      }
    }   // end of init section
    ResultSet rs;
    try {
      s_tagTableQuery.setString(1, m_tagId);
      rs = s_tagTableQuery.executeQuery();
      rs.next();
    }  catch (SQLException ex)  {
      throw ex;
    }
    int ix = 0;
    m_name = rs.getString(++ix);
    m_action =  rs.getString(++ix);
    m_slotForm = rs.getString(++ix);
    m_slotId = rs.getString(++ix);
    rs.close();
    
    if (m_slotForm.equals("ALL")) {
      m_slotname="ALL";
    } else if (m_slotForm.equals("QUERY")) {
      m_slotname="(?)";
    } else if (m_slotForm.equals("SPECIFIED")) { // look up name
      m_slotname = connect.fetchColumn("MultiRelationshipSlotType", "slotname",
                                         " where id='" + m_slotId + "'");
    } else { // unrecognized
      throw new EtravelerException("No such slot type specification as " + m_slotForm);
    }

  }   // end constructor

  /*
   * Check that relationship type exists and has htype compatible wit our 
   * hardware group.  Also check that action exists
   */
  void verify(ConcurrentHashMap<String, String> rMap,
              ConcurrentHashMap<String, String> aMap, 
              String hgroupId)  throws EtravelerException {
    if (!rMap.containsKey(m_name))  {
        throw new 
          EtravelerException("No such relationship type " + m_name);
    } else {
      m_relationshipId = rMap.get(m_name);
    }
    if (!aMap.containsKey(m_action))  {
        throw new 
          EtravelerException("No such relationship action " + m_action);
    } else {
      m_actionId = aMap.get(m_action);
    }
    String mapId = m_connect.fetchColumn(
        "HardwareTypeGroupMapping HGTM join MultiRelationshipType MRT on " + 
        "HGTM.hardwareTypeId=MRT.hardwareTypeId",
        "HGTM.id", " where hardwareGroupId='" + hgroupId + "' and MRT.id='" +
        m_relationshipId + "'");
    if (mapId == null) {
      throw new EtravelerException("Relationship type " + m_name +
          " is not compatible with this traveler's hardware group");
    }
    if (m_slotname.equals("") || m_slotname.equals("ALL")) {
      m_slotId=null;
      m_slotForm="ALL";
      m_verified = true;
      return;
    }
    /* If there only is one slot, issue warning and switch to "ALL" */
    String nSlot = m_connect.fetchColumn(
        "MultiRelationshipType MRT join MultiRelationshipSlotType MRST on " +
        "MRT.id=MRST.multiRelationshipTypeId", "count(MRST.slotname)",
        "where MRT.id='"
        + m_relationshipId + "'");
    if (Integer.parseInt(nSlot) == 1) {
      // print warning.  Only ALL makes sense
      m_slotForm="ALL";
      m_slotId=null;
      m_verified = true;
      throw new
        EtravelerWarning("Slot form set to ALL for single-slot relationship type");
    }
    if (m_slotname.equals("(?)") ) {
      m_slotId=null;
      m_slotForm="QUERY";
    } else {
      m_slotForm="SPECIFIED";
      m_slotId = m_connect.fetchColumn(
        "MultiRelationshipType MRT join MultiRelationshipSlotType MRST on " +
        "MRT.id=MRST.multiRelationshipTypeId", "MRST.id", "where MRST.slotname='"
        + m_slotname + "'");
      if (m_slotId == null) {
        throw new EtravelerException("Relationship type " + m_name +
                                    " has no slot with name " + m_slotname);
      }
    }
    m_verified = true;
  }

  private static String[] s_insertTagCols=
  {"processId", "multiRelationshipTypeId", "multiRelationshipActionId",
   "multiRelationshipSlotTypeId", "slotForm", "createdBy"};

  void writeToDb(DbConnection connect, ProcessNodeDb parent, String user) 
    throws    SQLException {
    String[] vals = new String[s_insertTagCols.length];
    vals[0] = parent.provideId();
    vals[1] = m_relationshipId;
    vals[2] = m_actionId;
    vals[3] = m_slotId;
    vals[4] = m_slotForm;
    vals[5] = user;
 
    try {
      m_tagId = m_connect.doInsert("ProcessRelationshipTag", s_insertTagCols, vals, "", 
          DbConnection.ADD_CREATION_TIMESTAMP);
    } catch (SQLException ex)  {
      System.out.println("Failed to create entry for hardware relationship " 
                         + m_name + "with exception");
      System.out.println(ex.getMessage());
      throw ex;
    }
  }

  // Importer interface
  public String provideRelationshipName() {return m_name;}
  public String provideRelationshipAction() {return m_action;}
  public String provideRelationshipSlot() {return m_slotname;}
  
  // ExportTarget interface
  public void acceptRelationshipName(String name) { m_name = name;}
  public void acceptRelationshipAction(String action) { m_action = action;}
  public void acceptRelationshipSlot(String slot) {m_slotname = slot;}
  public void acceptRelationshipParent(ProcessNode parent) {m_parent=parent;}
  public void acceptRelationshipTaskId(String id) {m_tagId = id;}
  

  private String m_tagId=null; /* this id is for entry in ProcessRelationshipTag */
  private String m_name="";
  private String m_action="";
  private String m_slotname="";
  private ProcessNode m_parent=null;
  private String m_relationshipId=null;
  private String m_actionId=null;
  private String m_slotId=null;
  private String m_slotForm="ALL";
 
  private DbConnection m_connect=null;
  private boolean m_verified=false;
 }
