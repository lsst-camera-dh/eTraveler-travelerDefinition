/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackendexceptions.DbContentException;
import org.lsstcorp.etravelerbackendexceptions.EtravelerException;
import org.lsstcorp.etravelerbackenddb.DbConnection;
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
      String userVersion, String htype, String parentEdgeId, 
      ProcessNodeDb travelerRoot) throws SQLException {
    m_connect = connect;
    m_parentEdgeId = parentEdgeId;
    init(processName, userVersion, htype, travelerRoot);
  }
  public ProcessNodeDb(DbConnection connect, String processName, int version,
      String htype, String parentEdgeId, ProcessNodeDb travelerRoot) 
      throws SQLException {
    m_connect = connect;
    m_parentEdgeId = parentEdgeId;
    init(processName, version, htype, travelerRoot);
  }
  private static String[] s_initCols = {"name", "hardwareTypeId",
    "hardwareRelationshipTypeId", "version", "userVersionString",
    "description", "instructionsURL", "substeps", "maxIteration", "travelerActionMask",
    "originalId"};
  private static String[] s_edgeCols = {"step", "cond"};
  private static PreparedStatement s_processQuery = null;
  private static PreparedStatement s_edgeQuery = null;
  private static PreparedStatement s_prereqQuery = null;
  private static PreparedStatement s_prescribedResultsQuery = null;
  private static PreparedStatement s_childQuery = null;
  private static PreparedStatement s_edgeInfoQuery = null;
  /**
   * Query db and save all information of interest for process specified by
   * input parameters
   * @param processName   "name" field in db
   * @param userVersion   "userVersionString"  field in db
   * @return 
   */
  private void init(String processName, String userVersion, String htype, 
      ProcessNodeDb travelerRoot) throws SQLException {
    // fetch id, then call the other init
    String where = " WHERE Process.name=" + processName + " and userVersionString=" 
            + userVersion + " and HardwareType.name='" + htype 
        + "' and Process.hardwareTypeId=HardwareType.id";
    String id = m_connect.fetchColumn("Process join HardwareType", "Process.id", 
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
  private void init(String processName, int version, String htype,
      ProcessNodeDb travelerRoot) throws SQLException {
    // fetch id, then call the other init
    String where = " WHERE Process.name='" + processName + "' and version=" 
            + version  + " and HardwareType.name='" + htype 
        + "' and Process.hardwareTypeId=HardwareType.id";
    String id = m_connect.fetchColumn("Process join HardwareType", "Process.id", where);
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
      m_hardwareTypeId = rs.getString(++ix);
      m_hardwareType = m_hardwareTypeNameMap.get(m_hardwareTypeId);
      m_hardwareRelationshipTypeId = rs.getString(++ix);
      if (m_hardwareRelationshipTypeId != null) {
        m_hardwareRelationshipType = 
          m_relationshipTypeMap.get(m_hardwareRelationshipTypeId);
      }
      m_version = rs.getString(++ix);
      m_userVersionString = rs.getString(++ix);
      if (m_userVersionString  == null) m_userVersionString = "";
      m_description = rs.getString(++ix);
      m_instructionsURL = rs.getString(++ix);
      m_substeps = rs.getString(++ix);
      m_maxIteration = rs.getString(++ix);
      m_travelerActionMask = rs.getInt(++ix);
      m_originalId = rs.getString(++ix);
      rs.close();
    
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
      s_prereqQuery.setString(1, m_id);
      rs = s_prereqQuery.executeQuery();
      boolean more = rs.next();
      while (more) {
        m_nPrerequisites++;
        more = rs.next();
      }    
      if (m_nPrerequisites > 0) {
        m_prerequisiteIds = new String[m_nPrerequisites];
        rs.beforeFirst();
        for (int i=0; i < m_nPrerequisites; i++) {
          rs.next();
          m_prerequisiteIds[i] = rs.getString(1);
        }
      }
      rs.close();
      s_prescribedResultsQuery.setString(1, m_id);
      rs = s_prescribedResultsQuery.executeQuery();
      more = rs.next();
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
    
    where = " WHERE processId=?"; 
    getCol[0] = "id";
    s_prereqQuery = m_connect.prepareQuery("PrerequisitePattern", getCol, where);
    s_prescribedResultsQuery = m_connect.prepareQuery("InputPattern", getCol, where);
    
    if ((s_processQuery == null) || (s_edgeQuery == null) || 
        (s_prereqQuery == null) || (s_prescribedResultsQuery == null) ||        
        (s_edgeInfoQuery == null)) {
      throw  new SQLException("DbConnection.prepareQuery failure");
    }
  }
  // ProcessNode.Importer interface implementation: support import into ProcessNode
  public String provideId() {return m_id;}
  public String provideName()  {return m_name;}
  public String provideHardwareType() {return m_hardwareType;}
  public String provideHardwareRelationshipType()  {
    return m_hardwareRelationshipType; }
  public String provideVersion() {return m_version;}
  public String provideUserVersionString() {return m_userVersionString;}
  public String provideDescription() {
    if (m_description == null) return "";
    return m_description;
  }
   public String provideInstructionsURL() {
    if (m_instructionsURL == null) return "";
    return m_instructionsURL;
  }
  public String provideMaxIteration() {return m_maxIteration;}
  public String provideSubsteps() {return m_substeps;}
  public int provideTravelerActionMask() {return m_travelerActionMask;}
  public String provideOriginalId() {return m_originalId;}
  public int provideNChildren() {return m_nChildren;}
  public int provideNPrerequisites() {return m_nPrerequisites;}
  public int provideNPrescribedResults() {return m_nPrescribedResults;}
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
  public void acceptHardwareType(String hardwareType) {m_hardwareType = hardwareType;}
  public void acceptHardwareRelationshipType(String hardwareRelationshipType) {
    m_hardwareRelationshipType = hardwareRelationshipType;
  }
  public void acceptVersion(String version) { m_version = version; }
  public void acceptUserVersionString(String userVersionString) {
    m_userVersionString = userVersionString;
  }
  public void acceptDescription(String description) {m_description=description;}
  public void acceptInstructionsURL(String url) {m_instructionsURL = url;}
  public void acceptMaxIteration(String maxIteration) {m_maxIteration=maxIteration;}
  public void acceptSubsteps(String substeps) {m_substeps = substeps; }
  public void acceptTravelerActionMask(int travelerActionMask) {
    m_travelerActionMask = travelerActionMask;
  }
  public  void acceptOriginalId(String originalId) {m_originalId = originalId;}
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
    boolean hasRelation = (m_hardwareRelationshipType != null);
    if (m_prerequisites == null)  {
      if (hasRelation) {
        m_prerequisitesDb = new PrerequisiteDb[2]; // just in case we need them
        m_prerequisitesDb[0] = null;
        m_prerequisitesDb[1] = null;
      }
      return;
    }
    // Then maybe...
    int allocLen = m_prerequisites.size();
    if (hasRelation) {
      allocLen += 2;
    }
    if (prerequisites.size() > 0) {
      m_prerequisitesDb = 
        new PrerequisiteDb[allocLen];
      for (int ip = 0; ip < prerequisites.size(); ip++) {
        m_prerequisitesDb[ip] = new PrerequisiteDb(m_connect);
        prerequisites.get(ip).exportTo(m_prerequisitesDb[ip]);
      }
      
      if (hasRelation) {
        m_prerequisitesDb[allocLen - 2] = null;
        m_prerequisitesDb[allocLen - 1] = null;
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
  public void verify(DbConnection connect) throws EtravelerException {
   
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
    
    // Verify m_hardwareType against db for top node.  For others just check
    // it matches parent
    if (m_dbParent == null) {
      if (m_hardwareTypeNameMap.containsKey(m_hardwareType) ) {
        m_hardwareTypeId = m_hardwareTypeNameMap.get(m_hardwareType);
      }  else {
        throw new EtravelerException("No such hardware type " + m_hardwareType);
      }
    }   else { // assuming we checked compatibility of child earlier
      m_hardwareTypeId = m_dbParent.m_hardwareTypeId;
    }
    // If ref, verify we have the right db and that the node we need really is.
    // there. Also fetch travelerActionMask
    if (m_isRef)   {
      if (!m_sourceDb.equals(connect.getSourceDb() ) ){
        throw new EtravelerException("Process definition refers to wrong db");
      }
      String where = " where name='" + m_name + "' and version='" 
          + m_version + "' and hardwareTypeId='" + m_hardwareTypeId + "'";
      m_id = m_connect.fetchColumn("Process", "id", where);
      if (m_id == null) {
        throw new EtravelerException("Process " + m_name + ", version " 
            + m_version + ", hardware type " + m_hardwareTypeId 
            + "does not exist for dbType " + m_sourceDb);
      } 
      String actionMaskString = m_connect.fetchColumn("Process", 
          "travelerActionMask", where);
      try {
        m_travelerActionMask = Integer.parseInt(actionMaskString);
      } catch (NumberFormatException ex) {
        throw new EtravelerException("Ref process has bad travelerActionMask");
      }
      
      m_verified = true;
      return;
    }
        
    // Verify relationship type if not null
    if (m_hardwareRelationshipType != null) {
      if (m_relationshipTypeMap.containsKey(m_hardwareRelationshipType) ) {
        m_hardwareRelationshipTypeId = 
          m_relationshipTypeMap.get(m_hardwareRelationshipType);
      } else {
        throw new EtravelerException("No such hardware relationship type " 
                                     + m_hardwareRelationshipType);
      }
    }

    if (m_prerequisitesDb != null) {
      for (int ip=0; ip < m_prerequisitesDb.length; ip++) {
        m_prerequisitesDb[ip].verify(m_prerequisiteTypeMap, 
                                     m_hardwareTypeNameMap);
      }
    }
    if (m_resultsDb != null) {
      for (int ir=0; ir < m_resultsDb.length; ir++) {
        m_resultsDb[ir].verify(m_semanticsTypeMap);
      }
    }
    if (!(m_version.equals("1"))) { 
      if (!acceptableVersion(m_version)) {
        throw new 
          EtravelerException("Unacceptable Process version: " + m_version);
      }
      /* Look up id of version 1 */
      String where = " where version=1 and name='" + m_name 
          + "' and hardwareTypeId='" + m_hardwareTypeId +"'";
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
    if (m_childrenDb != null) {
      for (int ic=0; ic < m_childrenDb.length; ic++) {
        m_childrenDb[ic].verify(connect);
      
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
    try {
      int iv = Integer.parseInt(v);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }
  // NOTE:  Can use keyword DEFAULT for columns not always set to user-supplied,
  //         value, e.g. hardwareRelationshipType
  private static   String[] s_insertProcessCols={"name", "hardwareTypeId", "version",
    "userVersionString", "description", "instructionsURL", "substeps", "maxIteration", "originalId",
    "travelerActionMask", "hardwareRelationshipTypeId", "createdBy"};
  private static   String[] s_insertEdgeCols={"parent", "child", "step", "cond", "createdBy"};
  private static   String[] s_insertTravTypeCols={"rootProcessId", "createdBy"};
  public void writeToDb(DbConnection connect, ProcessNodeDb parent) 
    throws    SQLException, EtravelerException {
    if (!m_verified) {
      throw new EtravelerException("Unverified ProcessNodeDb cannot be written");
    }
    if (m_isRef) {
      insertEdge();
      return;
    }
    if (!m_isCloned) {
      // Make our row in Process
      String[] vals = new String[s_insertProcessCols.length];
      vals[0] = m_name;
      vals[1] = m_hardwareTypeId;
      if (m_version.equals("next") || m_version.equals("modified")) {
        m_version = nextAvailableVersion(m_originalId);
      }
      vals[2] = m_version;
      if ((m_userVersionString != null) && m_userVersionString.isEmpty()) {
        vals[3] = null;
      } else {
        vals[3] = m_userVersionString;
      }
      vals[4] = m_description;
      vals[5] = m_instructionsURL;
      vals[6] = m_substeps;
      vals[7] = m_maxIteration;
      vals[8] = m_originalId;
      vals[9] = String.valueOf(m_travelerActionMask);
      vals[10] = m_hardwareRelationshipTypeId;
      //  Value for user should come from Confluence log-in
      vals[11] = m_vis.getUser();
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
      // If there is a hardware relationship, might have to add
      // prerequisite(s)
      if ((m_hardwareRelationshipType != null) &&
          ((m_travelerActionMask & 
            TravelerActionBits.MAKE_HARDWARE_RELATIONSHIP) != 0)) {
        // Get hardware types associated with this relationship
        String cmp1id;
        String cmp2id;
        String where = " where id='" + m_hardwareRelationshipType + "'";
        cmp1id = m_connect.fetchColumn("HardwareRelationshipType", 
                                       "hardwareTypeId", where);
        cmp2id = m_connect.fetchColumn("HardwareRelationshipType", 
                                       "componentTypeId", where);
        if ((cmp1id == null) || (cmp2id == null)) {
          throw new SQLException("Unable to retrieve relationship type info");
        }

        if (!cmp1id.equals(m_hardwareTypeId))  {
          addComponentPrerequisite(cmp1id);  
        }
        if (!cmp2id.equals(m_hardwareTypeId))  {
          addComponentPrerequisite(cmp2id);  
        }
      }
    
      if (m_prerequisitesDb != null) {
        //   Write out prerequisite rows
        //   Note a couple extra components may have been allocated, hence
        //   check for null
        if (m_prerequisitesDb.length > 0) {
          for (int ip = 0; ip < m_prerequisitesDb.length; ip++ ) {
            if (m_prerequisitesDb[ip] != null) {
              m_prerequisitesDb[ip].writeToDb(connect, this, m_vis.getUser());
            }
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
    String where = " where originalId='" + originalId + 
      "' order by version desc limit 1";
    String old = m_connect.fetchColumn("Process", "version", where);
    int intNext = Integer.parseInt(old) + 1;
    return Integer.toString(intNext);
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
  /*
   * Add new row to TravelerType table
   */
  public  void registerTraveler() throws SQLException, EtravelerException {
    String[] vals = new String[s_insertTravTypeCols.length];
    vals[0] = m_id;
    vals[1] = m_vis.getUser();
    try {
      String travTypeId = m_connect.doInsert("TravelerType", s_insertTravTypeCols,
          vals, "", DbConnection.ADD_CREATION_TIMESTAMP);
    }  catch (SQLException ex) {
        System.out.println("Failed to create edge leading to process " + m_name + "with exception");
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
  }
  private void initIdMaps() throws SQLException {
    m_relationshipTypeMap = new ConcurrentHashMap<String, String>();
    fillIdMap("HardwareRelationshipType", "name", m_relationshipTypeMap);
    m_semanticsTypeMap = new ConcurrentHashMap<String, String>();
    fillIdMap("InputSemantics", "name", m_semanticsTypeMap);
    m_prerequisiteTypeMap = new ConcurrentHashMap<String, String>();
    fillIdMap("PrerequisiteType", "name", m_prerequisiteTypeMap);
    m_hardwareTypeNameMap = new ConcurrentHashMap<String, String>();
    fillIdMap("HardwareType", "name", m_hardwareTypeNameMap);
    m_processNameIdMap = new ConcurrentHashMap<String, String>();

  }

  private void copyIdMaps() {
    m_relationshipTypeMap = m_travelerRoot.m_relationshipTypeMap;
    m_semanticsTypeMap = m_travelerRoot.m_semanticsTypeMap;
    m_prerequisiteTypeMap = m_travelerRoot.m_prerequisiteTypeMap;
    m_hardwareTypeNameMap = m_travelerRoot.m_hardwareTypeNameMap;
    m_processNameIdMap = m_travelerRoot.m_processNameIdMap;

  }

  private void fillIdMap(String table, String nameCol, 
                         ConcurrentHashMap<String, String> map) 
    throws SQLException {
    String[] getCols = { "id", nameCol};
    PreparedStatement idNameQuery;
    ResultSet rs;
    try {
      idNameQuery = m_connect.prepareQuery(table, getCols, "");
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
 
  private String m_id=null;  
  private String m_name=null;
  private String m_hardwareType=null;
  private String m_hardwareTypeId = null;
  private String m_hardwareRelationshipType=null;
  private String m_hardwareRelationshipTypeId=null;
  private String m_version=null;
  private String m_userVersionString="";
  private String m_description=null;
  private String m_instructionsURL=null;
  private String m_substeps=null;
  private String m_maxIteration=null;
  private int m_travelerActionMask=0;
  private String m_originalId=null;
  private String m_parentEdgeId = null;
  private int m_edgeStep = 0;
  private String m_edgeCondition = null;
  private int m_nChildren = 0;
  private int m_nPrerequisites = 0;
  private int m_nPrescribedResults = 0;
  private String[] m_childIds;   // save these to make children if asked
  private String[] m_childEdgeIds;
  private String[] m_prerequisiteIds; // save these to make assoc prereqs
  private String[] m_resultIds; // save these to make assoc prescribed results
  private DbConnection m_connect;
  private boolean m_isCloned=false;
  private boolean m_hasClones = false;
  private boolean m_isRef=false;
  private boolean m_edited=false;
  private String m_sourceDb=null;
  private ProcessNodeDb m_travelerRoot=null; 
  private ConcurrentHashMap<String, ProcessNodeDb> m_nodeMap=null;
  // For exporting to db
  private TravelerToDbVisitor m_vis=null;    // may not need this

  private ArrayList<ProcessNode> m_children=null;
  private ArrayList<Prerequisite> m_prerequisites=null;
  private ArrayList<PrescribedResult> m_results=null;
  private ProcessNodeDb[] m_childrenDb=null;
  private PrerequisiteDb[] m_prerequisitesDb=null;
  private PrescribedResultDb[] m_resultsDb=null;
  private ArrayList<NCRSpecificationDb> m_ncrSpecsDb=null;
  private ProcessNodeDb m_dbParent = null;
  
  private ConcurrentHashMap<String, String> m_relationshipTypeMap = null;
  private ConcurrentHashMap<String, String> m_semanticsTypeMap = null;
  private ConcurrentHashMap<String, String> m_prerequisiteTypeMap = null;

  private ConcurrentHashMap<String, String> m_hardwareTypeNameMap = null;
  private ConcurrentHashMap<String, String> m_processNameIdMap = null;

  private boolean m_verified=false;
}
