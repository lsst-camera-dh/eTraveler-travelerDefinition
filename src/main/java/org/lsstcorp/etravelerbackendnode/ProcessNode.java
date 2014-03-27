/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.yaml.snakeyaml.nodes.Node;
/**
 *
 * @author jrb
 */
public class ProcessNode implements  TravelerElement {
  static private void checkNonempty(String label, String toCheck) throws Exception {
    if ((toCheck == null) || (toCheck == "")) {
      throw new Exception("Every process step must have a " + label);
    }
  }
  public ProcessNode(ProcessNode parent, ProcessNode.Importer imp) 
      throws Exception {
    m_parent = parent;
    m_name = imp.provideName();
    checkNonempty("name", m_name);
    m_isCloned = imp.provideIsCloned();
    
    if (m_isCloned) return;
    
    m_hardwareType = imp.provideHardwareType();
    try {
      checkNonempty("hardware type", m_hardwareType);
      if ((parent != null) && (m_hardwareType != m_parent.m_hardwareType)) {
        throw new IncompatibleChild(m_name, parent.m_name, 
            "hardware type mismatch");
      }
    } catch (Exception ex) {
      if (parent != null) {
        m_hardwareType = parent.m_hardwareType;
      } else  {
        throw ex;
      }
    }
    m_hardwareRelationshipType = imp.provideHardwareRelationshipType();
    try {
      checkNonempty("hardware relationship type", m_hardwareRelationshipType);
    } catch (Exception ex) {
      if (parent != null) {  // child steps inherit relationship type
        m_hardwareRelationshipType = parent.m_hardwareRelationshipType;
      }
    }
    m_version = imp.provideVersion();
    m_userVersionString = imp.provideUserVersionString();
    m_description = imp.provideDescription();
    m_maxIteration = imp.provideMaxIteration();
    m_substeps = imp.provideSubsteps();
    checkNonempty("children type", m_substeps);
    if ((!m_substeps.equals("NONE")) 
       && (!m_substeps.equals("SEQUENCE"))
        && (!m_substeps.equals("SELECTION")) )  {
      throw new Exception("children type must be one of NONE/SEQUENCE/SELECTION");
    }
    m_travelerActionMask = imp.provideTravelerActionMask();
    //m_originalId = imp.provideOriginalId();
    if (parent != null) {
      m_parentEdge = imp.provideParentEdge(parent, this);
    }
    int nPrereq = imp.provideNPrerequisites();
    if (nPrereq > 0) {
      m_prerequisites = new Prerequisite[nPrereq];
      for (int iPrereq = 0; iPrereq < nPrereq; iPrereq++) {
        m_prerequisites[iPrereq] = imp.providePrerequisite(parent, iPrereq);
      }
    }
    int nResults = imp.provideNPrescribedResults();
    if (nResults > 0) {
      m_resultNodes = new PrescribedResult[nResults];
      for (int iResult = 0; iResult < nResults; iResult++) {
        m_resultNodes[iResult] = imp.provideResult(parent, iResult);
      }
    }
    int nChildren = imp.provideNChildren();
    if (nChildren > 0)  {
      m_children = new ProcessNode[nChildren];
      if (m_substeps.equals("SEQUENCE")) { m_sequenceCount = nChildren;}
      else {m_optionCount = nChildren;}
      for (int iChild = 0; iChild < nChildren; iChild++) {
        m_children[iChild] = imp.provideChild(this, iChild);
      }
    }  
    
    m_isOption = (m_optionCount > 0);
  }
  
  public int writeDb() {
        return 0;  // for now
    }
  /**
   * Verify that various values in the traveler are consistent with 
   * the database to which the traveler might be added
   * @param db
   * @return 
   */
  public boolean dbVerify(DbConnection db)  {
    // If we're top node, check that hardware type is valid
    // Check hardware relationship type (maybe cache known valid ones?)
    
    // Verify prerequisites (probably call a Prerequisite.verify(..) method ) 
    //  Similarly for PrescribedResult
    
    // Then recurse on children
    return true;
  }
 
