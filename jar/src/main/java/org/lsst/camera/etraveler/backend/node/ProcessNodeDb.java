/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;
import org.lsst.camera.etraveler.backend.exceptions.DbContentException;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Mediates between internal (ProcessNode) representation and DB rep.
 * One of the init methods must have been called successfully before
 * any of the provide... methods may be called.
 * @author jrb
 */
public class ProcessNodeDb implements ProcessNode.Importer, ProcessNode.ExportTarget {
  static String formProcessKey(String name, String version) {
    return name + "_" + version;
  }
  static String formProcessKey(String name, int version) {
    return name + "_" + version;
  }

  // Constructor when means is ProcessNode visitor
  public ProcessNodeDb(DbConnection connect, TravelerToDbVisitor vis,
                       ProcessNodeDb parent, int edgeStep) {
    m_connect = connect;
    m_vis = vis;
    m_dbParent = parent;
    m_edgeStep = edgeStep;
    if (parent != null) {
      m_travelerRoot = parent.m_travelerRoot;
      m_sourceDb = parent.m_sourceDb;
    }    else  {
      m_travelerRoot = this;
      m_sourceDb = connect.getSourceDb();
    }
  }

  // Remaining constructors are for case when we're being constructed from db
  public ProcessNodeDb(DbConnection connect, String id, String parentEdgeId,
      ProcessNodeDb travelerRoot) 
      throws SQLException {
   m_connect = connect;
   m_parentEdgeId = parentEdgeId;
   init(id, travelerRoot);
  }
  public ProcessNodeDb(DbConnection connect, String processName, 
      String userVersion, String hgroup, String parentEdgeId, 
      ProcessNodeDb travelerRoot) throws SQLException {
    m_connect = connect;
    m_parentEdgeId = parentEdgeId;
    init(processName, userVersion, hgroup, travelerRoot);
  }
  public ProcessNodeDb(DbConnection connect, String processName, int version,
      String hgroup, String parentEdgeId, ProcessNodeDb travelerRoot) 
      throws SQLException {
    m_connect = connect;
    m_parentEdgeId = parentEdgeId;
    init(processName, version, hgroup, travelerRoot);
  }
  private static String[] s_initCols = {"name", 
    "hardwareGroupId",  "version", 
    "userVersionString", "description", "shortDescription", "instructionsURL", "substeps", 
    "maxIteration", "travelerActionMask", "permissionMask", "originalId", "newLocation",
    "newHardwareStatusId"};
  private static String[] s_edgeCols = {"step", "cond"};
  private static PreparedStatement s_processQuery = null;
  private static PreparedStatement s_edgeQuery = null;
  private static PreparedStatement s_prereqQuery = null;
  private static PreparedStatement s_prescribedResultsQuery = null;
  private static PreparedStatement s_childQuery = null;
  private static PreparedStatement s_edgeInfoQuery = null;
  private static PreparedStatement s_relationshipTaskQuery = null;
  /**
   * Query db and save all information of interest for process specified by
   * input parameters
   * @param processName   "name" field in db
   * @param userVersion   "userVersionString"  field in db
   * @param hgroup         name for hardware group
   * @param travelerRoot
   * @return 
   */
  private void init(String processName, String userVersion, String hgroup, 
      ProcessNodeDb travelerRoot) throws SQLException {
    // fetch id, then call the other init
    String where = " WHERE Process.name=" + processName + " and userVersionString=" 
            + userVersion + " and HardwareGroup.name='" + hgroup 
        + "' and Process.hardwareGroupId=HardwareGroup.id";
    String id = m_connect.fetchColumn("Process join HardwareGroup", "Process.id", 
        where);
    if (id == null)  {
      SQLException ex = new SQLException("Fetch column failure");
      throw ex;
    }
    init(id, travelerRoot);
  }
  /**
   * Query db and save all information of interest for process specified by
   * input parameters
   * @param processName      "name" field in db
   * @param version          "version" field in db
   * @return 
   */
  private void init(String processName, int version, String hgroup,
      ProcessNodeDb travelerRoot) throws SQLException {
    // fetch id, then call the other init
    String where = " WHERE Process.name='" + processName + "' and version=" 
            + version  + " and HardwareGroup.name='" + hgroup 
        + "' and Process.hardwareGroupId=HardwareGroup.id";
    String id = m_connect.fetchColumn("Process join HardwareGroup", "Process.id", where);
    if (id == null) { 
      SQLException ex = new SQLException("fetch column failure");
      throw ex;
    }
    init(id, travelerRoot);
  }
  /**
   * Query db and save all information of interest for process with id = param.
   * @param processId
   * @return 
   */
  private void  init(String processId, ProcessNodeDb travelerRoot) throws SQLException {
    // fetch all information belonging to this Process;
    // get id - name correspondences
    m_id = processId;
    if (travelerRoot == null) {
      initIdMaps();
      m_travelerRoot = this;
      m_nodeMap = new ConcurrentHashMap<String, ProcessNodeDb>();
      m_nodeMap.put(m_id, this);
    } else {
      m_travelerRoot = travelerRoot;
      copyIdMaps();
    }
    if (s_processQuery == null)  {
      initQueries();
    }
    ResultSet rs;
   
    if (m_parentEdgeId != null) {  // needed even for clones
      try {
        s_edgeInfoQuery.setString(1, m_parentEdgeId);
        rs = s_edgeInfoQuery.executeQuery();
        rs.next();
        m_edgeStep = rs.getInt(1);
        m_edgeCondition = rs.getString(2);
      } catch (SQLException ex) {
        System.out.println("Query for process " + m_id + " failed with exception");
        System.out.println(ex.getMessage());
        throw ex;
      }
    }
    if (travelerRoot != null)  {
      if (travelerRoot.m_nodeMap.containsKey(m_id))  {
        m_isCloned = true;
        m_name = travelerRoot.m_nodeMap.get(m_id).m_name;
        m_version = travelerRoot.m_nodeMap.get(m_id).m_version;
        m_sourceDb = m_connect.getSourceDb();
        return;
      }  else {
        travelerRoot.m_nodeMap.put(m_id, this);
      }
    }

    try {
      s_processQuery.setString(1, m_id);
      rs = s_processQuery.executeQuery();
      rs.next();
      int ix = 0;
      m_name = rs.getString(++ix);
      m_hardwareGroupId = rs.getString(++ix);
      m_hardwareGroup = m_hardwareGroupNameMap.get(m_hardwareGroupId);
      m_version = rs.getString(++ix);
      m_userVersionString = rs.getString(++ix);
      if (m_userVersionString  == null) m_userVersionString = "";
      m_description = rs.getString(++ix);
      m_shortDescription = rs.getString(++ix);
      m_instructionsURL = rs.getString(++ix);
      m_substeps = rs.getString(++ix);
      m_maxIteration = rs.getString(++ix);
      m_travelerActionMask = rs.getInt(++ix);
      m_permissionGroupMask = rs.getInt(++ix);
      m_originalId = rs.getString(++ix);
      m_newLocation = rs.getString(++ix);
      m_newStatusId = rs.getString(++ix);
      rs.close();
      decodePermissionGroupMask();
      if (m_newStatusId != null)  { /* find corresponding string */
        m_newStatus = m_connect.fetchColumn("HardwareStatus", "name",
            " where id ='" + m_newStatusId + "'");
      }
    
      if (!m_substeps.equals("NONE")) { // can't use default m_nChildren=0 
        s_edgeQuery.setString(1, m_id);
        rs = s_edgeQuery.executeQuery();          
        rs.next();
        m_nChildren = rs.getInt(1);
        rs.close();
        m_childIds = new String[m_nChildren];
        m_childEdgeIds = new String[m_nChildren];
        s_childQuery.setFetchSize(m_nChildren);
        s_childQuery.setString(1, m_id);
        rs = s_childQuery.executeQuery();
        for (int i=0; i < m_nChildren; i++)  {
          rs.next();
          m_childIds[i] = rs.getString(1);
          m_childEdgeIds[i] = rs.getString(2);
        }
      }
      /* see if there are prerequisites associated with this step */    
      m_prerequisiteIds = getAssociateIds(s_prereqQuery);
      if (m_prerequisiteIds != null) 
        m_nPrerequisites = m_prerequisiteIds.length;
      /* see if there are relationship tasks associated with this step */
      m_relationshipTaskIds = getAssociateIds(s_relationshipTaskQuery);
      if (m_relationshipTaskIds != null)
        m_nRelationshipTasks = m_relationshipTaskIds.length;
  
      rs.close();
      
      /*
       * Do one query for required results, another for optional
       */
      s_prescribedResultsQuery.setString(1, m_id);
      // First get "regular" (required) results
      s_prescribedResultsQuery.setString(2, "0");
      rs = s_prescribedResultsQuery.executeQuery();
      boolean more = rs.next();
      while (more) {
        m_nPrescribedResults++;
        more = rs.next();
      }
      if (m_nPrescribedResults > 0) {
        m_resultIds = new String[m_nPrescribedResults];
        rs.beforeFirst();
        for (int i=0; i < m_nPrescribedResults; i++) {
          rs.next();
          m_resultIds[i] = rs.getString(1);
        }
      }
      rs.close();
      // Now do the same thing for optional results
      s_prescribedResultsQuery.setString(1, m_id);
      s_prescribedResultsQuery.setString(2, "1");
      rs = s_prescribedResultsQuery.executeQuery();
      more =rs.next();
      while (more) {
        m_nOptionalResults++;
        more = rs.next();
      }
      if (m_nOptionalResults > 0) {
        m_optionalResultIds = new String[m_nOptionalResults];
        rs.beforeFirst();
        for (int i=0; i < m_nOptionalResults; i++) {
          rs.next();
          m_optionalResultIds[i] = rs.getString(1);
        }
      }
      rs.close();
      //
      if (m_travelerRoot == this) {
      /*
       * Check for associated ExceptionType entries.  If found, invoke
       * constructor for each
       */
        String where = " where rootProcessId='" + m_id + "'";
        ArrayList<String> exceptIds = 
            m_connect.fetchColumnMulti("ExceptionType", "id", where);
        if (exceptIds != null)  {
          m_ncrSpecsDb = new ArrayList<NCRSpecificationDb>();
          for (String id: exceptIds)  {
            m_ncrSpecsDb.add(new NCRSpecificationDb(m_connect, id));
          }
        }
      }
      m_sourceDb = m_connect.getSourceDb();
    } catch (SQLException ex) {
      System.out.println("Query for process " + m_id + " failed with exception");
      System.out.println(ex.getMessage());
      throw ex;
    }    catch (EtravelerException ex) {
      System.out.println(ex.getMessage());
    }
  }
  private void initQueries() throws SQLException {
    String where = " WHERE id=?";
    s_processQuery = m_connect.prepareQuery("Process", s_initCols, where);
    s_edgeInfoQuery = m_connect.prepareQuery("ProcessEdge", s_edgeCols, where);
    
    
    where = " WHERE parent=?";    
    String[] getCol = {"COUNT(*)"};
    s_edgeQuery = m_connect.prepareQuery("ProcessEdge", getCol, where);
    String[] childCol = {"child", "id"};
    where = " WHERE parent=? order by abs(step)";    
    s_childQuery = m_connect.prepareQuery("ProcessEdge", childCol, where);  
    
    where = " WHERE processId=? order by id"; 
    getCol[0] = "id";
    s_prereqQuery = m_connect.prepareQuery("PrerequisitePattern", getCol, where);
    
    s_relationshipTaskQuery = m_connect.prepareQuery("ProcessRelationshipTag", 
        getCol, where);
    
    /*
     * For prescribed results distinguish based on setting of isOptional
     */
    where = " WHERE processId=? and isOptional=? order by id";
    s_prescribedResultsQuery = m_connect.prepareQuery("InputPattern", getCol, where);
    
    if ((s_processQuery == null) || (s_edgeQuery == null) || 
        (s_prereqQuery == null) || (s_prescribedResultsQuery == null) ||        
        (s_edgeInfoQuery == null) || s_relationshipTaskQuery == null) {
      throw  new SQLException("DbConnection.prepareQuery failure");
    }
  }
  /*
   * Called by constructor when we're forming an object from db. 
   */
  private void decodePermissionGroupMask() {
    if (m_permissionGroupMask == 0) return;
    m_permissionGroups = new ArrayList<String>();
    int remaining = m_permissionGroupMask;
    int bit = 1;
    while ((remaining != 0) && (bit < Integer.MAX_VALUE))  {
      if ((bit & remaining) != 0) {
        if (m_permissionGroupMap.contains(Integer.toString(bit))) {
          m_permissionGroups.add(m_permissionGroupMap.get(Integer.toString(bit)));
        }
        remaining -= bit;
      }
      bit *= 2;
    }
  }
  /*
   * Called by verify when writing to Db.  Here we must check that
   * user-supplied strings really do correspond to something in the
   * PermissionGroup table (values already stored in one of our maps)
   */
  private void formPermissionGroupMask() throws EtravelerException {
    if (m_permissionGroups == null)  return;
    m_permissionGroupMask = 0;
    for (String g : m_permissionGroups)  {
      if (!m_permissionGroupMap.contains(g)) {
        throw new EtravelerException("No such permission group as '" + g + "'");
      }
      String maskBit = m_permissionGroupMap.get(g);
      m_permissionGroupMask += Integer.parseInt(maskBit);
    }
  }
  String [] getAssociateIds(PreparedStatement query) throws SQLException {
    query.setString(1, m_id);
    ResultSet rs = query.executeQuery();
    boolean more = rs.next();
    int count = 0;
    while (more) {
      count++;
        more = rs.next();
    }
    if (count == 0) {
      rs.close();
      return null;
    }
    
    String [] retArray = new String[count];
    rs.beforeFirst();
    for (int i=0; i < count; i++) {
      rs.next();
      retArray[i] = rs.getString(1);
    }
    rs.close();
    return retArray;  
  }
  // ProcessNode.Importer interface implementation: support import into ProcessNode
  public String provideId() {return m_id;}
  public String provideName()  {return m_name;}
  public String provideHardwareGroup() {return m_hardwareGroup;}
  /*
  public String provideHardwareRelationshipType()  {
    return m_hardwareRelationshipType; }
  public String provideHardwareRelationshipSlot()  {
    return m_hardwareRelationshipSlot; }
    */
  public String provideVersion() {return m_version;}
  public String provideUserVersionString() {return m_userVersionString;}
  public String provideDescription() {
    if (m_description == null) return "";
    return m_description;
  }
  public String provideShortDescription() {
    if (m_shortDescription == null) return "";
    return m_shortDescription;
  }
   public String provideInstructionsURL() {
    if (m_instructionsURL == null) return "";
    return m_instructionsURL;
  }
  public String provideMaxIteration() {return m_maxIteration;}
  public String provideNewLocation() {return m_newLocation;}
  public String provideNewStatus() {return m_newStatus;}
  public String provideSubsteps() {return m_substeps;}
  public int provideTravelerActionMask() {return m_travelerActionMask;}
  public String provideOriginalId() {return m_originalId;}
  public ArrayList<String> providePermissionGroups() {return m_permissionGroups;}
  public int provideNChildren() {return m_nChildren;}
  public int provideNPrerequisites() {return m_nPrerequisites;}
  public int provideNPrescribedResults() {return m_nPrescribedResults;}
  public int provideNOptionalResults() {return m_nOptionalResults;}
  public int provideNRelationshipTasks() { return m_nRelationshipTasks;}
  public int provideEdgeStep() {return m_edgeStep;}
  public String provideEdgeCondition() {return m_edgeCondition;}
  //public String provideParentEdgeId() {return m_parentEdgeId;}
  public ProcessEdge provideParentEdge(ProcessNode parent, ProcessNode child) {
    ProcessEdge parentEdge = new ProcessEdge(parent, child, m_edgeStep, m_edgeCondition);
    parentEdge.setId(m_parentEdgeId);
    return parentEdge;
  }
  public ProcessNode provideChild(ProcessNode parent, int n) throws Exception {
    return new ProcessNode(parent, new ProcessNodeDb(m_connect, m_childIds[n],
        m_childEdgeIds[n], m_travelerRoot));
  }
  public Prerequisite providePrerequisite(ProcessNode parent, int n) throws Exception {
    return new
      Prerequisite(parent, new 
                   PrerequisiteDb(m_connect, m_prerequisiteIds[n],
                                  m_prerequisiteTypeMap, 
                                  m_hardwareTypeNameMap));
  }
  public PrescribedResult provideResult(ProcessNode parent, int n) throws Exception {
    return new PrescribedResult(parent, new PrescribedResultDb(m_connect, m_resultIds[n]));
  }
  public PrescribedResult provideOptionalResult(ProcessNode parent, int n) throws Exception {
    return new PrescribedResult(parent, 
        new PrescribedResultDb(m_connect, m_optionalResultIds[n]));
  }
  public RelationshipTask provideRelationshipTask(ProcessNode parent, int n) throws Exception {
    return new RelationshipTask(parent,
        new RelationshipTaskDb(m_connect, m_relationshipTaskIds[n]));
  }
  public boolean provideIsCloned() {return m_isCloned;}
  public boolean provideHasClones() {return m_isCloned;}
  public boolean provideIsRef() { return m_isRef;}  // does this make any sense?
  public String provideSourceDb() { return m_sourceDb;}

