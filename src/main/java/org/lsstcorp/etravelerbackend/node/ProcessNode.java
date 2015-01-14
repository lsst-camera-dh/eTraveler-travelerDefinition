/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

import org.lsstcorp.etravelerbackend.exceptions.IncompatibleChild;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import org.lsstcorp.etravelerbackend.db.DbConnection;
import org.yaml.snakeyaml.nodes.Node;
import java.io.Writer;
import javax.management.Attribute;
import javax.management.AttributeList;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
  // FIX ME:   do something about m_root, m_nodeMap
  public ProcessNode(ProcessNode parent, ProcessNode orig, int step) 
      throws EtravelerException {
    m_parent = parent;
    initNodeMap(parent);
    m_copiedFrom = orig;
    /* handle m_parentEdge a bit further down */
    /* m_clonedFrom is a tricky one!  */
    if (orig.m_originalId != null) m_originalId = new String(orig.m_originalId);
    if (orig.m_processId != null) {
      m_processId = new String(orig.m_processId);
      m_isRef = true;
    }
    m_name = new String(orig.m_name);
    m_version = new String(orig.m_version);
    m_isCloned = orig.m_isCloned;
    if (m_parent != null) {
      m_parentEdge = new ProcessEdge(m_parent, this, step, 
          orig.getCondition());
    }
    /* For cloned nodes, storeToNodeMap sets m_clonedFrom as side effect */
    if (m_isCloned != storeToNodeMap()) {
      throw new EtravelerException("Inconsistent clone information");
    }
    m_sourceDb = orig.m_sourceDb;
    if (m_isCloned)   { // get remainder of fields from our big brother
      copyFrom(m_clonedFrom);
    }  else {
      copyFrom(orig);
    }
    m_hasClones = orig.m_hasClones;
    m_sequenceCount = orig.m_sequenceCount;
    m_optionCount = orig.m_optionCount;
    
    
    if (orig.m_children != null) {
      int clen = orig.m_children.size();
      m_children = new ArrayList<ProcessNode>(clen);
      for (int ic = 0; ic < clen; ic++) {
        m_children.add(new ProcessNode(this, (orig.m_children).get(ic), ic));
      }
    }
      
  }
  /**
   *  Copy various attributes from model node to self. Assume a few key fields
   * (name, version, source db..) and edge information has already been handled
   * For now, do not copy children.  That will also be handled elsewhere.
   * @param model 
   */
  private void copyFrom(ProcessNode model) {
    m_hardwareType = new String(model.m_hardwareType);
    if (model.m_hardwareRelationshipType != null)
      m_hardwareRelationshipType = new String(model.m_hardwareRelationshipType);

    if (model.m_userVersionString != null) 
      m_userVersionString = new String(model.m_userVersionString);
    m_description = new String(model.m_description);
    m_instructionsURL = new String(model.m_instructionsURL);
    m_maxIteration = new String(model.m_maxIteration);
    m_substeps = new String(model.m_substeps);
    m_isOption = model.m_isOption;
    m_travelerActionMask = model.m_travelerActionMask;
   

    if (model.m_prerequisites != null) {
      int plen = model.m_prerequisites.size();
      m_prerequisites = new ArrayList<Prerequisite>(plen);
      //for (int ip = 0; ip < plen; ip++) {
      for (Prerequisite pre: model.m_prerequisites)  {
        m_prerequisites.add(new Prerequisite(this, pre));
      }
    }
    if (model.m_resultNodes != null) {
      int rlen = model.m_resultNodes.size();
      m_resultNodes = new ArrayList<PrescribedResult>(rlen);
      for (PrescribedResult res: model.m_resultNodes) {
        m_resultNodes.add(new PrescribedResult(this, res));
      }
    }
  }
  public ProcessNode(ProcessNode parent, ProcessNode.Importer imp) 
      throws Exception {
    m_parent = parent;
    initNodeMap(parent);
  
    m_name = imp.provideName();
    checkNonempty("name", m_name);
    m_version = imp.provideVersion();
    m_processId = imp.provideId();
    m_sourceDb = imp.provideSourceDb();
    if (parent != null) {
      m_parentEdge = imp.provideParentEdge(parent, this);
    }
    m_isRef = imp.provideIsRef();
    m_isCloned = imp.provideIsCloned();
    if (m_isCloned) {
      if (!storeToNodeMap()) {
        throw new EtravelerException("Inconsistent clone information");
      }
      return;
    }  else {
      if (storeToNodeMap()) {
        throw new EtravelerException("Inconsistent clone information");
      }
    }
 
    if (m_isRef) return;
    
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
    
    /*
    try {
      checkNonempty("hardware relationship type", m_hardwareRelationshipType);
    } catch (Exception ex) {
      if (parent != null) {  // child steps inherit relationship type
        m_hardwareRelationshipType = parent.m_hardwareRelationshipType;
      }
    }
    */
    
    m_userVersionString = imp.provideUserVersionString();
    m_description = imp.provideDescription();
    m_instructionsURL = imp.provideInstructionsURL();
    m_maxIteration = imp.provideMaxIteration();
    if ((m_parent == null) && (!m_maxIteration.equals("1"))) {
      throw new EtravelerException("Root step may not have max iteration > 1");
    }

    m_substeps = imp.provideSubsteps();
    m_sourceDb = imp.provideSourceDb();
    m_processId = imp.provideId();
    m_originalId = imp.provideOriginalId();
    /* No more to do if we're a ref */

    
    checkNonempty("children type", m_substeps);
    if ((!m_substeps.equals("NONE")) 
       && (!m_substeps.equals("SEQUENCE"))
        && (!m_substeps.equals("SELECTION")) )  {
      throw new Exception("children type must be one of NONE/SEQUENCE/SELECTION");
    }
    m_travelerActionMask = imp.provideTravelerActionMask();
    //m_originalId = imp.provideOriginalId();
    if ( ( (m_travelerActionMask & TravelerActionBits.HARNESSED) != 0) &&
        (!m_substeps.equals("NONE")) ) {
        throw new EtravelerException("Harnessed steps may not have substeps");
    }
    boolean automatable = ((m_travelerActionMask & TravelerActionBits.AUTOMATABLE) != 0);
    if (automatable & !(m_substeps.equals("SEQUENCE"))) {
      throw new EtravelerException("Step " + m_name + " is not automatable!");
    }
    int nPrereq = imp.provideNPrerequisites();
    if (nPrereq > 0) {
      m_prerequisites = new ArrayList<Prerequisite>(nPrereq);
      for (int iPrereq = 0; iPrereq < nPrereq; iPrereq++) {
        m_prerequisites.add(imp.providePrerequisite(parent, iPrereq));
      }
    }
    int nResults = imp.provideNPrescribedResults();
    if (nResults > 0) {
      m_resultNodes = new ArrayList<PrescribedResult>(nResults);
      for (int iResult = 0; iResult < nResults; iResult++) {
        m_resultNodes.add(imp.provideResult(parent, iResult));
      }
    }
    int nChildren = imp.provideNChildren();
    if (nChildren > 0)  {
      m_children = new ArrayList<ProcessNode>(nChildren);
      if (m_substeps.equals("SEQUENCE")) { m_sequenceCount = nChildren;}
      else {m_optionCount = nChildren;}
      for (int iChild = 0; iChild < nChildren; iChild++) {
        m_children.add(imp.provideChild(this, iChild));
      }
    }  
    if (automatable)  { // check all non-ref children are harnessed jobs or are
      // themselves automatable
      for (ProcessNode child: m_children) {
        if (!child.m_isRef) {
          if (((child.m_travelerActionMask & 
              (TravelerActionBits.HARNESSED | TravelerActionBits.AUTOMATABLE)) == 0)) {
            throw new EtravelerException("Step " + m_name +
                " is not automatable due to non-harnessed or non-automated child step");
          }
        }
      }
    }
    if (m_parent != null) {
      m_isOption = m_parent.m_substeps.equals("SELECTION");
    }
  }
  /**
   *  When making a new traveler, set up map to associate (name, version) with
   * ProcessNode.  Only make entry the first time the (name, version) combination
   * occurs.  Other nodes with same (name, version) are clones
   * @param parent 
   */
  private void initNodeMap(ProcessNode parent) {
    if (parent == null)  {
      m_root = this;
      m_nodeMap = new ConcurrentHashMap<String, ProcessNode>();
    }  else {
      m_nodeMap = m_parent.m_nodeMap;
    }
  }
  /**
   * 
   * @return true if we're cloned from another node 
   */

  private boolean storeToNodeMap() {
    ProcessNode val = m_nodeMap.putIfAbsent(makeKey(), this);
    if (val == null) return false;
    boolean isCloned = (val != this);
    if (isCloned) {
      m_clonedFrom = val;
      m_clonedFrom.m_hasClones = true;
      m_clonedFrom.addBuddy(this);

    }
    return isCloned;
  }
  private void addBuddy(ProcessNode buddy)  {
    if (m_buddies == null) m_buddies = new ArrayList<ProcessNode>();
    m_buddies.add(m_buddies.size(), buddy);
  }

  private String makeKey() {
    return m_name + '%' + m_version;
  }
  public AttributeList getAttributes() {
    AttributeList pList = new AttributeList(20);
    pList.add(new Attribute("name", m_name));
    pList.add(new Attribute("version", m_version));
    if (m_userVersionString != null) {
      pList.add(new Attribute("user version string", m_userVersionString));
    } else {
      pList.add(new Attribute("user version string", ""));
    }
    pList.add(new Attribute("hardware type", m_hardwareType));
    if (m_hardwareRelationshipType != null) {
      pList.add(new Attribute("hardware relationship type", m_hardwareRelationshipType));
    } else {
      pList.add(new Attribute("hardware relationship type", ""));
    }
    pList.add(new Attribute("description", m_description));
    pList.add(new Attribute("max iterations", m_maxIteration));
    pList.add(new Attribute("child type", m_substeps));
    pList.add(new Attribute("traveler action mask", Integer.toString(m_travelerActionMask)));
    int nChild = m_optionCount;

    if (m_sequenceCount > nChild) nChild = m_sequenceCount;
    pList.add(new Attribute("# substeps", Integer.toString(nChild)));
    int nPrereq = 0;
    if (m_prerequisites != null)      nPrereq = m_prerequisites.size();
    pList.add(new Attribute("# prerequisites", Integer.toString(nPrereq)));
    int nResults = 0;
    if (m_resultNodes != null) nResults = m_resultNodes.size();
    pList.add(new Attribute("# solicited results", Integer.toString(nResults)));
    pList.add(new Attribute("instructions URL", m_instructionsURL));
   
    if (m_isOption) {
      pList.add(new Attribute("condition", m_parentEdge.getCondition()));
    }
    pList.add(new Attribute("Edited", Boolean.toString(m_edited)));
    return pList;
  }
  
  public AttributeList getPrerequisiteAttributes(int ix) {
    if (ix >= m_prerequisites.size()) return null;
    return m_prerequisites.get(ix).getAttributes();
  }
  
  public AttributeList getResultAttributes(int ix) {
    if (ix >= m_resultNodes.size()) return null;
    return m_resultNodes.get(ix).getAttributes();
  }
  /**
   * Remove prerequisites as directed by changedPrereq array
   * @param changedPrereq Entry for each prerequisite.  0 means unchanged,
   * 1 means modified, 2 means should be removed
   */
  public void rmPrereqs(ArrayList<Integer> changedPrereq)  {
    /* Remove from back so indices are all good */
    int ix = changedPrereq.size() - 1;
    for (;  ix >= 0; ix--) {
      if (changedPrereq.get(ix) == 2) {
        m_prerequisites.remove(ix);
      }
    }
  }
  public void rmResults(ArrayList<Integer> changedResult)  {
    /* Remove from back so indices are all good */
    int ix = changedResult.size() - 1;
    for (;  ix >= 0; ix--) {
      if (changedResult.get(ix) == 2) {
        m_resultNodes.remove(ix);
      }
    }
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
  public void accept(TravelerVisitor visitor, String activity, Object cxt) 
      throws EtravelerException {
    visitor.visit(this, activity, cxt);
  }
  
 
  /**
   * Interface for importing process nodes from another representation.
   * ProcessNodeDb implements this interface.
   */
  public interface Importer {
    String provideId();
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
    String provideOriginalId();
    int provideNChildren();
    int provideNPrerequisites();
    int provideNPrescribedResults();
    // More provides for parent edge:
    //String provideParentEdgeId();
    ProcessEdge provideParentEdge(ProcessNode parent, ProcessNode child);
    String provideEdgeCondition();
    int provideEdgeStep();
    boolean provideIsCloned();
    boolean provideHasClones();
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
    vis.visit(this, "dot file", null);
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
    void acceptChildren(ArrayList<ProcessNode> children);
    void acceptPrerequisites(ArrayList<Prerequisite> prerequisites);
    void acceptPrescribedResults(ArrayList<PrescribedResult> prescribedResults);
    // Following is to transmit condition assoc. with parent edge
    void acceptCondition(String condition); 
    void acceptClonedFrom(ProcessNode process);
    void acceptHasClones(boolean hasClones);
    void acceptIsCloned(boolean isCloned);
    void acceptIsRef(boolean isRef);
    void acceptEdited(boolean edited);
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
    void acceptChildren(ArrayList<ProcessNode> children);
    void exportDone();
  }
  public void exportTo(TravelerElement.ExportTarget target) {
    if (target instanceof ProcessNode.ExportTarget) {
      ProcessNode.ExportTarget ptarget = (ProcessNode.ExportTarget) target;  
  
      ptarget.acceptId(m_processId);
      if (m_parentEdge != null) {
        ptarget.acceptCondition(m_parentEdge.getCondition());
      }
      ptarget.acceptIsCloned(m_isCloned);
      ptarget.acceptName(m_name);
      /* Some targets, in particular yaml, need to know early on if the
       * node is a clone
       */
  
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
  
      ptarget.acceptIsRef(m_isRef);
      ptarget.acceptEdited(m_edited);
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

  /**
   * Undo edit on ourself, making use of stashed m_copiedFrom
   */
  public boolean recover(boolean recurse) {
    if (m_copiedFrom.m_isCloned) {
      copyFrom(m_clonedFrom);
      m_isCloned = true;
    } else {
      copyFrom(m_copiedFrom, recurse);
    }
    return true;
  }

  /**
   * Undo edit on ourself and all clones.
   */
  public boolean recoverAll(boolean recurse) {
    if (m_isCloned) return m_clonedFrom.recoverAll(recurse);
    
    recover(recurse);
    // now do all buddies
    updateBuddies(recurse);
    return true;
  }

  public void updateBuddies(boolean recurse) {
    for (ProcessNode node:  m_buddies) {
      node.recover(recurse);
    }
  }

  /**
   * Undo edit on descendent ProcessNode. Called on root of traveler containing
   *   edited node
   * @param path   Path to node to be recovered (starting at traveler root)
   * @param editType  One of "modified", "deleted", "added"
   * @param orig    Original traveler from which we were cloned
   * @return 
   */
  boolean recover(ProcessNode theNode, String path, String editType, 
      ProcessNode origRoot) {
    // For now just handling "modify"
    // Find the corresponding node in the original traveler
    ProcessNode theSource = origRoot;
    String[] cmps = path.split("/");
    for (String cmp: cmps) {
      if (cmp.isEmpty()) continue;
      for (ProcessNode child: theSource.m_children) {
        if (child.m_name.equals(cmp)) {
          theSource = child;
          break;
        } 
      }
    }
    theNode.copyFrom(theSource, false);
    return true;
  }
  /**
   * Copy immediate node attributes, prereqs and prescribed results from
   * source node.  Do not copy children unless recurs is true
   * @param src 
   * @param recurs
   */
  void copyFrom(ProcessNode src, boolean recurs)  {
    // some things can't have changed; name, hardwareType, userVersionString,
    // hardwareRelationshipType, isOption, travelerActionMask,
    // Don't bother copying those.
  
    m_isRef = src.m_isRef;
    m_edited = false;   // FIX ME could cause trouble if child was modified 
    m_processId = src.m_processId;
    m_version = src.m_version;
    m_originalId = src.m_originalId;
    m_description = src.m_description;
    m_instructionsURL = src.m_instructionsURL;
    m_maxIteration = src.m_maxIteration;
    m_userVersionString = src.m_userVersionString;
    // For now, do not handle recurs==true, so leave option count and seq count
    // alone
    if (src.getPrerequisiteCount() > 0) {
      m_prerequisites = new ArrayList<Prerequisite>(src.getPrerequisiteCount());
      for (Prerequisite srcPre: src.m_prerequisites) {
        m_prerequisites.add(new Prerequisite(this, srcPre));
      }
    } else m_prerequisites = null;
    if (src.getResultCount() > 0) {
      m_resultNodes = new ArrayList<PrescribedResult>(src.getResultCount());
      for (PrescribedResult srcRes: src.m_resultNodes) {
        m_resultNodes.add(new PrescribedResult(this, srcRes));
      }
    }
    
  }
  
  public ProcessNode getParent() {return m_parent;}
  public ProcessEdge getParentEdge() { return m_parentEdge;}
  public String getName() { return m_name;}
  public String getVersion() {return m_version;}
  public String getUserVersionString() {return m_userVersionString;}
  public String getProcessId() {return m_processId;}
  public String getHardwareType() {return m_hardwareType;}
  public String getDescription() {return m_description;}
  public String getInstructionsURL() { return m_instructionsURL;}
  public String getMaxIteration() {return m_maxIteration;}
  public String getCondition() {
    if (m_parentEdge == null) return null;
    return m_parentEdge.getCondition();
  }
  public int getPrerequisiteCount() { 
    if (m_prerequisites == null) return 0;
    return m_prerequisites.size(); }
  public int getResultCount() { 
    if (m_resultNodes == null) return 0;
    return m_resultNodes.size(); }
  public boolean getIsEdited() {return m_edited;}
  public String getSourceDb() {return m_sourceDb;}
  public boolean isRef() {return m_isRef; }
  public void setProcessId(String id) {m_processId = id;}
  public void setOriginalId(String id) {m_originalId = id;}
  public void setDescription(String description) {m_description = description;}
  public void setVersion(String version)  {m_version = version;}
  public void setUserVersionString(String ustring) {m_userVersionString = ustring;}
  public void setInstructionsURL(String url) {m_instructionsURL = url;}
  public void setMaxIteration(String maxIt) {m_maxIteration = maxIt;}
  public void newVersion() {
    m_edited = true;
    m_isRef = false;
    m_version = "modified";
    ProcessNode parent = m_parent;
    while (parent != null) {
      if (parent.m_edited == false) {
        parent.m_edited = true;
        parent.m_isRef = false;
        // parent.m_version = "modified";
        parent = parent.m_parent;
      } else break;
    }
  }
  public ArrayList<Prerequisite> getPrerequisites() {
    return m_prerequisites;
  }
  public ArrayList<PrescribedResult> getResults() {
    return m_resultNodes;
  }
  public boolean isCloned() { return m_isCloned; }
  public boolean hasClones() { return m_hasClones; }
  public ProcessNode clonedFrom() { return m_clonedFrom;}
  public boolean hasChildren() {
    if (m_isCloned) return m_clonedFrom.hasChildren();
    if (m_children == null) return false;
    return (m_children.size() > 0);
  }

  private ProcessNode m_parent=null;
  private ProcessEdge m_parentEdge=null;
  // If m_clonedFrom set to non-null, most other properties are ignored
  private ProcessNode m_clonedFrom=null;  
  private ProcessNode m_copiedFrom=null;
  private int m_sequenceCount=0;
  private int m_optionCount=0;
  private ArrayList<ProcessNode> m_children=null;
  private ArrayList<Prerequisite> m_prerequisites=null;
  private ArrayList<PrescribedResult> m_resultNodes=null;
  private String m_name=null;
  private boolean m_isCloned=false;
  private boolean m_hasClones=false;
  private boolean m_isRef=false;
  private String m_hardwareType=null;
  private String m_hardwareRelationshipType=null;
  private String m_processId=null;
  private String m_version=null;
  private String m_userVersionString="";
  private String m_description=null;
  private String m_instructionsURL= "";
  private String m_maxIteration=null;
  private String m_substeps=null;
  private String m_sourceDb=null;
  private boolean m_isOption=false;
  private int m_travelerActionMask=0;
  private String m_originalId=null;
  private boolean m_edited=false;
  private ProcessNode m_root=null;
 /*
   * Use the following data structures to deal with clones.  Hash map
   * has an entry for each unique (name, version) encountered with
   * value = first ProcessNode encountered which matches (call it big brother)
   * If there are others, each of these has m_clonedFrom set to big brother.
   * And m_buddies for big brother keeps track of all the clones it has.
   * So any node in such an affinity group can find all the others.
   */
  private ConcurrentHashMap<String, ProcessNode> m_nodeMap=null;
  private ArrayList<ProcessNode> m_buddies=null;
}