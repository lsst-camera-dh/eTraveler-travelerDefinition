/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import org.lsstcorp.etravelerbackendutil.Verify;

/**
 * Simple date class with all information describing an NCR (entry in 
 * Exception table). 
 * @author jrb
 */
public class NCRSpecification {
  private ProcessNode m_travelerRoot=null;
  private ProcessNode m_exitProcess=null;
  private ProcessNode m_returnProcess=null;
  private String      m_NCRId = null;     
  private String m_ncrCondition=null;
  private String m_dbType=null;
  private NCRSpecification(ProcessNode travelerRoot, ProcessNode exitProcess, 
      ProcessNode returnProcess, String NCRId, String ncrCondition,
      String dbType) {
    m_travelerRoot = travelerRoot;
    m_exitProcess = exitProcess;
    m_returnProcess = returnProcess;
    m_NCRId = NCRId;
    m_ncrCondition = ncrCondition;
    m_dbType = dbType;
  }
  /**
   *  Validate input.  If it looks ok, make a new object and return
   * @param travelerRoot
   * @param exitProcess
   * @param returnProcess
   * @param NCRId
   * @param ncrCondition
   * @return 
   */
  public static NCRSpecification makeNCRSpecification(ProcessNode travelerRoot,
      ProcessNode exitProcess, ProcessNode returnProcess, String NCRId,
      String ncrCondition, String dbType) {
    if ((travelerRoot==null) || (exitProcess==null) || (returnProcess==null))
    {return null;}
    if ((NCRId==null) || (ncrCondition==null) || (dbType==null) ) {return null;}
    
    if (! Verify.isPosInt(NCRId).isEmpty()) return null;
    
    return new NCRSpecification(travelerRoot, exitProcess, returnProcess, 
        NCRId, ncrCondition, dbType);
    
  }
  public ProcessNode getRoot() {return m_travelerRoot;}
  public ProcessNode getExit() {return m_exitProcess;}
  public ProcessNode getReturn() {return m_returnProcess;}
  public String getNCRId() {return m_NCRId;}
  public String getCondition() {return m_ncrCondition;}
  public String getDbType() {return m_dbType;}
}
