/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author jrb
 */
public class PrerequisiteDb implements Prerequisite.Importer, Prerequisite.ExportTarget {
  private static PreparedStatement s_prereqQuery;
  private static PreparedStatement s_processQuery;
  private static String[] 
    s_prereqCols = {"name", "description", "prerequisiteTypeId", "processId", 
                    "prereqProcessId", "hardwareTypeId", "quantity"};
  private static String[] s_processCols = {"version", "userVersionString"};
  // private static ConcurrentHashMap<String, String> s_prereqIdMap;
 
  // Use this constructor when building from Prerequisite instance
  // Most of the work of initialization is done through ExportTarget interface
  public PrerequisiteDb(DbConnection connect) {
    m_connect=connect;
  }

  // Use this constructor when we have to make up a prequisite to go with
  // hardware relationship type.  Fill all the fields writeDb requires.
  public PrerequisiteDb(DbConnection connect, String name, String prereqType,
                        String prereqTypeId, String prereqProcessId,
                        String hardwareTypeId, int quantity) {
    m_connect = connect;
    m_name = name;
    m_type = prereqType;
    m_typeId = prereqTypeId;
    m_hardwareTypeId = hardwareTypeId;
    m_quantity = quantity;
  }
  /**
   * 
   * @return true iff required initialization has been done and fields have
   * values consistent with db 
   */
  public boolean verify(ConcurrentHashMap<String, String> pType,
                        ConcurrentHashMap<String, String> hNameType)
    throws EtravelerException {
    //     check that prereq type is ok; cache id
    if (!pType.containsKey(m_type) ) {
      throw new EtravelerException("No such prerequisite type " + m_type);
    }
    m_typeId = pType.get(m_type);
    
    // if it's component, check that name field is a valid name
    if ((m_type.equals("COMPONENT")) || (m_type.equals("TEST_EQUIPMENT")) ) {
      if (hNameType.containsKey(m_name)) {
        m_hardwareTypeId = hNameType.get(m_name);
      } else {
        if (m_type.equals("COMPONENT") ) {
          throw new EtravelerException("No such component type " + m_name);
        }
      }
    }

    m_verified = true; 
    return m_verified;
  }
  
  private static String[] s_insertPrerequisiteCols=
  {"name", "prerequisiteTypeId", "processId", "prereqProcessId",
   "hardwareTypeId", "quantity", "createdBy"   };

  void writeToDb(DbConnection connect, ProcessNodeDb parent, String user) 
    throws    SQLException, EtravelerException {
    String[] vals = new String[s_insertPrerequisiteCols.length];

    // code to set vals appropriately
    vals[0] = m_name;
    vals[1] = m_typeId;
    vals[2] = parent.provideId();
    // if type is PROCESS_STEP prereqProcessId can only reliably be 
    // determined at this point
    if (m_type.equals("PROCESS_STEP") ) {
      String where = " where name='"+m_name+"' and ";
      if (m_userVersionString != null) {
        where += "userVersionString='" + m_userVersionString + "'";
      } else {
        where += "version='" + m_version + "'";
      }
      m_prereqProcessId =  m_connect.fetchColumn("Process", "id", where);
      if (m_prereqProcessId == null) {
        System.out.println("Failed to fetch process id for prerequisite " 
            + m_name);
        throw new EtravelerException("Failed writing process step prereq");
      }
    }
    vals[3] = m_prereqProcessId;
    vals[4] = m_hardwareTypeId;
    vals[5] = String.valueOf(m_quantity);
    vals[6] = user;                      // for the time being

    // Do the write to db
    try {
      m_id = m_connect.doInsert("PrerequisitePattern", 
                                s_insertPrerequisiteCols, vals, "", 
                                DbConnection.ADD_CREATION_TIMESTAMP);
    } catch (SQLException ex)  {
      System.out.println("Failed to create entry for prerequisite named " 
                         + m_name + "with exception");
      System.out.println(ex.getMessage());
      throw ex;
    }
  }

