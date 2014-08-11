/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
//import  org.yaml.snakeyaml.Util;
import org.lsstcorp.etravelerbackendexceptions.ConflictingChildren;
import org.lsstcorp.etravelerbackendexceptions.UnknownReferent;
import org.lsstcorp.etravelerbackendexceptions.UnrecognizedYamlKey;
import org.lsstcorp.etravelerbackendexceptions.WrongTypeYamlValue;
import org.lsstcorp.etravelerbackendexceptions.YamlIncompatibleKeys;
import org.lsstcorp.etravelerbackendexceptions.NullYamlValue;
import org.lsstcorp.etravelerbackendexceptions.EtravelerException;
import  org.yaml.snakeyaml.Yaml;
import  org.yaml.snakeyaml.nodes.MappingNode;
import  org.yaml.snakeyaml.nodes.Node;
import  org.yaml.snakeyaml.nodes.ScalarNode;
import  org.yaml.snakeyaml.nodes.SequenceNode;
import  java.util.concurrent.ConcurrentHashMap;
import  java.util.ArrayList;
import  java.util.LinkedHashMap;
import  java.util.HashMap;
import  java.util.List;
import  java.util.Map;
import  java.util.Set;
import  java.util.Iterator;


/**
 *  Mediates between internal (ProcessNode) representation and yaml rep.
 *  Data members correspond to Yaml.  "provide" methods make the
 *  mapping to ProcessNode.
 * @author jrb
 */
public class ProcessNodeYaml implements ProcessNode.Importer {

  /*
   * Keys for map s_knownKeys are just that: the set of keys which may
   * appear in that part of a yaml file describing a process step.
   * For keys having scalar value, the value in the map is the default
   * for that key.   For other keys (all of which have list values),
   * the value in the map is null.
   * 
   * Will need to make some adjustments to support Clone.
   */
  static ArrayList<String> s_knownKeys;

  /**
     Concatenate process name, process version to get key, e.g. "myStep_1".  
     Value is just process name (e.g. "myStep" )
     Really this data structure should be per-session
   */
 
  static String formProcessKey(String name, String version) {
    return name + "_" + version;
  }
  static String formProcessKey(String name, int version) {
    return name + "_" + version;
  }

