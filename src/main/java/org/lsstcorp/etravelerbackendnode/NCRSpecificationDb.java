/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import java.sql.SQLException;
import java.util.ArrayList;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.lsstcorp.etravelerbackendexceptions.EtravelerException;

/**
 *
 * @author jrb
 */
public class NCRSpecificationDb {
  private NCRSpecification m_spec=null;
  private String m_travelerRootId=null;
  private String m_exitPathString=null; /* comma-separated ids */
  private String m_returnPathString=null; /* comma-separated ids */
  private TravelerToDbVisitor m_vis=null;
  private boolean m_verified=false;
  
  private static String[] s_insertExceptionCols={"conditionString", 
    "exitProcessPath", "returnProcessPath", "exitProcessId", "returnProcessId",
    "rootProcessId", "NCRProcessId", "createdBy"};
  
  
  public NCRSpecificationDb(NCRSpecification spec, TravelerToDbVisitor vis) {
    m_spec=spec;
    m_vis = vis;
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
    // Sanity check that these processes really are in the db.
    try {
      conn.setAutoCommit(false);
      boolean ok = checkId(m_spec.getRoot(), conn);
      if (ok) ok = checkId(m_spec.getExit(), conn);
      if (ok) ok = checkId(m_spec.getReturn(), conn);
      conn.getConnection().commit();
    } catch (SQLException ex)  {
      throw new EtravelerException("SQL error while verifying NCR: " + ex.getMessage());
    }
    m_verified = true;
  } 
  public void writeToDb(DbConnection conn) throws EtravelerException {
    String[] vals = new String[s_insertExceptionCols.length];
    vals[0] = m_spec.getCondition();
    vals[1] = m_exitPathString;
    vals[2] = m_returnPathString;
    vals[3] = m_spec.getExit().getProcessId();
    vals[4] = m_spec.getReturn().getProcessId();
    vals[5] = m_spec.getRoot().getProcessId();
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
  private static boolean checkId(ProcessNode proc, DbConnection conn) {
    String where = " where name='" + proc.getName() +
        "' and version ='" + proc.getVersion() +"'";
    String id = conn.fetchColumn("Process", "id", where);
    if (id == null) return false;
    return id.equals(proc.getProcessId());
  }
}