  // Use this constructor when reading from db
  public PrerequisiteDb(DbConnection connect, String id, 
                        ConcurrentHashMap<String, String> prereqTypeMap,
                        ConcurrentHashMap<String, String> hardwareNameMap)
    throws SQLException, EtravelerException {
    m_id = id;

    if (s_prereqQuery == null) {
      String where = " WHERE id=?";
      s_prereqQuery = connect.prepareQuery("PrerequisitePattern", s_prereqCols, where);
      s_processQuery = connect.prepareQuery("Process", s_processCols, where);
 
      if ((s_prereqQuery == null) || (s_processQuery == null) ) {
        throw new SQLException("DbConnection.prepareQuery failure");
      }
    }

    ResultSet rs;

    try {
      s_prereqQuery.setString(1, m_id);
      rs = s_prereqQuery.executeQuery();
      rs.next();
      int ix=0;
      m_name = rs.getString(++ix);
      m_description = rs.getString(++ix);
      String typeId = rs.getString(++ix);

      if (!prereqTypeMap.containsKey(typeId))  {
        throw new UnknownDbId(typeId, "PrerequisiteType");
      }
      m_type = prereqTypeMap.get(typeId);
      m_parentProcessId = rs.getString(++ix);   // do we need this?
      m_prereqProcessId = rs.getString(++ix);
      m_hardwareTypeId = rs.getString(++ix);
      m_quantity = rs.getInt(++ix);
      rs.close();
    }  catch (SQLException ex)   {
      throw ex;
    }
    if (m_type.equals("PROCESS_STEP") ) {  // get version info
      s_processQuery.setString(1, m_prereqProcessId);
      rs = s_processQuery.executeQuery();
      rs.next();
      m_version = rs.getString(1);
      m_userVersionString = rs.getString(2);
    } else if ((m_type.equals("COMPONENT")) || 
               (m_type.equals("TEST_EQUIPMENT")) )  {

      //  look up hardwareTypeId if non-null, non-empty
    }
  }
  // Prerequisite.ExportTarget interface
  public void acceptPrerequisiteType(String prerequisiteType) {
    m_type = prerequisiteType;
  }
  public void acceptPrereqProcessVersion(String version) {m_version=version;}
  public void acceptPrereqProcessUserVersionString(String userVersionString) {
    m_userVersionString = userVersionString;
  }
  public void acceptPrereqName(String name) { m_name = name; }
  public void acceptPrereqId(String prereqId) { 
    m_id = prereqId; // normally will be null
  } 
  public void acceptPrereqParent(ProcessNode process) {
    // Don't know that we need to save this
  }
  public void acceptPrereqQuantity(int quantity) {m_quantity = quantity; }
  public void acceptPrereqDescription(String description) {
    m_description = description;
  }
  
  public String provideName() {return m_name;}
  public String provideType() {return m_type;}
  public String provideDescription() {
    return m_description;
  }
  public int provideQuantity() {return m_quantity;}
  public String provideVersion() {return m_version;}
  public String provideUserVersionString() {return m_userVersionString;}
  
  public String getHardwareTypeId() { return m_hardwareTypeId; }
  private String m_name=null;  
  
  // For type PROCESS_STEP m_nameId is id of process
  // For type COMPONENT (and also TEST_EQUIPMENT if it's tracked equipment)
  // m_nameId is id of hardware type.
  // following is used used for PROCESS_STEP type
  private String m_prereqProcessId=null;  
  // following is used for COMPONENT and sometimes TEST_EQUIPMENT
  private String m_hardwareTypeId=null;
  private String m_type=null;
  private String m_typeId=null;
  private String m_description=null;
  private int m_quantity=1;

  // Used only if type is PROCESS_STEP
  private String m_version=null;
  private String m_userVersionString=null;

  private String m_id=null;      // Id in Prereq
  private String m_parentProcessId=null;   // do we need this?
  
  /**
   * Set to true if 
   *    constructed from db      OR
   *    constructed from Prerequisite instance and has been verified against db
   */
  boolean m_verified = false;
  
  // For use as export target
  private DbConnection m_connect = null;
  
 
}
