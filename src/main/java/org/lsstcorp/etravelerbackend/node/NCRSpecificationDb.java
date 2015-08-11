/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

import java.sql.SQLException;
import java.util.ArrayList;
import org.lsstcorp.etravelerbackend.db.DbConnection;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author jrb
 */
public class NCRSpecificationDb {
  private NCRSpecification m_spec=null;
  private String m_condition=null;
  private String m_travelerRootId=null;
  private String m_exitPathString=null; /* comma-separated ids */
  private String m_returnPathString=null; /* comma-separated ids */
  private String m_exitProcessId=null;
  private String m_returnProcessId=null;
  private String m_ncrProcessId=null;
  private TravelerToDbVisitor m_vis=null;
  private boolean m_verified=false;
  private String m_status=null;
  
  private static String[] s_insertExceptionCols={"conditionString", 
    "exitProcessPath", "returnProcessPath", "exitProcessId", "returnProcessId",
    "rootProcessId", "NCRProcessId", "createdBy"};
  private static String[] s_queryExceptionCols={"conditionString", "exitProcessPath",
    "returnProcessPath", "exitProcessId", "returnProcessId",
    "rootProcessId", "NCRProcessId", "status"};

  
  /**
   *  Constructor for new object defined from web interface
   * @param spec
   * @param vis 
   */
  public NCRSpecificationDb(NCRSpecification spec, TravelerToDbVisitor vis) {
    m_spec=spec;
    m_vis = vis;
    m_ncrProcessId = spec.getNCRId();
    m_condition = spec.getCondition();
  }
  
  /**
   *  Constructor for object read in from db
   * @param conn   Connection for db to read from
   * @param id     ExceptionType.id
   */
  public NCRSpecificationDb(DbConnection conn, String id) throws SQLException,
      EtravelerException {
    String where = " where id='" + id + "'";
    PreparedStatement q = conn.prepareQuery("ExceptionType", s_queryExceptionCols, where);
    ResultSet rs = q.executeQuery();
    if (!rs.relative(1)) {
      throw new EtravelerException("ExceptionType row with id " + id + " not found");
    }
    
    m_condition = rs.getString("conditionString");
    m_exitPathString = rs.getString("exitProcessPath");
    m_returnPathString = rs.getString("returnProcessPath");
    m_exitProcessId = rs.getString("exitProcessId");
    m_returnProcessId = rs.getString("returnProcessId");
    m_ncrProcessId = rs.getString("NCRProcessId");
    m_travelerRootId = rs.getString("rootProcessId");
    m_status = rs.getString("status");
  }
  
  public void verify(DbConnection conn) throws EtravelerException {
    m_travelerRootId = m_spec.getRoot().getProcessId();
 
    m_exitPathString = formPathString(m_spec.getRoot(), m_spec.getExit(), ".");
    if (m_exitPathString == null) {
      throw new EtravelerException("NCRSpecificationDb: Cannot verify exit process");
    }
    m_returnPathString = formPathString(m_spec.getRoot(), m_spec.getReturn(), ".");
    if (m_returnPathString == null) {
      throw new EtravelerException("NCRSpecificationDb: Cannot verify return process");
    }
    if (!okReturnPath(m_exitPathString, m_returnPathString)) {
      throw new EtravelerException("Bad return process selection");
    }
    /*
     * Return path has to satisfy some constraints w.r.t. exit path.. Return must be
     *  equal to exit OR direct ancestor OR sibling OR sibling of direct ancestor
     *  Ultimately, this means path for return can be no longer than path for exit,
     *  and all but last component must match.
    */
    // Sanity check that these processes really are in the db.
    try {
      conn.setAutoCommit(false);
      m_travelerRootId = checkId(m_spec.getRoot(), conn);
      m_exitProcessId = checkId(m_spec.getExit(), conn);
      m_returnProcessId = checkId(m_spec.getReturn(), conn);
      conn.getConnection().commit();
      conn.setAutoCommit(true);
    } catch (SQLException ex)  {
      throw new EtravelerException("SQL error while verifying NCR: " + ex.getMessage());
    }
    m_verified = ((m_travelerRootId !=null) && (m_exitProcessId != null)
        && (m_returnProcessId != null) );
  } 
  public void writeToDb(DbConnection conn) throws EtravelerException {
    if (!m_verified) throw new EtravelerException("Bad ncr specification");
    String[] vals = new String[s_insertExceptionCols.length];
    vals[0] = m_spec.getCondition();
    vals[1] = m_exitPathString;
    vals[2] = m_returnPathString;
    vals[3] = m_exitProcessId;
    vals[4] = m_returnProcessId;
    vals[5] = m_travelerRootId;
    vals[6] = m_spec.getNCRId();
    vals[7] = m_vis.getUser();
    try {
      conn.setAutoCommit(true);
      conn.doInsert("ExceptionType", s_insertExceptionCols, vals, "", 
          DbConnection.ADD_CREATION_TIMESTAMP);
    } catch (SQLException ex) {
      throw new EtravelerException("Write to ExceptionType failed with exception: "
          + ex.getMessage());
    }
    // All or almost all within try..catch
    // Create new ExceptionType row from info in our private members
    
    // Last action is to commit
    //  In catch do rollback
  }
  private static String formPathString(ProcessNode root, ProcessNode node, String sep) {
    ProcessNode current = node;
    if (node == null)  return null;
 
    ArrayList<String> ids = new ArrayList<String>();
    String result = "";
    while (current != null) {
      if (current == root)   break;       /* we're done */
      ids.add(current.getParentEdge().getId());
      current = current.getParent();
      if (current == null) return null;    /* failure */
    }
    int ix = ids.size() - 1;
    result += ids.get(ix);
    while (ix > 0) {
      ix--;
      result += sep + ids.get(ix);
    }
    return result;
  }
  private static boolean okReturnPath(String exitPath, String returnPath)  {
    String[] exitComps = exitPath.split(".");
    String[] returnComps = returnPath.split(".");
    if (returnComps.length > exitComps.length) return false;
    if (returnComps.length < 2) return true;
    for (int i=0; i < returnComps.length - 1; i++ ) {
      if (!returnComps[i].equals(exitComps[i])) return false;
    }
    return true;
  }
  private static String checkId(ProcessNode proc, DbConnection conn) {
    String where = " where name='" + proc.getName() +
        "' and version ='" + proc.getVersion() +"'";
    String id = conn.fetchColumn("Process", "id", where);
    if (id == null) return null;
    if (id.equals(proc.getProcessId())) return id;
    return null;
  }
}