  public void finishImport(ProcessNode process) {
    process.setProcessId(m_id);
    process.setOriginalId(m_originalId);
  }   
  
  // ProcessNode.ExportTarget implementation: allow ProcessNode to export to us
  public void acceptId(String id) {   m_id = id; }
  public void acceptName(String name) {  m_name = name; }
  public void acceptHardwareGroup(String hardwareGroup) {
    m_hardwareGroup=hardwareGroup;
  }
  
  public void acceptVersion(String version) { m_version = version; }
  public void acceptUserVersionString(String userVersionString) {
    m_userVersionString = userVersionString;
  }
  public void acceptDescription(String description) {m_description=description;}
  public void acceptShortDescription(String desc) {m_shortDescription=desc;}
  public void acceptInstructionsURL(String url) {m_instructionsURL = url;}
  public void acceptMaxIteration(String maxIteration) {m_maxIteration=maxIteration;}
  public void acceptNewLocation(String newLoc) {
    m_newLocation=newLoc;
    if (newLoc == null) return;
    /* Canonical non-null representation for operator prompt is "(?)" */
    if (newLoc.equals("(?)")) m_newLocation = null;
  }
  public void acceptNewStatus(String newStat) {
    m_newStatus=newStat;
    if (newStat == null) return;
    /* Canonical non-null representation for operator prompt is "(?)" */
    if (newStat.equals("(?)")) m_newStatus = null;
  }
  public void acceptSubsteps(String substeps) {m_substeps = substeps; }
  public void acceptTravelerActionMask(int travelerActionMask) {
    m_travelerActionMask = travelerActionMask;
  }
  public  void acceptOriginalId(String originalId) {m_originalId = originalId;}
  public  void acceptPermissionGroups(ArrayList<String> groups) {
    if (groups == null) return;
    m_permissionGroups = new ArrayList<String>(groups.size());
    for (String g : groups) {
      m_permissionGroups.add(new String(g));
    }
  }
  public void acceptChildren(ArrayList<ProcessNode> children) {
    if (m_isCloned || m_isRef)  {
      m_children = null;
    } else {
      m_children = children;
    }
    if (m_children == null) {
      return;
    }
    // Do recursion here
    if (children.size() > 0) {
      m_childrenDb = new ProcessNodeDb[children.size()];
      for (int i = 0; i < children.size(); i++) {
        int edgeStep = i +1;
        if (m_substeps.equals("SELECTION")) { edgeStep = -edgeStep; }
        m_childrenDb[i] = new ProcessNodeDb(m_connect, m_vis, this, edgeStep);
        children.get(i).exportTo(m_childrenDb[i]);
      }
    }
  }
    