  static void initKnownKeys() {
    s_knownKeys = new ArrayList<String>();
    s_knownKeys.add("Name");
    s_knownKeys.add("HardwareType");
    s_knownKeys.add("HardwareRelationshipType");
    s_knownKeys.add("Version");
    s_knownKeys.add("UserVersionString");
    s_knownKeys.add("Description");
    s_knownKeys.add("InstructionsURL");
    s_knownKeys.add("MaxIteration");
    s_knownKeys.add("Condition");  
    s_knownKeys.add("Clone");
    s_knownKeys.add("Sequence");
    s_knownKeys.add("Selection");
    s_knownKeys.add("Prerequisites");
    s_knownKeys.add("RequiredInputs");
    s_knownKeys.add("TravelerActions");
    s_knownKeys.add("RefName");
    s_knownKeys.add("RefVersion");
    s_knownKeys.add("SourceDb");
  }
  static final int NAME=0;
  static final int HARDWARETYPE=1;
  static final int HARDWARERELATIONSHIPTYPE=2;
  static final int VERSION=3;
  static final int USERVERSIONSTRING=4;
  static final int DESCRIPTION=5;
  static final int INSTRUCTIONSURL=6;
  static final int MAXITERATION=7;
  static final int CONDITION=8;
  static final int CLONE=9;
  static final int SEQUENCE=10;
  static final int SELECTION=11;
  static final int PREREQUISITES=12;
  static final int REQUIREDINPUTS=13;
  static final int TRAVELERACTIONS=14;
  static final int REFNAME=15;
  static final int REFVERSION=16;
  static final int SOURCEDB=17;

 
  /**
   * 
   * @param yamlMap      Result of loading with snakeyaml
   * @param parent       Reference to ProcessNodeYaml parent, if any
   * @param isSelection  True only if we're in a list under Selection key.
   * @param iChild       0 if no parent.  Else positive int
   * @throws UnrecognizedYamlKey 
   */
  public void readYaml(Map<String, Object> yamlMap, 
                       ProcessNodeYaml parent, boolean isSelection, int iChild,
                       HashMap<String, ProcessNodeYaml> processes) 
    throws EtravelerException, Exception {
    if (processes == null) {  
      if (m_processes == null) {
        m_processes = new HashMap<String, ProcessNodeYaml>();
      }
    } else {
      m_processes = processes;
    }
    if (s_knownKeys == null) {
      initKnownKeys();
    }
    if (parent == null)  {
      if (yamlMap.containsKey("SourceDb"))  {
        m_sourceDb = yamlMap.get("SourceDb").toString();
      } 
    } else {
      m_sourceDb = parent.m_sourceDb;
    }
    m_parent = parent;
    m_edgeStep = iChild;
    
    // First check for RefName
    if (yamlMap.containsKey("RefName"))  {
      if (m_sourceDb == null)  {
        throw new NullYamlValue("SourceDb", "root Process", " when traveler contains ref node");
      }     
      if (yamlMap.containsKey("Name")) {  // not allowed
        throw new YamlIncompatibleKeys("RefName", "Name");
      }
      if (yamlMap.containsKey("Clone"))  { // also not allowed
        throw new YamlIncompatibleKeys("Refname", "Clone");
      }
      m_name = getStringVal(yamlMap, "RefName");
      m_isRef = true;
      m_version = getStringVal(yamlMap, "RefVersion", m_version);
      m_edgeCondition = getStringVal(yamlMap, "Condition" );
      return;
    }
    // Check for Clone
    if (yamlMap.containsKey("Clone"))  {
      if (yamlMap.get("Clone") == null) {
        throw new NullYamlValue("Clone", "Process", "");
      }
      if (yamlMap.containsKey("Name")) {   // not allowed for Clone
        throw new YamlIncompatibleKeys("Clone", "Name");
      }
      m_name = (yamlMap.get("Clone")).toString();
     
      m_version = getStringVal(yamlMap, "Version", m_version);
      
      m_edgeCondition = getStringVal(yamlMap, "Condition");     
      
      /* A clone must be cloned from something appearing earlier in the
       * yaml definition, but it must not be an ancestor
       */
      String processKey = formProcessKey(m_name, m_version);  
      ProcessNodeYaml referent = m_processes.get(processKey);
      if (referent == null) {
        throw new UnknownReferent(m_name, m_version);
      }
      m_clonedFrom = referent;
      ProcessNodeYaml ancestor = m_parent;
      while (ancestor != null) {
        if (referent == ancestor) {
          throw new EtravelerException("May not clone ancestor: " + m_name);
        }
        ancestor = ancestor.m_parent;
      }
      m_isClone = true;
      return;
    }

    Iterator<String> it = yamlMap.keySet().iterator();
    List<Node> list = null;               // only used if there are children
    while (it.hasNext()) {
      String foundKey = it.next();
      int keyIx = s_knownKeys.indexOf(foundKey);
      if (keyIx == -1) {
        throw new UnrecognizedYamlKey(foundKey, "Process");
      }
      
    
      if (yamlMap.get(foundKey) == null)  {
        throw new NullYamlValue(foundKey, "Process", "");
      }
      String v = yamlMap.get(foundKey).toString();
      switch (keyIx)  {
      case NAME:
        m_name = v; break;
      case HARDWARETYPE:
        m_hardwareType = v;
        break;
      case HARDWARERELATIONSHIPTYPE:
        m_hardwareRelationshipType =v; break;
      case VERSION:
        m_version = v; break;
      case USERVERSIONSTRING:
        m_userVersionString = v; break;
      case DESCRIPTION:
        m_description = v; break;
      case INSTRUCTIONSURL:
        m_instructionsURL = v; break;
      case MAXITERATION:
        m_maxIteration = v; 
        try {
          int maxI = Integer.parseInt(v);
          if (maxI < 1)  {
            throw new WrongTypeYamlValue("maxIteration", v, "Process");
          }
        } catch (NumberFormatException e) {
          throw new WrongTypeYamlValue("maxIteration", v, "Process");
        }
        break;
      case CONDITION:
        m_edgeCondition = v; break;
      case SOURCEDB:
      case REFNAME:
      case REFVERSION:
        break;  /* all handled above */
        /*
          already dealt with Clone above
      case CLONE:
        System.out.println("Clone is not yet implemented");
        throw new UnrecognizedYamlKey("Clone", "Process");
        */
      default: // remaining keys have list values
        // SequenceNode yamlSequence = (SequenceNode) yamlMap.get(foundKey);
        list = (List<Node>) yamlMap.get(foundKey);
        //list = yamlSequence.getValue();
        switch (keyIx) {
        case TRAVELERACTIONS:
          List<String> actionList = (List<String>) yamlMap.get(foundKey);
          for (int i=0; i < list.size(); i++) {
            String act = actionList.get(i);
            if (act.equals("HarnessedJob")) {
              m_travelerActionMask |= TravelerActionBits.HARNESSED;}
            else { 
              if (act.equals("MakeHardwareRelationship")) {
                m_travelerActionMask |= 
                  TravelerActionBits.MAKE_HARDWARE_RELATIONSHIP;
              } else {
                if (act.equals("BreakHardwareRelationship")) {
                  m_travelerActionMask |= 
                    TravelerActionBits.BREAK_HARDWARE_RELATIONSHIP;
                } else {
                  if (act.equals("SetHardwareStatus")) {
                    m_travelerActionMask |= 
                      TravelerActionBits.SET_HARDWARE_STATUS;
                  } else {
                    if (act.equals("SetHardwareLocation")) {
                      m_travelerActionMask |= 
                        TravelerActionBits.SET_HARDWARE_LOCATION;
                    } else {
                      if (act.equals("Async")) {
                        m_travelerActionMask |= TravelerActionBits.ASYNC;
                      } else {
                        if (act.equals("Automatable")) {
                          m_travelerActionMask |= TravelerActionBits.AUTOMATABLE;
                        } else    {
                          throw new UnrecognizedYamlKey(act, "TravelerActions");
                        }
                      }
                    }
                  }
                }
              }
            }
          }
          if ((m_travelerActionMask & (TravelerActionBits.ASYNC | 
                                      TravelerActionBits.HARNESSED)) ==
              TravelerActionBits.ASYNC) {
            m_travelerActionMask -= TravelerActionBits.ASYNC;
            System.out.println("Async qualifier on non-harnessed step ignored");
          }

          break;
        case SEQUENCE:
          if (m_substeps == "SELECTION") {
            throw new ConflictingChildren("SELECTION", "SEQUENCE");
          }
          m_substeps="SEQUENCE";
          m_nChildren = list.size();
          break;
        case SELECTION:
          if (m_substeps == "SEQUENCE") {
            throw new ConflictingChildren("SELECTION", "SEQUENCE");
          }
          m_substeps="SELECTION";
          m_nChildren = list.size();
          break;
        case PREREQUISITES:
          m_nPrerequisites = list.size();
          m_prerequisites = new PrerequisiteYaml[list.size()];
          for (int iP = 0; iP < m_nPrerequisites; iP++) {
       
            Map<String, Object> prereqMap = (Map<String, Object>) list.get(iP);
            m_prerequisites[iP] = new PrerequisiteYaml();
            m_prerequisites[iP].readYaml(prereqMap, this, iP);
          }
          // m_prerequisites = new PrerequisiteNodeYaml[list.size()];
          //  iterate and call PrerequisiteNodeYaml version of readYaml
          break;
        case REQUIREDINPUTS: 
          m_nPrescribedResults = list.size();
          m_prescribedResults = new PrescribedResultYaml[list.size()];
          for (int iR = 0; iR < m_nPrescribedResults; iR++) {
           
            Map<String, Object> reqInputsMap = (Map<String, Object>) list.get(iR);
            m_prescribedResults[iR] = new PrescribedResultYaml();
            m_prescribedResults[iR].readYaml(reqInputsMap, this, iR);
          }
      
          // similar to prereq case
          break;
        }
      }
    }
    // If "break" is not explicitly specified, set "make" bit
    if ((m_hardwareRelationshipType != null)  && !m_hardwareRelationshipType.isEmpty()) {  
      if ((m_travelerActionMask & TravelerActionBits.BREAK_HARDWARE_RELATIONSHIP) == 0) {
        m_travelerActionMask |= TravelerActionBits.MAKE_HARDWARE_RELATIONSHIP;
      }
    }
    // Hardware type is inherited from parent
    if (m_parent != null) {
      if (m_hardwareType == "") {
        m_hardwareType = m_parent.m_hardwareType;
      }
    }
    if (!m_isClone) {
      String processKey = formProcessKey(m_name, m_version);
      if (m_processes.containsKey(processKey)) {
        // tilt!
        throw new Exception("Duplicate process node named " + m_name);
      }
      m_processes.put(processKey, this);
    }

    // Finished with keys. Have handled everything except process children
    if (m_nChildren > 0) {    // Do recursion
      m_children = new ProcessNodeYaml[m_nChildren];
      boolean hasSelection = (m_substeps.equals("SELECTION"));
      for (int iC = 0; iC < m_nChildren; iC++) {
        //MappingNode yMap = (MappingNode) list.get(iC);
        //Map<String, Object> processMap = 
        //  (Map<String, Object>) yMap;
        Map<String, Object> processMap = (Map<String, Object>) list.get(iC);
        m_children[iC] = new ProcessNodeYaml();
        m_children[iC].readYaml(processMap, this, hasSelection, iC, m_processes);
      }
    }        
  
  }
  private String getStringVal(Map<String, Object> yamlMap, String keyName, String dflt) 
      throws NullYamlValue {
    if (yamlMap.containsKey(keyName) ) {
        if (yamlMap.get(keyName) == null) {
          throw new NullYamlValue(keyName, "Process", "");
        } else {
          return (yamlMap.get(keyName)).toString();
        }
      } else {
      return dflt;
    }
  }
  private String getStringVal(Map<String, Object> yamlMap, String keyName) 
      throws NullYamlValue {
    return getStringVal(yamlMap, keyName, null);
  }

/*
  private static String[] s_edgeCols = {"step", "cond"};
*/

