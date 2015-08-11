package org.lsstcorp.etravelerbackend.node;
import org.lsstcorp.etravelerbackend.exceptions.UnknownDbId;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import org.lsstcorp.etravelerbackend.db.DbConnection;
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

  private static String[] s_tagTableCols = {"MRT.name", "MRA.name"};

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
   
    rs.close();
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
    m_verified = true;
  }

  private static String[] s_insertTagCols=
  {"processId", "multiRelationshipTypeId", "multiRelationshipActionId", 
    "createdBy"};

  void writeToDb(DbConnection connect, ProcessNodeDb parent, String user) 
    throws    SQLException {
    String[] vals = new String[s_insertTagCols.length];
    vals[0] = parent.provideId();
    vals[1] = m_relationshipId;
    vals[2] = m_actionId;
    vals[3] = user;
 
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
  
  // ExportTarget interface
  public void acceptRelationshipName(String name) { m_name = name;}
  public void acceptRelationshipAction(String action) { m_action = action;}
  public void acceptRelationshipParent(ProcessNode parent) {m_parent=parent;}
  public void acceptRelationshipTaskId(String id) {m_tagId = id;}
  

  private String m_tagId=null; /* this id is for entry in ProcessRelationshipTag */
  private String m_name="";
  private String m_action="";
  private ProcessNode m_parent=null;
  private String m_relationshipId=null;
  private String m_actionId=null;
 
  private DbConnection m_connect=null;
  private boolean m_verified=false;
 }
