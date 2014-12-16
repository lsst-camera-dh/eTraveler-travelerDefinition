/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import java.sql.SQLException;
import org.lsstcorp.etravelerbackend.db.DbConnection;

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
        m_processNodeDb.registerTraveler();
        if (m_useTransactions) m_connect.commit();
      } catch (Exception ex) {
        if (m_useTransactions) {
          try {
            m_connect.rollback();
          }  catch (SQLException rollEx) {
            
          }
        }
        throw new EtravelerException(ex.getMessage());
      }
    }
    
  }
  // leave these empty.  Easier to do everything from ProcessNode visit
  public void visit(PrescribedResult result, String activity) {
    
  }
  public void visit(Prerequisite prerequisite, String activity) {
    
  }
  
  public void visit(NCRSpecification ncrSpec, String activity) throws EtravelerException {
    if (activity.equals("new")) {
      m_specDb = new NCRSpecificationDb(ncrSpec, this);
    }
    else if (activity.equals("verify")) {
      if (m_specDb==null) throw new EtravelerException("Null NCR specification");
      m_specDb.verify(m_connect);
      
    } else if (activity.equals("write")) {
      m_specDb.writeToDb(m_connect);
    }
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
  private NCRSpecificationDb m_specDb = null;
}