  // ProcessNode.Importer interface implementation
  public String provideId() {return null;}
  public String provideName()  {return m_name;}
  public String provideHardwareType() {return m_hardwareType;}
  public String provideHardwareRelationshipType()  {
    return m_hardwareRelationshipType; }
  public String provideVersion() {return m_version;}
  public String provideUserVersionString() {return m_userVersionString;}
  public String provideDescription() {return m_description;}
  public String provideInstructionsURL() {return m_instructionsURL;}
  public String provideMaxIteration() {return m_maxIteration;}
  public String provideSubsteps() {return m_substeps;}
  public int provideTravelerActionMask() {return m_travelerActionMask;}
  public String provideOriginalId() {return null;}
  public int provideNChildren() {return m_nChildren;}
  public int provideNPrerequisites() {return m_nPrerequisites;}
  public int provideNPrescribedResults() {return m_nPrescribedResults;}
  public boolean provideIsCloned() {return (m_clonedFrom != null); }
  public boolean provideIsRef() {return m_isRef; }
  public String provideSourceDb() {return m_sourceDb;}
  public int provideEdgeStep() {return m_edgeStep;}
  public String provideEdgeCondition() {return m_edgeCondition;}
  //public String provideParentEdgeId() {return m_parentEdgeId;}
  public ProcessEdge provideParentEdge(ProcessNode parent, ProcessNode child) {
    ProcessEdge parentEdge = new ProcessEdge(parent, child, m_edgeStep, m_edgeCondition);
    ///parentEdge.setId(m_parentEdgeId);
    return parentEdge;
  }
  public ProcessNode provideChild(ProcessNode parent, int n) throws Exception {
    return new ProcessNode(parent, m_children[n]);
                           // ProcessNodeDb(m_connect, m_childIds[n],
                           //        m_childEdgeIds[n]) );


  }
 
  
  public Prerequisite providePrerequisite(ProcessNode parent, int n) {
    return new Prerequisite(parent, m_prerequisites[n]);
  }
   public PrescribedResult provideResult(ProcessNode parent, int n) {
    return new PrescribedResult(parent, m_prescribedResults[n]);
  }
  public void finishImport(ProcessNode process) {}

