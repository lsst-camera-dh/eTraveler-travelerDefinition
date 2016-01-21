/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.sql.SQLException;
import org.lsst.camera.etraveler.backend.db.DbConnection;

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
  public void visit(ProcessNode process, String activity, Object cxt)  
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
      m_processNodeDb.verify(m_connect, m_subsystem);
    }  else if (activity.equals("write"))  {
      /* Perhaps pass reason string in cxt argument.  Then
       * pass as argument to registerTraveler
       */
      try {
        if (m_useTransactions) m_connect.setAutoCommit(false);
        m_processNodeDb.writeToDb(m_connect, null);
        m_processNodeDb.registerTraveler(m_owner, m_reason);
        if (m_useTransactions) m_connect.commit();
        if (m_processNodeDb.isRootNode() ) {
          m_travelerName = m_processNodeDb.provideName();
          m_travelerVersion = m_processNodeDb.provideVersion();
          m_travelerHardwareGroup = m_processNodeDb.provideHardwareGroup();
        }
      } catch (Exception ex) {
        if (m_useTransactions) {
          try {
            m_connect.rollback();
            
          }  catch (SQLException rollEx) {
            throw new EtravelerException("Rollback failed with exception " + rollEx.getMessage());
          }
        }
        throw new EtravelerException(ex.getMessage());
      } finally {
        try {
          m_connect.setAutoCommit(true);
        } catch (Exception ex) {
          throw new EtravelerException("Set autocommit faile with exception " +
              ex.getMessage());
        }
      }
    }
    
  }
  // leave these empty.  Easier to do everything from ProcessNode visit
  public void visit(PrescribedResult result, String activity, Object cxt) {  }
  public void visit(Prerequisite prerequisite, String activity, Object cxt) {  }
  public void visit(RelationshipTask rel, String activity, Object cxt) {  }
  
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
  public String getTravelerName() {return m_travelerName;}
  public String getTravelerVersion() {return m_travelerVersion;}
  public String getTravelerHardwareGroup() {return m_travelerHardwareGroup;}
  public String getSubsystem() {return m_subsystem;}
  public void setSubsystem(String sub){m_subsystem=sub;}
  public void setUseTransactions(boolean setting) {
    m_useTransactions = setting;
  }
  public void setOwner(String owner) {m_owner = owner;}
  public void setReason(String reason) {m_reason = reason;}
  //public String getOwner() {return m_owner;}
  //public String getReason() {return m_reason;}
  private ProcessNode m_process = null;
  private ProcessNodeDb m_processNodeDb=null;
  private DbConnection m_connect = null;
  private boolean m_useTransactions = true;
  private String m_user = null;
  private NCRSpecificationDb m_specDb = null;
  private String m_owner = null;
  private String m_reason = null;
  private String m_travelerName = null;
  private String m_travelerVersion= null;
  private String m_travelerHardwareGroup=null;
  private String m_subsystem=null;
}
