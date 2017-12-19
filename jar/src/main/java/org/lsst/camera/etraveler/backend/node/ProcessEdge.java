/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;

/**
 * Represents a row in the ProcessEdge db table
 * @author jrb
 */
import org.lsst.camera.etraveler.backend.db.DbConnection;
public class ProcessEdge {
  public ProcessEdge(ProcessNode parent, ProcessNode child, int step,
                     String condition, String hardwareCondition) {
    m_parent = parent;
    m_child = child;
    m_step = step;
    m_condition = condition;
    m_hardwareCondition = hardwareCondition;
  }
  // may need a separate constructor for CloneNode, if there is such a thing
  // Or just have a way in ProcessNode to keep track if it's a clone
  private ProcessNode m_parent;
  private ProcessNode m_child; // maybe unnecessary
  private int     m_step;
  private String  m_condition;
  private String  m_hardwareCondition;
  private String m_edgeId;
  public void setCondition(String cond) {
    m_condition = cond;
  }
  public void setHardwareCondition(String cond) {
    m_hardwareCondition = cond;
  }
  public String getCondition() {return m_condition;}
  public String getHardwareCondition() {return m_hardwareCondition;}
  public void setId(String id) {
    m_edgeId = id;
  }
  public String getId() {return m_edgeId;}
  public int writeDb(DbConnection conn, String user, String childId) {
  return 0; // for now
    
  }
  
    
}