  // Properties read in directly from yaml
  private String m_name=null;
  private String m_hardwareType=null;
  private String m_hardwareRelationshipType=null;
  private String m_version="1";
  private String m_userVersionString=null;
  private String m_instructionsURL=null;
  private String m_description=null;
  private String m_maxIteration="1";
  private String m_edgeCondition = null;
  private String m_sourceDb = null;  // only of interest for top node
  
  private int m_nChildren = 0;
  private int m_nPrerequisites = 0;
  private int m_nPrescribedResults = 0;
  private boolean m_isClone=false;
  private boolean m_isRef=false;
  /*   private int m_nInputs = 0;  same as above?? */

  // Not read in directly from yaml
  private ProcessNodeYaml m_parent=null;
  private int m_travelerActionMask=0;  
  private String m_substeps="NONE";
  private int m_edgeStep = 0;    // inferred from location in file

  private ProcessNodeYaml m_clonedFrom=null;

  private ProcessNodeYaml[] m_children;
  private PrerequisiteYaml[] m_prerequisites;
  private PrescribedResultYaml[] m_prescribedResults;
  
  // Keep track of process name/version pairs we've seen
  private HashMap<String, ProcessNodeYaml> m_processes = null;
 
  // probably don't need these
  //  private String m_id=null;
  // private String m_originalId=null;
  // private String m_parentEdgeId = null;
  // private String[] m_childIds;   // save these to make children if asked
  // private String[] m_childEdgeIds;
}