  public void acceptPrerequisites(ArrayList<Prerequisite> prerequisites) {    
    if (m_isCloned || m_isRef)  {
      m_prerequisites = null;
    } else {
      m_prerequisites = prerequisites;
    }

    if (m_prerequisites == null)  {
      return;
    }
    // Then maybe...
    int allocLen = m_prerequisites.size();
   
    if (prerequisites.size() > 0) {
      m_prerequisitesDb = 
        new PrerequisiteDb[allocLen];
      for (int ip = 0; ip < prerequisites.size(); ip++) {
        m_prerequisitesDb[ip] = new PrerequisiteDb(m_connect);
        prerequisites.get(ip).exportTo(m_prerequisitesDb[ip]);
      }
    }
  }
 
  public void acceptRelationshipTasks(ArrayList<RelationshipTask> rels)  {
    if (m_isCloned || m_isRef) {
      m_relationshipTasks = null;
    } else {
      m_relationshipTasks = rels;
    }
    if (rels == null) return;
    if (rels.size() > 0) {
      m_relationshipTasksDb = new RelationshipTaskDb[rels.size()];
      for (int irt = 0; irt < rels.size(); irt++) {
        m_relationshipTasksDb[irt] = new RelationshipTaskDb(m_connect);
        rels.get(irt).exportTo(m_relationshipTasksDb[irt]);
      }
    }
  } 
  public void acceptPrescribedResults(ArrayList<PrescribedResult> prescribedResults) {
    if (m_isCloned || m_isRef)  {
      m_results = null;
    } else {
      m_results = prescribedResults;
    }
    if (m_results == null)  {
      return;
    }
    if (prescribedResults.size() > 0) {
      m_resultsDb = new PrescribedResultDb[prescribedResults.size()];
      for (int ir = 0; ir < prescribedResults.size(); ir++) {
        m_resultsDb[ir] = new PrescribedResultDb(m_connect);
        m_results.get(ir).exportTo(m_resultsDb[ir]);
      }
    }
  }
  public void acceptOptionalResults(ArrayList<PrescribedResult> prescribedResults) {
    if (m_isCloned || m_isRef)  {
      m_optionalResults = null;
    } else {
      m_optionalResults = prescribedResults;
    }
    if (m_optionalResults == null)  {
      return;
    }
    if (prescribedResults.size() > 0) {
      m_optionalResultsDb = new PrescribedResultDb[prescribedResults.size()];
      for (int ir = 0; ir < prescribedResults.size(); ir++) {
        m_optionalResultsDb[ir] = new PrescribedResultDb(m_connect);
        m_optionalResults.get(ir).exportTo(m_optionalResultsDb[ir]);
      }
    }
  }
  // Following is to transmit condition assoc. with parent edge
  public void acceptCondition(String condition) {
    m_edgeCondition = condition;
  }
  public void acceptClonedFrom(ProcessNode process) {
    if (process != null) m_isCloned = true;
  }
  public void acceptIsCloned(boolean isCloned) {
    m_isCloned = isCloned;
  }
  public void acceptHasClones(boolean hasClones) {
    m_hasClones = hasClones;
  }
  public void acceptIsRef(boolean isRef) {
    m_isRef = isRef;
  }
  public void acceptEdited(boolean edited) { m_edited = edited;}
  public void exportDone() {
    if (m_edited) {m_version = "modified";}
  }
  public boolean isRootNode() {
    return (m_travelerRoot == this);
  }
  public void verify(DbConnection connect, String sub) throws EtravelerException {   
    // For first time through (parentless node)  maybe look up some things,
    // such as all possible relationship types, prereq types and semantic types.
    //  Save and pass on through when calling verify on children, prereqs, etc.
    if (m_dbParent == null)  {    
      try {
        initIdMaps();
      }  catch (Exception ex) {
        throw new
          EtravelerException("ProcessNodeDb.verify: Error initializing maps :"
                                     + ex.getMessage());
      }
    }  else {  copyIdMaps();  }
    
    // Verify  m_hardwareGroup against db for top node.  
    // For children just check it matches parent
    if (m_dbParent == null) {
      if (m_hardwareGroup == null) {
        throw new EtravelerException("No hardware group specified");
      } else  {   // hardware group has been specified
        if (m_hardwareGroupNameMap.containsKey(m_hardwareGroup)) {
          m_hardwareGroupId = m_hardwareGroupNameMap.get(m_hardwareGroup);
        } else {
          throw new EtravelerException("No hardware group entry for " + m_hardwareGroup);
        }
      }
      if (sub == null) sub = "Default";
      m_subsystemId = connect.fetchColumn("Subsystem", "id", " where shortName='" + sub + "'");
      if (m_subsystemId == null) {
        throw new EtravelerException("Unknown subsystem:  " + sub);
      }        
    }   else { // assuming we checked compatibility of child earlier
      m_hardwareGroupId = m_dbParent.m_hardwareGroupId;
    }
    if (m_newStatus != null) {
      if ((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_STATUS) != 0) {
        if (m_hardwareStatusIdMap.containsKey(m_newStatus)) {
          m_newStatusId = m_hardwareStatusIdMap.get(m_newStatus);
        }  else {
          throw new EtravelerException("No hardware status entry for " 
              + m_newStatus);
        }
      } else if ((m_travelerActionMask & 
          (TravelerActionBits.ADD_LABEL + TravelerActionBits.REMOVE_LABEL)) != 0) {
        if (m_hardwareLabelIdMap.containsKey(m_newStatus)) {
          m_newStatusId = m_hardwareLabelIdMap.get(m_newStatus);
        } else {
          throw new EtravelerException("No hardware label entry for " + m_newStatus);
        }
      }
    }
    formPermissionGroupMask();
    // If ref, verify we have the right db and that the node we need really is.
    // there. Also fetch travelerActionMask
    if (m_isRef)   {
      if (!m_sourceDb.equals(connect.getSourceDb() ) ){
        throw new EtravelerException("Process definition refers to wrong db");
      }
      String searchVersion = m_version;
      // In case "last" has been specified for ref, we only insist that there
      // be at least a version 1
      if (searchVersion.equals("last")) searchVersion = "1";
      String where = " where name='" + m_name + "' and version='" 
          + searchVersion + "' and hardwareGroupId='" + m_hardwareGroupId + "'";
      m_id = m_connect.fetchColumn("Process", "id", where);
      if (m_id == null) {
        throw new EtravelerException("Process " + m_name + ", version " 
            + m_version + ", hardware group " + m_hardwareGroupId 
            + "does not exist for dbType " + m_sourceDb);
      } 
      String actionMaskString = m_connect.fetchColumn("Process", 
          "travelerActionMask", where);
      try {
        m_travelerActionMask = Integer.parseInt(actionMaskString);
      } catch (NumberFormatException ex) {
        throw new EtravelerException("Ref process has bad travelerActionMask");
      }
      if (m_version.equals("last")) {
        m_originalId = m_id;
        m_id = null;
      }
      m_verified = true;
      return;
    }
        
    if (m_prerequisitesDb != null) {
      for (int ip=0; ip < m_prerequisitesDb.length; ip++) {
        /* there are circumstances where there are extra null entries
         * at the end of the array.
         */
        if (m_prerequisitesDb[ip] == null) {
          break;
        }
        m_prerequisitesDb[ip].verify(m_prerequisiteTypeMap, 
                                     m_hardwareTypeNameMap);
      }
    }
    if (m_resultsDb != null) {
      for (int ir=0; ir < m_resultsDb.length; ir++) {
        m_resultsDb[ir].verify(m_semanticsTypeMap);
      }
    }
    if (m_optionalResultsDb != null) {
      for (int ir=0; ir < m_optionalResultsDb.length; ir++) {
        m_optionalResultsDb[ir].verify(m_semanticsTypeMap);
      }
    }
    if (m_relationshipTasksDb != null) {
      for (int rt=0; rt < m_relationshipTasksDb.length; rt++) {
        m_relationshipTasksDb[rt].verify(m_relationshipTypeMap,
            m_relationshipActionMap, m_hardwareGroupId);
      }
    }
    if (!(m_version.equals("1"))) { 
      if (!acceptableVersion(m_version)) {
        throw new 
          EtravelerException("Unacceptable Process version: " + m_version);
      }
      /* Look up id of version 1 */
      String where = " where version=1 and name='" + m_name 
          + "' and hardwareGroupId='" + m_hardwareGroupId +"'";
      m_originalId = m_connect.fetchColumn("Process", "id", where);
      if (m_originalId == null)  {
        /* If "next" then need not have been prior version with this name.
           Other accepted values - numeric or "modified" - require
           existence of a version 1.
         */
        if (m_version.equals("next")) m_version = "1";
        else {
            throw new 
              EtravelerException("No version 1 for Process name " + m_name);
          }
      }
    }
    // Should check if REMOVELABEL bit is set that thing to be removed is
    // a label and not a regular status     !!!!
    if (m_childrenDb != null) {
      for (int ic=0; ic < m_childrenDb.length; ic++) {
        m_childrenDb[ic].verify(connect, null);
      
        if ((m_travelerActionMask & TravelerActionBits.AUTOMATABLE) != 0) {
          if ((m_childrenDb[ic].m_travelerActionMask & 
              (TravelerActionBits.AUTOMATABLE + TravelerActionBits.HARNESSED)) == 0) {
            throw new EtravelerException("Step " + m_name + " not automatable ");
          }
        }
      }
    }
    m_verified = true;
  }
  private boolean acceptableVersion(String v) {
    if (v.equals("next") || v.equals("modified") ) return true;
    if (v.equals("last") && m_isRef) return true;
    try {
      int iv = Integer.parseInt(v);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }
  // NOTE:  Can use keyword DEFAULT for columns not always set to user-supplied,
  //         value, e.g. hardwareRelationshipType
  private static   String[] s_insertProcessCols={"name", 
    "hardwareGroupId", "version", "userVersionString", "description", "shortDescription",
    "instructionsURL", "substeps", "maxIteration", "newLocation", "newHardwareStatusId", "originalId",
    "travelerActionMask", "permissionMask", "createdBy"};
  private static   String[] s_insertEdgeCols={"parent", "child", "step", "cond", "createdBy"};
 
  
  public void writeToDb(DbConnection connect, ProcessNodeDb parent) 
    throws    SQLException, EtravelerException {
    if (!m_verified) {
      throw new EtravelerException("Unverified ProcessNodeDb cannot be written");
    }
    if (m_isRef) {
      if (m_version.equals("last")) {
        m_version = findLastVersion(m_originalId);
        String where = " where version='" + m_version + "' and originalId='" 
            + m_originalId + "'";
        m_id = m_connect.fetchColumn("Process", "id", where);
      }
      insertEdge();
      return;
    }
    if (!m_isCloned) {
      // Make our row in Process
      String[] vals = new String[s_insertProcessCols.length];
      int ix=0;
      vals[ix] = m_name;
    
      vals[++ix] = m_hardwareGroupId;
      if (m_version.equals("next") || m_version.equals("modified")) {
        m_version = nextAvailableVersion(m_originalId);
      }
      vals[++ix] = m_version;
      if ((m_userVersionString != null) && m_userVersionString.isEmpty()) {
        vals[++ix] = null;
      } else {
        vals[++ix] = m_userVersionString;
      }
      vals[++ix] = m_description;
      vals[++ix] = m_shortDescription;
      vals[++ix] = m_instructionsURL;
      vals[++ix] = m_substeps;
      vals[++ix] = m_maxIteration;
      vals[++ix] = m_newLocation;
      vals[++ix] = m_newStatusId;
      vals[++ix] = m_originalId;
      vals[++ix] = String.valueOf(m_travelerActionMask);
      if (m_permissionGroups != null ) {
        vals[++ix] = String.valueOf(m_permissionGroupMask);
      } else vals[++ix] = null;
      //  Value for user should come from Confluence log-in
      vals[++ix] = m_vis.getUser();
      try {
        m_id = m_connect.doInsert("Process", s_insertProcessCols, vals, "", 
                                  DbConnection.ADD_CREATION_TIMESTAMP);
        if (m_originalId == null) {  // no prev. version. We are the original
          m_originalId = m_id;
          m_connect.updateColumn("Process", "originalId", m_originalId, 
              " where id='" + m_id + "'", 0);
        }
      } catch (SQLException ex)  {
        System.out.println("Failed to create entry for process " + m_name + " with exception");
        System.out.println(ex.getMessage());
        throw new EtravelerException("Failed to create entry for process " 
                                     + m_name + " with SQL exception "
                                     + ex.getMessage());
      }
      String key = formProcessKey(m_name, m_version);
      String oldValue  = m_processNameIdMap.putIfAbsent(key, m_id);
      // Should not be duplicate.  
      // This has already been checked in ProcessNodeYaml

      //  Write out prescribed results rows
      if (m_resultsDb != null) {
        if (m_resultsDb.length > 0) {
          for (int ir = 0; ir < m_resultsDb.length; ir++ ) {
            m_resultsDb[ir].writeToDb(connect, this, m_vis.getUser());
          }
        }
      }
      // And also optional results
      if (m_optionalResultsDb != null) {
        if (m_optionalResultsDb.length > 0) {
          for (int ior = 0; ior < m_optionalResultsDb.length; ior++) {
            m_optionalResultsDb[ior].writeToDb(connect, this, m_vis.getUser());
          }
        }
      }
      
      /* put in something for RelationshipTask here %%%!!!  */
      if (m_relationshipTasksDb != null) { // write them to db
        if (m_relationshipTasksDb.length > 0) {
          for (int irt = 0; irt < m_relationshipTasksDb.length; irt++) {
              m_relationshipTasksDb[irt].writeToDb(connect, this, m_vis.getUser());
          }
        }
      }  
    
      if (m_prerequisitesDb != null) {
        //   Write out prerequisite rows
        if (m_prerequisitesDb.length > 0) {
          for (int ip = 0; ip < m_prerequisitesDb.length; ip++ ) { 
              m_prerequisitesDb[ip].writeToDb(connect, this, m_vis.getUser());         
          }
        }
      }
    }   else {   // need to find our id to make edge
      m_id = m_processNameIdMap.get(formProcessKey(m_name, m_version));
    }
    //  If parent isn't null, make edge between us and it
    insertEdge();
 
    //   Call recursively on children.  
    if (m_childrenDb == null) { return; }
    for (int iChild = 0; iChild < m_childrenDb.length; iChild++ ) {
      m_childrenDb[iChild].writeToDb(connect, this);
    }
  }
  private String nextAvailableVersion(String originalId) 
    throws SQLException, EtravelerException {
   
    String old = findLastVersion(originalId);
    int intNext = Integer.parseInt(old) + 1;
    return Integer.toString(intNext);
  }
  private String findLastVersion(String originalId)
      throws SQLException, EtravelerException {
       String where = " where originalId='" + originalId + 
      "' order by version desc limit 1";
    String last = m_connect.fetchColumn("Process", "version", where);
    return last;
  }
  private void insertEdge() throws SQLException {
    if (m_dbParent != null)  {
      String[] edgeVals = new String[s_insertEdgeCols.length];
      edgeVals[0] = m_dbParent.m_id;
      edgeVals[1] = m_id;
      edgeVals[2] = String.valueOf(m_edgeStep);
      edgeVals[3] = m_edgeCondition;
      edgeVals[4] = m_vis.getUser();
 
      try {
        m_parentEdgeId = m_connect.doInsert("ProcessEdge", s_insertEdgeCols, 
            edgeVals, "", DbConnection.ADD_CREATION_TIMESTAMP);
      } catch (SQLException ex) {
        System.out.println("Failed to create edge leading to process " + m_name + "with exception");
        System.out.println(ex.getMessage());
        throw ex;
      }
    } 
  }
  /**
     Only makes sense to ask for subsystem when node has come from db
     and is the root of a traveler
     If something goes wrong, null is returned
   */
  public String getSubsystem(DbConnection conn) throws SQLException {
    if (m_id == null) return null;
    if (m_dbParent != null) return null;
    String where = " where rootProcessId='" + m_id +"'";
    String tableSpec = "TravelerType TT join Subsystem S on TT.subsystemId=S.id";
    return conn.fetchColumn(tableSpec, "S.shortName", where);
  }
  /*
   * Add new row to TravelerType and TravelerTypeStateHistory tables
   */
   private static   String[] s_insertTravTypeCols={"rootProcessId", "owner", 
     "reason", "subsystemId", "createdBy"};
   private static   String[] s_insertTravTypeHistoryCols={"reason", "createdBy", "travelerTypeId",
    "travelerTypeStateId"};
  public  void registerTraveler(String owner, String reason) 
      throws SQLException, EtravelerException {
    String[] vals = new String[s_insertTravTypeCols.length];
    vals[0] = m_id;
    vals[1] = owner.trim();
    vals[2] = reason.trim();
    vals[3] = m_subsystemId;
    vals[4] = m_vis.getUser();
    String [] valsHist = new String[s_insertTravTypeHistoryCols.length];
    valsHist[0] = "new traveler";
    valsHist[1] = m_vis.getUser();
    valsHist[3] = "1";    /* cheating here.  This is TravelerTypeState value for "new" */
    try {
      String travTypeId = m_connect.doInsert("TravelerType", s_insertTravTypeCols,
          vals, "", DbConnection.ADD_CREATION_TIMESTAMP);
      valsHist[2] = travTypeId;
      String travTypeHistoryId = m_connect.doInsert("TravelerTypeStateHistory",
          s_insertTravTypeHistoryCols, valsHist, "", DbConnection.ADD_CREATION_TIMESTAMP);
    }  catch (SQLException ex) {
        System.out.println("Failed to create TravelerType or TravelerTypeStateHistory entry for " 
            + m_name + "with exception");
        System.out.println(ex.getMessage());
        throw ex;
    }
  }
  private void addComponentPrerequisite(String cmpId) {
    // Check if it's already in original prerequisites
    if (m_prerequisites != null) {
      for (int i = 0; i < m_prerequisites.size(); i++) {
        if (cmpId.equals(m_prerequisitesDb[i].getHardwareTypeId()) ) {
          return;
        }
      }
    }
    int newIx = m_prerequisitesDb.length - 2;
    if (m_prerequisitesDb[newIx] != null) {
      newIx++;
    }
    String cmpName = m_hardwareTypeNameMap.get(cmpId);
    String prereqCmpTypeId = m_prerequisiteTypeMap.get("COMPONENT");
    m_prerequisitesDb[newIx] = new
      PrerequisiteDb(m_connect, cmpName, "COMPONENT", prereqCmpTypeId, null,
                     cmpId, 1);
  }
  /**
   *  Clear out any static data structures dependent on db connection
   */
  public static void reset() {
   s_processQuery = null;
   s_edgeQuery = null;
   s_prereqQuery = null;
   s_prescribedResultsQuery = null;
   s_childQuery = null;
   s_edgeInfoQuery = null;
   PrerequisiteDb.reset();
   PrescribedResultDb.reset();
   RelationshipTaskDb.reset();
  }
  private void initIdMaps() throws SQLException {
    m_relationshipTypeMap = new ConcurrentHashMap<String, String>();
    fillIdMap("MultiRelationshipType", "name", " where TRUE", m_relationshipTypeMap);
    m_relationshipActionMap = new ConcurrentHashMap<String, String>();
    fillIdMap("MultiRelationshipAction", "name", " where TRUE", m_relationshipActionMap);
    m_semanticsTypeMap = new ConcurrentHashMap<String, String>();
    fillIdMap("InputSemantics", "name", " where TRUE", m_semanticsTypeMap);
    m_prerequisiteTypeMap = new ConcurrentHashMap<String, String>();
    fillIdMap("PrerequisiteType", "name", " where TRUE", m_prerequisiteTypeMap);
    m_hardwareTypeNameMap = new ConcurrentHashMap<String, String>();
    fillIdMap("HardwareType", "name", " where TRUE", m_hardwareTypeNameMap);
    m_hardwareGroupNameMap = new ConcurrentHashMap<String, String>();
    fillIdMap("HardwareGroup", "name", " where TRUE",  m_hardwareGroupNameMap);
    m_hardwareStatusIdMap = new ConcurrentHashMap<String, String>();
    fillIdMap("HardwareStatus", "name", " where isStatusValue='1'", m_hardwareStatusIdMap);
    m_hardwareLabelIdMap = new ConcurrentHashMap<String, String>();
    fillIdMap("HardwareStatus", "name", " where isStatusValue='0'", m_hardwareLabelIdMap);
    m_permissionGroupMap = new ConcurrentHashMap<String, String>();
    fill2WayMap("PermissionGroup", "maskBit", "name", " where TRUE", m_permissionGroupMap);
    m_processNameIdMap = new ConcurrentHashMap<String, String>();
  }

  private void copyIdMaps() {
    m_relationshipTypeMap = m_travelerRoot.m_relationshipTypeMap;
    m_relationshipActionMap = m_travelerRoot.m_relationshipActionMap;
    m_semanticsTypeMap = m_travelerRoot.m_semanticsTypeMap;
    m_prerequisiteTypeMap = m_travelerRoot.m_prerequisiteTypeMap;
    m_hardwareTypeNameMap = m_travelerRoot.m_hardwareTypeNameMap;
    m_hardwareGroupNameMap = m_travelerRoot.m_hardwareGroupNameMap;
    m_processNameIdMap = m_travelerRoot.m_processNameIdMap;
    m_hardwareStatusIdMap = m_travelerRoot.m_hardwareStatusIdMap;
    m_hardwareLabelIdMap = m_travelerRoot.m_hardwareLabelIdMap;
    m_permissionGroupMap = m_travelerRoot.m_permissionGroupMap;
  }

  private void fillIdMap(String table, String nameCol, String where,
      ConcurrentHashMap<String, String> map) throws SQLException {
    fill2WayMap(table, "id", nameCol, where, map);
  }
  private void fill2WayMap(String table, String keyCol, String nameCol, String where,
                         ConcurrentHashMap<String, String> map) 
    throws SQLException {
    String[] getCols = { keyCol, nameCol};
    PreparedStatement idNameQuery;
    ResultSet rs;
    try {
      idNameQuery = m_connect.prepareQuery(table, getCols, where);
      rs = idNameQuery.executeQuery();
      boolean more = rs.next();
      while (more) {  
        String oldValue;
        // use name as key
        oldValue = map.putIfAbsent(rs.getString(2), rs.getString(1));
        if (oldValue == null) {
          // and also use id as key 
          // id is always string rep. of an integer; name never will be
          oldValue = map.putIfAbsent(rs.getString(1), rs.getString(2));
        }
        if (oldValue != null) {
          throw new DbContentException("id or " + nameCol + "duplicate in "
              + table); 
        }
        more = rs.next();
      }
    }      catch (SQLException ex) {
      System.out.println("Exception reading table " + table + ": " +
                         ex.getMessage());
      throw ex;
    }
  }  
 
  /* Pick a separator string unlikely to appear in any actual db entry */
  private static final String GLUE = "%&*";
  private static final String GLUE_QUOTED = "\\Q" + GLUE + "\\E";
  private static String formUniqueKey(String val1, String val2)  {
    return (val1 + GLUE + val2);
  }
  private static String[] parseKey(String key) {
    
    return key.split(GLUE_QUOTED);
  }
 
  private String m_id=null;  
  private String m_name=null;
  private String m_hardwareGroup=null;
  private String m_hardwareGroupId = null;
  private String m_version=null;
  private String m_userVersionString="";
  private String m_description=null;
  private String m_shortDescription=null;
  private String m_instructionsURL=null;
  private String m_substeps=null;
  private String m_maxIteration=null;
  private String m_newLocation;
  private String m_newStatus;
  private String m_newStatusId;
  private int m_travelerActionMask=0;
  private int m_permissionGroupMask=0;
  private String m_originalId=null;
  private String m_parentEdgeId = null;
  private int m_edgeStep = 0;
  private String m_edgeCondition = null;
  private int m_nChildren = 0;
  private int m_nPrerequisites = 0;
  private int m_nPrescribedResults = 0;
  private int m_nOptionalResults = 0;
  private int m_nRelationshipTasks = 0;
  private String[] m_childIds;   // save these to make children if asked
  private String[] m_childEdgeIds;
  private String[] m_prerequisiteIds; // save these to make assoc prereqs
  private String[] m_resultIds; // save these to make assoc prescribed results
  private String[] m_optionalResultIds;
  private String[] m_relationshipTaskIds;
  private ArrayList<String>  m_permissionGroups=null;
  private DbConnection m_connect;
  private boolean m_isCloned=false;
  private boolean m_hasClones = false;
  private boolean m_isRef=false;
  private boolean m_edited=false;
  private String m_sourceDb=null;
  private String m_subsystemId = null;  /* only used for root node */
  private ProcessNodeDb m_travelerRoot=null; 
  private ConcurrentHashMap<String, ProcessNodeDb> m_nodeMap=null;
  // For exporting to db
  private TravelerToDbVisitor m_vis=null;    // may not need this

  private ArrayList<ProcessNode> m_children=null;
  private ArrayList<Prerequisite> m_prerequisites=null;
  private ArrayList<PrescribedResult> m_results=null;
  private ArrayList<PrescribedResult> m_optionalResults=null;
  private ArrayList<RelationshipTask> m_relationshipTasks=null;
  private ProcessNodeDb[] m_childrenDb=null;
  private PrerequisiteDb[] m_prerequisitesDb=null;
  private PrescribedResultDb[] m_resultsDb=null;
  private PrescribedResultDb[] m_optionalResultsDb=null;
  private RelationshipTaskDb [] m_relationshipTasksDb=null;
  private ArrayList<NCRSpecificationDb> m_ncrSpecsDb=null;
  private ProcessNodeDb m_dbParent = null;
  
  private ConcurrentHashMap<String, String> m_relationshipTypeMap = null;
  private ConcurrentHashMap<String, String> m_relationshipActionMap = null;
  private ConcurrentHashMap<String, String> m_semanticsTypeMap = null;
  private ConcurrentHashMap<String, String> m_prerequisiteTypeMap = null;

  private ConcurrentHashMap<String, String> m_hardwareTypeNameMap = null;
  private ConcurrentHashMap<String, String> m_hardwareGroupNameMap = null;
  private ConcurrentHashMap<String, String> m_hardwareStatusIdMap = null;
  private ConcurrentHashMap<String, String> m_hardwareLabelIdMap = null;
  private ConcurrentHashMap<String, String> m_processNameIdMap = null;
  private ConcurrentHashMap<String, String> m_permissionGroupMap = null;

  private boolean m_verified=false;
}
