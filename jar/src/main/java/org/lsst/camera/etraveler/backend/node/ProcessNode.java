/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;

import org.lsst.camera.etraveler.backend.exceptions.IncompatibleChild;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.db.DbConnection;
// import org.yaml.snakeyaml.nodes.Node;
// import java.io.Writer;
import javax.management.Attribute;
import javax.management.AttributeList;
// import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jrb
 */
public class ProcessNode implements  TravelerElement
{
  static private void checkNonempty(String label, String toCheck) throws Exception {
    if ((toCheck == null) || (toCheck.equals(""))) {
      throw new Exception("Every process step must have a " + label);
    }
  }
  public static String hardwareDefaultString() { return "(otherwise)";}

  // Deep copy
  // FIX ME:   do something about m_root, m_nodeMap
  public ProcessNode(ProcessNode parent, ProcessNode orig, int step) 
      throws EtravelerException {
    m_parent = parent;
    initNodeMap(parent);
    m_copiedFrom = orig;
    /* handle m_parentEdge a bit further down */
    /* m_clonedFrom is a tricky one!  */
    if (orig.m_originalId != null) m_originalId = orig.m_originalId;
    if (orig.m_processId != null) {
      m_processId = orig.m_processId;
      m_isRef = true;
    }
    m_name = orig.m_name;
    m_version = orig.m_version;
    m_isCloned = orig.m_isCloned;
    if (m_parent != null) {
      m_parentEdge = new ProcessEdge(m_parent, this, step, 
                                     orig.getCondition(),
                                     orig.getHardwareCondition());
    }
    /* For cloned nodes, storeToNodeMap sets m_clonedFrom as side effect */
    if (m_isCloned != storeToNodeMap()) {
      throw new EtravelerException("Inconsistent clone information");
    }
    if (orig.m_sourceDb != null) m_sourceDb = orig.m_sourceDb;
     if (orig.m_newLocation != null) m_newLocation = orig.m_newLocation;
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
      m_children = new ArrayList< >(clen);
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
    m_hardwareGroup = model.m_hardwareGroup;

    if (model.m_userVersionString != null) 
      m_userVersionString = model.m_userVersionString;
    if (model.m_jobname != null) {
      m_jobname = model.m_jobname;
    }		
    m_description = model.m_description;
    m_shortDescription = model.m_shortDescription;
    m_instructionsURL = model.m_instructionsURL;
    m_maxIteration = model.m_maxIteration;
    m_substeps = model.m_substeps;
    m_substepType = model.m_substepType;
    m_travelerActionMask = model.m_travelerActionMask;
    if (model.m_newLocation != null) 
      m_newLocation = model.m_newLocation;
    if (model.m_locationSite != null) 
      m_locationSite = model.m_locationSite;
    if (model.m_newStatus != null)  
      m_newStatus = model.m_newStatus;
    if (model.m_labelGroup != null) 
      m_labelGroup = model.m_labelGroup;
    
    if (model.m_newStatusId != null)
      m_newStatusId = model.m_newStatusId;

    if (model.m_prerequisites != null) {
      int plen = model.m_prerequisites.size();
      m_prerequisites = new ArrayList< >(plen);

      for (Prerequisite pre: model.m_prerequisites)  {
        m_prerequisites.add(new Prerequisite(this, pre));
      }
    }
    if (model.m_resultNodes != null) {
      int rlen = model.m_resultNodes.size();
      m_resultNodes = new ArrayList< >(rlen);
      for (PrescribedResult res: model.m_resultNodes) {
        m_resultNodes.add(new PrescribedResult(this, res));
      }
    }
    if (model.m_optionalResultNodes != null) {
      int orlen = model.m_optionalResultNodes.size();
      m_optionalResultNodes = new ArrayList< >(orlen);
      for (PrescribedResult res: model.m_optionalResultNodes) {
        m_optionalResultNodes.add(new PrescribedResult(this, res));
      }
    }
    if (model.m_relationshipTasks != null) {
      int rlen = model.m_relationshipTasks.size();
      m_relationshipTasks = new ArrayList< >(rlen);
      for (RelationshipTask rel: model.m_relationshipTasks) {
        m_relationshipTasks.add(new RelationshipTask(this, rel));
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
    
    m_hardwareGroup = imp.provideHardwareGroup();
    try {
      checkNonempty("hardware group", m_hardwareGroup);
      if ((parent != null) &&
          (!m_hardwareGroup.equals(m_parent.m_hardwareGroup))) {
        throw new IncompatibleChild(m_name, parent.m_name, 
            "hardware group mismatch");
      }
    } catch (Exception ex) {
      if (parent != null) {
        m_hardwareGroup = parent.m_hardwareGroup;
      } else {
        throw ex;
      }
    }    

    m_jobname = imp.provideJobname();
    m_userVersionString = imp.provideUserVersionString();
    m_description = imp.provideDescription();
    m_shortDescription = imp.provideShortDescription();
    m_instructionsURL = imp.provideInstructionsURL();
    m_maxIteration = imp.provideMaxIteration();
    m_substeps = imp.provideSubsteps();
    m_travelerActionMask = imp.provideTravelerActionMask();
    //m_originalId = imp.provideOriginalId();
    if ( ( (m_travelerActionMask & TravelerActionBits.HARNESSED) != 0) &&
        (!m_substeps.equals("NONE")) ) {
        throw new EtravelerException("Harnessed steps may not have substeps");
    }
    boolean automatable = ((m_travelerActionMask & TravelerActionBits.AUTOMATABLE) != 0);
    if (automatable && !(m_substeps.equals("SEQUENCE"))) {
      throw new EtravelerException("Step " + m_name + " is not automatable!");
    }
    m_newLocation = imp.provideNewLocation();
    m_locationSite = imp.provideLocationSite();
    if (((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_LOCATION) !=0)
        && (m_newLocation == null) && (m_locationSite == null) )
      m_newLocation = "(?)";
    m_newStatus = imp.provideNewStatus();
    m_labelGroup = imp.provideLabelGroup();
    if (((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_STATUS) !=0) &&
        (m_newStatus == null) ) m_newStatus = "(?)";
    if (((m_travelerActionMask & TravelerActionBits.ADD_LABEL) !=0) &&
        (m_newStatus == null) && (m_labelGroup == null) ) m_newStatus = "(?)";
    if ((m_parent == null) && (!m_maxIteration.equals("1"))) {
      throw new EtravelerException("Root step may not have max iteration > 1");
    }
  
    m_sourceDb = imp.provideSourceDb();
    m_processId = imp.provideId();
    m_originalId = imp.provideOriginalId();
    m_permissionGroups = imp.providePermissionGroups();
    m_travelerTypeLabels = imp.provideTravelerTypeLabels();
    
    checkNonempty("children type", m_substeps);
    if ((!m_substeps.equals("NONE")) 
       && (!m_substeps.equals("SEQUENCE"))
        && (!m_substeps.equals("SELECTION"))
        && (!m_substeps.equals("HARDWARE_SELECTION")) )  {
      throw new
        Exception("children type must be one of NONE/SEQUENCE/SELECTION/HARDWARE_SELECTION");
    }
   
    int nPrereq = imp.provideNPrerequisites();
    if (nPrereq > 0) {
      m_prerequisites = new ArrayList< >(nPrereq);
      for (int iPrereq = 0; iPrereq < nPrereq; iPrereq++) {
        m_prerequisites.add(imp.providePrerequisite(parent, iPrereq));
      }
    }
    int nResults = imp.provideNPrescribedResults();
    if (nResults > 0) {
      m_resultNodes = new ArrayList< >(nResults);
      for (int iResult = 0; iResult < nResults; iResult++) {
        m_resultNodes.add(imp.provideResult(parent, iResult));
      }
    }
    int nOptionalResults = imp.provideNOptionalResults();
    if (nOptionalResults > 0) {
      m_optionalResultNodes = new ArrayList< >(nResults);
      for (int iOpt = 0; iOpt < nOptionalResults; iOpt++) {
        m_optionalResultNodes.add(imp.provideOptionalResult(parent, iOpt));
      }
    }
    //
    int nRel = imp.provideNRelationshipTasks();
    if (nRel > 0) {
      m_relationshipTasks = new ArrayList< >(nRel);
      for (int iRel = 0; iRel < nRel; iRel++) {
        m_relationshipTasks.add(imp.provideRelationshipTask(parent, iRel));
      }
    }

    //
    int nChildren = imp.provideNChildren();
    if (nChildren > 0)  {
      m_children = new ArrayList< >(nChildren);
      if (m_substeps.equals("SEQUENCE")) { m_sequenceCount = nChildren;}
      else {m_optionCount = nChildren;}
      for (int iChild = 0; iChild < nChildren; iChild++) {
        ProcessNode child = imp.provideChild(this, iChild);
        if (m_substeps.equals("HARDWARE_SELECTION") &&
            (child.getHardwareCondition() == null)) {
          child.getParentEdge().setHardwareCondition(ProcessNode.hardwareDefaultString());
        }
        m_children.add(child);
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
      m_substepType = m_parent.m_substeps;
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
      m_nodeMap = new ConcurrentHashMap< >();
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
    if (m_buddies == null) m_buddies = new ArrayList< >();
    m_buddies.add(m_buddies.size(), buddy);
  }

  private String makeKey() {
    // return m_name + '%' + m_version;
    return m_name;
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
    if (m_jobname != null) {
      pList.add(new Attribute("jobname", m_jobname));
    } else {
      pList.add(new Attribute("jobname", ""));
    }

    if (m_hardwareGroup != null)
      pList.add(new Attribute("hardware group", m_hardwareGroup));
    pList.add(new Attribute("description", m_description));
    pList.add(new Attribute("short description", m_shortDescription));
    pList.add(new Attribute("max iterations", m_maxIteration));
    pList.add(new Attribute("child type", m_substeps));
    pList.add(new Attribute("traveler action mask", Integer.toString(m_travelerActionMask)));
    if (m_permissionGroups != null) {
      if (m_permissionGroups.size() > 0) {
        String groupString = m_permissionGroups.get(0);
        for (int i=1; i < m_permissionGroups.size(); i++) {
          groupString += ", " + m_permissionGroups.get(i);
        }
        pList.add(new Attribute("permission groups", groupString));
      }
    }
    if (m_travelerTypeLabels != null) {
      if (m_travelerTypeLabels.size() > 0) {
        String labelsString = m_travelerTypeLabels.get(0);
        for (int i=1; i < m_travelerTypeLabels.size(); i++) {
          labelsString += ", " + m_travelerTypeLabels.get(i);
        }
        pList.add(new Attribute("traveler type labels", labelsString));
      }
    }
    
    int nChild = m_optionCount;
    if (m_locationSite != null) {
      pList.add(new Attribute("new location site", m_locationSite));
    } else    if (m_newLocation != null) {
      pList.add(new Attribute("new location", m_newLocation));
    }
    if (m_newStatus != null)  {
      if ((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_STATUS) != 0) {
        pList.add(new Attribute("new status", m_newStatus));
      } else if ((m_travelerActionMask & TravelerActionBits.ADD_LABEL) != 0) {
        if (m_labelGroup != null) pList.add(new Attribute("add label from group", m_labelGroup));
        else pList.add(new Attribute("add label", m_newStatus));
      } else if ((m_travelerActionMask & TravelerActionBits.REMOVE_LABEL) != 0) {
        if (m_labelGroup != null) pList.add(new Attribute("remove label from group", m_labelGroup));
        else pList.add(new Attribute("remove label", m_newStatus));
      }
    }
    if (m_newStatus == null) {
      if ((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_STATUS) != 0)
        {
          pList.add(new Attribute("new status", "(?)"));
      } else if ((m_travelerActionMask & TravelerActionBits.ADD_LABEL) != 0) {
        if (m_labelGroup != null) pList.add(new Attribute("add label from group", m_labelGroup));
        else pList.add(new Attribute("add label", "(?)"));
      }
      else if ((m_travelerActionMask & TravelerActionBits.REMOVE_LABEL) != 0) {
        if (m_labelGroup != null)
          pList.add(new Attribute("remove label from group", m_labelGroup));
        else pList.add(new Attribute("remove label", "(?)"));
      }
    }
      
    if (m_sequenceCount > nChild) nChild = m_sequenceCount;
    pList.add(new Attribute("# substeps", Integer.toString(nChild)));
    int nPrereq = 0;
    if (m_prerequisites != null)      nPrereq = m_prerequisites.size();
    pList.add(new Attribute("# prerequisites", Integer.toString(nPrereq)));
    int nResults = 0;
    if (m_resultNodes != null) nResults = m_resultNodes.size();
    pList.add(new Attribute("# rqd solicited results", Integer.toString(nResults)));
    int nOptionalResults = 0;
    if (m_optionalResultNodes != null) nOptionalResults = m_optionalResultNodes.size();
    pList.add(new Attribute("# opt solicited results", 
                            Integer.toString(nOptionalResults)));
    int nRelationshipTasks = 0;
    if (m_relationshipTasks != null) 
      nRelationshipTasks = m_relationshipTasks.size();
    pList.add(new Attribute("# relationship tasks",
                            Integer.toString(nRelationshipTasks)));
    pList.add(new Attribute("instructions URL", m_instructionsURL));
   
    if (m_substepType.equals("SELECTION")) {
      pList.add(new Attribute("condition", m_parentEdge.getCondition()));
    } else {
      if (m_substepType.equals("HARDWARE_SELECTION")) {
      pList.add(new Attribute("hardwareTypeCondition", m_parentEdge.getHardwareCondition()));
    }

    }
    pList.add(new Attribute("Edited", Boolean.toString(m_edited)));
    return pList;
  }
  
  public AttributeList getPrerequisiteAttributes(int ix) {
    if (ix >= m_prerequisites.size()) return null;
    return m_prerequisites.get(ix).getAttributes();
  }
  
  public AttributeList getResultAttributes(int ix, boolean rqd) {
    if (rqd) {
      if (ix >= m_resultNodes.size()) return null;
      return m_resultNodes.get(ix).getAttributes();
    } else {
      if (ix >= m_optionalResultNodes.size()) return null;
      return m_optionalResultNodes.get(ix).getAttributes();
    }
  }
  public AttributeList getRelationshipAttributes(int ix) {
    if (ix >= m_relationshipTasks.size()) return null;
    return m_relationshipTasks.get(ix).getAttributes();
  }
  
  public StringArrayWriter collectOutput()  {
    StringArrayWriter wrt = new StringArrayWriter();
    TravelerPrintVisitor vis = new TravelerPrintVisitor();
    //String key = makeKey(name, version, hgroup, dbType);
    //if (!s_writers.containsKey(key)) {
    TravelerPrintVisitor.setEol("\n");
    vis.setWriter(wrt);
    TravelerPrintVisitor.setIndent("&nbsp;&nbsp");
    try {
      vis.visit(this, "Print Html", null);
    }  catch (EtravelerException ex)  {
        System.out.println("Print to Html failed with exception");
        System.out.println(ex.getMessage());
        return null;
    }
    return wrt;
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

  public void rmOptionalResults(ArrayList<Integer> changedResult)  {
    /* Remove from back so indices are all good */
    int ix = changedResult.size() - 1;
    for (;  ix >= 0; ix--) {
      if (changedResult.get(ix) == 2) {
        m_optionalResultNodes.remove(ix);
      }
    }
  }


  // Probably unused.  See instead ProcessNodeDb.writeToDb
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
    // this verification is handled elsewhere: see ProcessNodeDb.verify.
    // Probably this routine should go
    return true;
  }
 
  static public ProcessNode findProcess(String name) {
      return null;
  }
  @Override
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
    String provideHardwareGroup();
    String provideVersion();
    String provideJobname();
    String provideUserVersionString();
    String provideDescription();
    String provideShortDescription();
    String provideInstructionsURL();
    String provideMaxIteration();
    String provideNewLocation();
    String provideLocationSite();
    String provideNewStatus();
    String provideLabelGroup();
    String provideSubsteps();
    int provideTravelerActionMask();
    String provideOriginalId();
    ArrayList<String> providePermissionGroups();
    ArrayList<String> provideTravelerTypeLabels();
    int provideNChildren();
    int provideNPrerequisites();
    int provideNPrescribedResults();
    int provideNOptionalResults();
    int provideNRelationshipTasks();
    // More provides for parent edge:
    //String provideParentEdgeId();
    ProcessEdge provideParentEdge(ProcessNode parent, ProcessNode child);
    String provideEdgeCondition();
    String provideEdgeHardwareCondition();
    int provideEdgeStep();
    boolean provideIsCloned();
    boolean provideHasClones();
    boolean provideIsRef();
    String provideSourceDb();
    ProcessNode provideChild(ProcessNode parent, int n) throws Exception;
    Prerequisite providePrerequisite(ProcessNode parent, int n) throws Exception;
    PrescribedResult provideResult(ProcessNode parent, int n) throws Exception;
    PrescribedResult provideOptionalResult(ProcessNode parent, int n) throws Exception;
    RelationshipTask provideRelationshipTask(ProcessNode parent, int n) throws Exception;
    /* chance for source to do anything else it needs to do */
    void finishImport(ProcessNode process);
  }
  
  /**
   * Interface for exporting a process node to another representation, such
   * as a text file
   */
  public interface ExportTarget extends TravelerElement.ExportTarget {
    void acceptId(String id);
    void acceptName(String name);
    void acceptHardwareGroup(String hardwareGroup);
    void acceptIsCloned(boolean isCloned);
    void acceptIsRef(boolean isRef);
    void acceptVersion(String version);
    void acceptJobname(String jobname);
    void acceptUserVersionString(String userVersionString);
    void acceptDescription(String description);
    void acceptShortDescription(String desc);
    void acceptInstructionsURL(String instructionsURL);
    void acceptMaxIteration(String maxIterations);
    void acceptNewLocation(String newLoc, String site);
    void acceptNewStatus(String newStat, String labelGroup);
    void acceptSubsteps(String substeps);
    void acceptTravelerActionMask(int travelerActionMask);
    void acceptPermissionGroups(ArrayList<String> groups);
    void acceptTravelerTypeLabels(ArrayList<String> labels);
    void acceptOriginalId(String originalId);
    void acceptChildren(ArrayList<ProcessNode> children);
    void acceptPrerequisites(ArrayList<Prerequisite> prerequisites);
    void acceptPrescribedResults(ArrayList<PrescribedResult> prescribedResults);
    void acceptOptionalResults(ArrayList<PrescribedResult> optionalResults);
    void acceptRelationshipTasks(ArrayList<RelationshipTask> tasks);
    // Following is to transmit condition assoc. with parent edge
    void acceptCondition(String condition); 
    void acceptHardwareCondition(String condition); 
    void acceptClonedFrom(ProcessNode process);
    void acceptHasClones(boolean hasClones);
    
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
  @Override
  public void exportTo(TravelerElement.ExportTarget target) {
    if (target instanceof ProcessNode.ExportTarget) {
      ProcessNode.ExportTarget ptarget = (ProcessNode.ExportTarget) target;  
  
      ptarget.acceptId(m_processId);
      if (m_parentEdge != null) {
        ptarget.acceptCondition(m_parentEdge.getCondition());
        ptarget.acceptHardwareCondition(m_parentEdge.getHardwareCondition());
      }
      ptarget.acceptIsCloned(m_isCloned);
      ptarget.acceptName(m_name);
      /* Some targets, in particular yaml, need to know early on if the
       * node is a clone
       */
  
      ptarget.acceptHardwareGroup(m_hardwareGroup);
      /* Make sure target gets action mask early since it can affect
         interpretation of some other fields */
      ptarget.acceptTravelerActionMask(m_travelerActionMask);

      ptarget.acceptVersion(m_version);
      ptarget.acceptJobname(m_jobname);
      ptarget.acceptUserVersionString(m_userVersionString);
      ptarget.acceptShortDescription(m_shortDescription);
      ptarget.acceptDescription(m_description);
      ptarget.acceptInstructionsURL(m_instructionsURL);
      ptarget.acceptMaxIteration(m_maxIteration);

      ptarget.acceptNewLocation(m_newLocation, m_locationSite);

      ptarget.acceptNewStatus(m_newStatus, m_labelGroup);
      ptarget.acceptOriginalId(m_originalId);
      ptarget.acceptSubsteps(m_substeps);
      ptarget.acceptPermissionGroups(m_permissionGroups);
      ptarget.acceptTravelerTypeLabels(m_travelerTypeLabels);
      ptarget.acceptPrerequisites(m_prerequisites);
      ptarget.acceptPrescribedResults(m_resultNodes);
      ptarget.acceptOptionalResults(m_optionalResultNodes);
      ptarget.acceptRelationshipTasks(m_relationshipTasks);
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

  void acceptHardwareCondition(String condition) {
    if (m_parentEdge != null) {
      m_parentEdge.setHardwareCondition(condition);
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
    // some things can't have changed; name, userVersionString,
    // hardwareGorup, hardwareRelationshipType, isOption, travelerActionMask,
    // Don't bother copying those.
  
    m_isRef = src.m_isRef;
    m_edited = false;   // FIX ME could cause trouble if child was modified 
    m_processId = src.m_processId;
    m_version = src.m_version;
    m_originalId = src.m_originalId;
    m_description = src.m_description;
    m_shortDescription = src.m_shortDescription;
    m_instructionsURL = src.m_instructionsURL;
    m_maxIteration = src.m_maxIteration;
    m_userVersionString = src.m_userVersionString;
    m_jobname = src.m_jobname;
    m_newLocation = src.m_newLocation;
    m_locationSite = src.m_locationSite;
    m_newStatus = src.m_newStatus;
    m_labelGroup = src.m_labelGroup;
    // For now, do not handle recurs==true, so leave option count and seq count
    // alone
    if (src.getPrerequisiteCount() > 0) {
      m_prerequisites = new ArrayList< >(src.getPrerequisiteCount());
      for (Prerequisite srcPre: src.m_prerequisites) {
        m_prerequisites.add(new Prerequisite(this, srcPre));
      }
    } else m_prerequisites = null;
    if (src.getResultCount() > 0) {
      m_resultNodes = new ArrayList< >(src.getResultCount());
      for (PrescribedResult srcRes: src.m_resultNodes) {
        m_resultNodes.add(new PrescribedResult(this, srcRes));
      }
    }
    if (src.getOptionalResultCount() > 0) {
      m_optionalResultNodes = new ArrayList< >(src.getOptionalResultCount());
      for (PrescribedResult srcRes: src.m_optionalResultNodes) {
        m_optionalResultNodes.add(new PrescribedResult(this, srcRes));
      }
    }
    if (src.getRelationshipTaskCount() > 0) {
      m_relationshipTasks = new ArrayList< >(src.getRelationshipTaskCount());
      for (RelationshipTask srcTask: src.m_relationshipTasks) {
        m_relationshipTasks.add(new RelationshipTask(this, srcTask));
      }
    }
    
  }
  
  public ProcessNode getParent() {return m_parent;}
  public ProcessEdge getParentEdge() { return m_parentEdge;}
  public String getName() { return m_name;}
  public String getVersion() {return m_version;}
  public String getUserVersionString() {return m_userVersionString;}
  public String getJobname() {return m_jobname;}
  public String getProcessId() {return m_processId;}
  public String getHardwareGroup() {return m_hardwareGroup;}
  public String getDescription() {return m_description;}
  public String getShortDescription() {return m_shortDescription;}
  public String getInstructionsURL() { return m_instructionsURL;}
  public String getMaxIteration() {return m_maxIteration;}
  public String getNewLocation() {return m_newLocation;}
  public String getLocationSite() {return m_locationSite;}
  public String getNewStatus() {return m_newStatus;}
  public String getNewStatusId() {return m_newStatusId;}
  public String getLabelGroup() {return m_labelGroup;}
  public String getCondition() {
    if (m_parentEdge == null) return null;
    return m_parentEdge.getCondition();
  }
  public String getHardwareCondition() {
    if (m_parentEdge == null) return null;
    return m_parentEdge.getHardwareCondition();
  }
  public int getPrerequisiteCount() { 
    if (m_prerequisites == null) return 0;
    return m_prerequisites.size(); }
  public int getResultCount() { 
    if (m_resultNodes == null) return 0;
    return m_resultNodes.size(); }
  public int getOptionalResultCount() {
    if (m_optionalResultNodes  == null) return 0;
    return m_optionalResultNodes.size();
  }
  public int getRelationshipTaskCount() {
    if (m_relationshipTasks == null) return 0;
    return m_relationshipTasks.size();
  }
  public boolean getIsEdited() {return m_edited;}
  public String getSourceDb() {return m_sourceDb;}
  public boolean isRef() {return m_isRef; }
  public void setProcessId(String id) {m_processId = id;}
  public void setOriginalId(String id) {m_originalId = id;}
  public void setDescription(String description) {m_description = description;}
  public void setShortDescription(String desc) 
  {m_shortDescription = desc;}
  public void setVersion(String version)  {m_version = version;}
  public void setJobname(String jobname)  {m_jobname = jobname;}
  public void setUserVersionString(String ustring) {m_userVersionString = ustring;}
  public void setInstructionsURL(String url) {m_instructionsURL = url;}
  public void setMaxIteration(String maxIt) {m_maxIteration = maxIt;}
  public void setNewLocation(String newLoc) {m_newLocation = newLoc;}
  public void setLocationSite(String site) {m_locationSite = site;}
  public void setNewStatus(String newStat) {m_newStatus = newStat;}
  public void setLabelGroup(String group) {m_labelGroup = group;}
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
  public ArrayList<PrescribedResult> getOptionalResults() {
    return m_optionalResultNodes;
  }
  public ArrayList<RelationshipTask> getRelationshipTasks() {
    return m_relationshipTasks;
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
  private ArrayList<PrescribedResult> m_optionalResultNodes=null;
  private ArrayList<RelationshipTask> m_relationshipTasks=null;
  private ArrayList<String> m_permissionGroups=null;
  private ArrayList<String> m_travelerTypeLabels=null;
  private String m_name=null;
  private boolean m_isCloned=false;
  private boolean m_hasClones=false;
  private boolean m_isRef=false;
  private String m_hardwareGroup=null;
  private String m_processId=null;
  private String m_version=null;
  private String m_jobname=null;
  private String m_userVersionString="";
  private String m_description=null;
  private String m_shortDescription=null;
  private String m_instructionsURL= "";
  private String m_maxIteration=null;
  private String m_newLocation=null;
  private String m_locationSite=null;
  private String m_newStatus=null;
  private String m_labelGroup=null;
  private String m_newStatusId=null; /* Make accessible for export to YAML */
  private String m_substeps=null;
  private String m_sourceDb=null;
  private String m_substepType="SEQUENCE"; // comes from parent
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
