/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.yaml.snakeyaml.nodes.Node;
import java.io.Writer;
import javax.management.Attribute;
import javax.management.AttributeList;

/**
 *
 * @author jrb
 */
public class ProcessNode implements  TravelerElement
{
  static private void checkNonempty(String label, String toCheck) throws Exception {
    if ((toCheck == null) || (toCheck == "")) {
      throw new Exception("Every process step must have a " + label);
    }
  }

  // Deep copy
  public ProcessNode(ProcessNode parent, ProcessNode orig, int step) {
    m_parent = parent;
    /* handle m_parentEdge a bit further down */
    /* m_clonedFrom is a tricky one! */
    if (orig.m_originalId != null) m_originalId = new String(orig.m_originalId);
    if (orig.m_processId != null) m_processId = new String(orig.m_processId);
    m_sequenceCount = orig.m_sequenceCount;
    m_optionCount = orig.m_optionCount;
    m_name = new String(orig.m_name);
    m_isCloned = orig.m_isCloned;
    m_hardwareType = new String(orig.m_hardwareType);
    if (orig.m_hardwareRelationshipType != null)
      m_hardwareRelationshipType = new String(orig.m_hardwareRelationshipType);
    m_version = new String(orig.m_version);
    if (orig.m_userVersionString != null) 
      m_userVersionString = new String(orig.m_userVersionString);
    m_description = new String(orig.m_description);
    m_instructionsURL = new String(orig.m_instructionsURL);
    m_maxIteration = new String(orig.m_maxIteration);
    m_substeps = new String(orig.m_substeps);
    m_isOption = orig.m_isOption;
    m_travelerActionMask = orig.m_travelerActionMask;
    m_sourceDb = orig.m_sourceDb;
    if (m_parent != null) {
      m_parentEdge = new ProcessEdge(m_parent, this, step, 
                                     orig.getCondition());
    }
    if (orig.m_prerequisites != null) {
      int plen = orig.m_prerequisites.length;
      m_prerequisites = new Prerequisite[plen];
      for (int ip = 0; ip < plen; ip++) {
        m_prerequisites[ip] = new Prerequisite(this, orig.m_prerequisites[ip]);
      }
    }
    if (orig.m_resultNodes != null) {
      int rlen = orig.m_resultNodes.length;
      m_resultNodes = new PrescribedResult[rlen];
      for (int ir = 0; ir < rlen; ir++) {
        m_resultNodes[ir] = new PrescribedResult(this, orig.m_resultNodes[ir]);
      }
    }
    if (orig.m_children != null) {
      int clen = orig.m_children.length;
      m_children = new ProcessNode[clen];
      for (int ic = 0; ic < clen; ic++) {
        m_children[ic] = new ProcessNode(this, orig.m_children[ic], ic);
      }
    }
      
  }
  public ProcessNode(ProcessNode parent, ProcessNode.Importer imp) 
      throws Exception {
    m_parent = parent;
    m_name = imp.provideName();
    checkNonempty("name", m_name);

    if (parent != null) {
      m_parentEdge = imp.provideParentEdge(parent, this);
    }
    
    m_isCloned = imp.provideIsCloned();
    if (m_isCloned) return;
    
    m_isRef = imp.provideIsRef();
    if (m_isRef) {
      m_version = imp.provideVersion();
      return;
    }
    
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
    m_instructionsURL = imp.provideInstructionsURL();
    m_maxIteration = imp.provideMaxIteration();
    m_substeps = imp.provideSubsteps();
    m_sourceDb = imp.provideSourceDb();
    /* No more to do if we're a ref */

    
    checkNonempty("children type", m_substeps);
    if ((!m_substeps.equals("NONE")) 
       && (!m_substeps.equals("SEQUENCE"))
        && (!m_substeps.equals("SELECTION")) )  {
      throw new Exception("children type must be one of NONE/SEQUENCE/SELECTION");
    }
    m_travelerActionMask = imp.provideTravelerActionMask();
    //m_originalId = imp.provideOriginalId();

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
  public AttributeList getAttributes() {
    AttributeList pList = new AttributeList(20);
    pList.add(new Attribute("name", m_name));
    pList.add(new Attribute("version", m_version));
    if (m_userVersionString != null) {
      pList.add(new Attribute("user version string", m_userVersionString));
    }
    pList.add(new Attribute("hardware type", m_hardwareType));
    if (m_hardwareRelationshipType != null) {
      pList.add(new Attribute("hardware relationship type", m_hardwareRelationshipType));
    }
    pList.add(new Attribute("description", m_description));
    pList.add(new Attribute("max iterations", m_maxIteration));
    pList.add(new Attribute("child type", m_substeps));
    pList.add(new Attribute("traveler action mask", Integer.toString(m_travelerActionMask)));
    int nChild = m_optionCount;

    if (m_sequenceCount > nChild) nChild = m_sequenceCount;
    pList.add(new Attribute("# substeps", Integer.toString(nChild)));
    int nPrereq = 0;
    if (m_prerequisites != null)      nPrereq = m_prerequisites.length;
    pList.add(new Attribute("# prerequisites", Integer.toString(nPrereq)));
    int nResults = 0;
    if (m_resultNodes != null) nResults = m_resultNodes.length;
    pList.add(new Attribute("# solicited results", Integer.toString(nResults)));
    pList.add(new Attribute("Instructions URL", m_instructionsURL));
    return pList;
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
    String provideInstructionsURL();
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
    boolean provideIsRef();
    String provideSourceDb();
    ProcessNode provideChild(ProcessNode parent, int n) throws Exception;
    Prerequisite providePrerequisite(ProcessNode parent, int n) throws Exception;
    PrescribedResult provideResult(ProcessNode parent, int n) throws Exception;
    /* chance for source to do anything else it needs to do */
    void finishImport(ProcessNode process);
  }
  
  public void makeDot(Writer writer) throws EtravelerException {
    TravelerDotVisitor vis = new TravelerDotVisitor();
    vis.initOutput(writer, "\n");
    vis.visit(this, "dot file");
    vis.endOutput();
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
    void acceptInstructionsURL(String instructionsURL);
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
    void acceptIsRef(boolean isRef);
    // Do we need anything more having to do with edges?
    // What about acceptChild ?
    // Signal to node in case it needs to do anything after contents are complete
    void exportDone();
  }
  /*
   * used to build parallel tree structure.  Wrapper keeps a refernece to 
   * corresponding ProcessNode so interface is minimal
   */
  public interface Wrapper {
    void acceptName(String name);
    void acceptChildren(ProcessNode[] children);
    void exportDone();
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
      ptarget.acceptInstructionsURL(m_instructionsURL);
      ptarget.acceptMaxIteration(m_maxIteration);
      ptarget.acceptOriginalId(m_originalId);
      ptarget.acceptSubsteps(m_substeps);
      ptarget.acceptTravelerActionMask(m_travelerActionMask);
      ptarget.acceptPrerequisites(m_prerequisites);
      ptarget.acceptPrescribedResults(m_resultNodes);
      ptarget.acceptChildren(m_children);
      ptarget.acceptIsCloned(m_isCloned);
      ptarget.acceptIsRef(m_isRef);
      ptarget.exportDone();
    }
  }
  public void exportToWrapper(Wrapper target) {
    target.acceptName(m_name);
    target.acceptChildren(m_children);
  }

  void acceptCondition(String condition) {
    if (m_parentEdge != null) {
      m_parentEdge.setCondition(condition);
    }
  }
  
  public String getName() { return m_name;}
  public String getVersion() {return m_version;}
  public String getCondition() {
    if (m_parentEdge == null) return null;
    return m_parentEdge.getCondition();
  }
  public boolean isRef() {return m_isRef; }
  public void setProcessId(String id) {m_processId = id;}
  public void setOriginalId(String id) {m_originalId = id;}
  public void setDescription(String description) {m_description = description;}
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
  private boolean m_isRef=false;
  private String m_hardwareType=null;
  private String m_hardwareRelationshipType=null;
  private String m_processId=null;
  private String m_version=null;
  private String m_userVersionString=null;
  private String m_description=null;
  private String m_instructionsURL= "";
  private String m_maxIteration=null;
  private String m_substeps=null;
  private String m_sourceDb=null;
  private boolean m_isOption=false;
  private int m_travelerActionMask=0;
  private String m_originalId=null;
}