  static public ProcessNode findProcess(String name) {
      return null;
  }
  public void accept(TravelerVisitor visitor, String activity) 
      throws EtravelerException {
    visitor.visit(this, activity);
  }
  
 
  /**
   * Interface for importing process nodes from another representation.
   * ProcessNodeDb implements this interface.
   */
  public interface Importer {
    //String provideId();
    String provideName();
    String provideHardwareType();
    String provideHardwareRelationshipType();
    String provideVersion();
    String provideUserVersionString();
    String provideDescription();
    String provideMaxIteration();
    String provideSubsteps();
    int provideTravelerActionMask();
    //String provideOriginalId();
    int provideNChildren();
    int provideNPrerequisites();
    int provideNPrescribedResults();
    // More provides for parent edge:
    //String provideParentEdgeId();
    ProcessEdge provideParentEdge(ProcessNode parent, ProcessNode child);
    String provideEdgeCondition();
    int provideEdgeStep();
    boolean provideIsCloned();
    ProcessNode provideChild(ProcessNode parent, int n) throws Exception;
    Prerequisite providePrerequisite(ProcessNode parent, int n) throws Exception;
    PrescribedResult provideResult(ProcessNode parent, int n) throws Exception;
  }
  /**
   * Interface for exporting a process node to another representation, such
   * as a text file
   */
  public interface ExportTarget extends TravelerElement.ExportTarget {
    void acceptId(String id);
    void acceptName(String name);
    void acceptHardwareType(String hardwareType);
    void acceptHardwareRelationshipType(String hardwareRelationshipType);
    void acceptVersion(String version);
    void acceptUserVersionString(String userVersionString);
    void acceptDescription(String description);
    void acceptMaxIteration(String maxIterations);
    void acceptSubsteps(String substeps);
    void acceptTravelerActionMask(int travelerActionMask);
    void acceptOriginalId(String originalId);
    void acceptChildren(ProcessNode[] children);
    void acceptPrerequisites(Prerequisite[] prerequisites);
    void acceptPrescribedResults(PrescribedResult[] prescribedResults);
    // Following is to transmit condition assoc. with parent edge
    void acceptCondition(String condition); 
    void acceptClonedFrom(ProcessNode process);
    void acceptIsCloned(boolean isCloned);
    // Do we need anything more having to do with edges?
    // What about acceptChild ?
  }
  public void exportTo(TravelerElement.ExportTarget target) {
    if (target instanceof ProcessNode.ExportTarget) {
      ProcessNode.ExportTarget ptarget = (ProcessNode.ExportTarget) target;  
  
      ptarget.acceptId(m_processId);
      if (m_parentEdge != null) {
        ptarget.acceptCondition(m_parentEdge.getCondition());
      }
      ptarget.acceptName(m_name);
      ptarget.acceptHardwareType(m_hardwareType);
      ptarget.acceptHardwareRelationshipType(m_hardwareRelationshipType);
      ptarget.acceptVersion(m_version);
      ptarget.acceptUserVersionString(m_userVersionString);
      ptarget.acceptDescription(m_description);
      ptarget.acceptMaxIteration(m_maxIteration);
      ptarget.acceptOriginalId(m_originalId);
      ptarget.acceptSubsteps(m_substeps);
      ptarget.acceptTravelerActionMask(m_travelerActionMask);
      ptarget.acceptPrerequisites(m_prerequisites);
      ptarget.acceptPrescribedResults(m_resultNodes);
      ptarget.acceptChildren(m_children);
      ptarget.acceptIsCloned(m_isCloned);
    }
  }
  
  public String getName() { return m_name;}
  private ProcessNode m_parent=null;
  private ProcessEdge m_parentEdge=null;
  // If m_clonedFrom set to non-null, most other properties are ignored
  private ProcessNode m_clonedFrom=null;  
  private int m_sequenceCount=0;
  private int m_optionCount=0;
  private ProcessNode[] m_children=null;
  private Prerequisite[] m_prerequisites=null;
  private PrescribedResult[] m_resultNodes=null;
  private String m_name=null;
  private boolean m_isCloned=false;
  private String m_hardwareType=null;
  private String m_hardwareRelationshipType=null;
  private String m_processId=null;
  private String m_version=null;
  private String m_userVersionString=null;
  private String m_description=null;
  private String m_maxIteration=null;
  private String m_substeps=null;
  private boolean m_isOption=false;
  private int m_travelerActionMask=0;
  private String m_originalId=null;
}
