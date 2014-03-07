/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import java.sql.SQLException;
import org.lsstcorp.etravelerbackenddb.DbConnection;

/**
 *
 * @author jrb
 */
public class TravelerToDbVisitor implements TravelerVisitor  {
  public TravelerToDbVisitor(DbConnection connection) {
    m_connect = connection;
  }
  public void setUser(String user) {
    m_user = user;
  }
  public String getUser() { return m_user; }
  public void visit(ProcessNode process, String activity)  
      throws EtravelerException  {
    if (activity.equals("new") )  {
      m_process = process;
      m_processNodeDb = new ProcessNodeDb(m_connect, this, null, 0);
      process.exportTo(m_processNodeDb);     
    } else {
      if (m_processNodeDb == null) {
        throw new EtravelerException("TravelerToDbVisitor: Missing 'new' step ");
      }
    }
    if (activity.equals("verify"))  {
      try {
        m_connect.setAutoCommit(true);
      } catch (SQLException ex) {
        throw new EtravelerException("SQL failure setting AutoCommit");
      }
      m_processNodeDb.verify(m_connect);
    }  else if (activity.equals("write"))  {
      try {
        if (m_useTransactions) m_connect.setAutoCommit(false);
        m_processNodeDb.writeToDb(m_connect, null);
        if (m_useTransactions) m_connect.commit();
      } catch (SQLException ex) {
        if (m_useTransactions) {
          try {
            m_connect.rollback();
          }  catch (SQLException rollEx) {
            
          }
        }
        throw new EtravelerException("SQL failure");
      }
    }
    
  }
  // leave these empty.  Easier to do everything from ProcessNode visit
  public void visit(PrescribedResult result, String activity) {
    
  }
  public void visit(Prerequisite prerequisite, String activity) {
    
  }
  public ProcessNode getProcess() {return m_process;}
  public DbConnection getConnection() {return m_connect;}
  public ProcessNodeDb getProcessNodeDb() {return m_processNodeDb; }
  public void setUseTransactions(boolean setting) {
    m_useTransactions = setting;
  }
  private ProcessNode m_process = null;
  private ProcessNodeDb m_processNodeDb=null;
  private DbConnection m_connect = null;
  private boolean m_useTransactions = true;
  private String m_user = null;
}
