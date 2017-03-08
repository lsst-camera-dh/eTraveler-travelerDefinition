/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;
import org.lsst.camera.etraveler.backend.exceptions.ConflictingChildren;
import org.lsst.camera.etraveler.backend.exceptions.UnknownReferent;
import org.lsst.camera.etraveler.backend.exceptions.UnrecognizedYamlKey;
import org.lsst.camera.etraveler.backend.exceptions.WrongTypeYamlValue;
import org.lsst.camera.etraveler.backend.exceptions.YamlIncompatibleKeys;
import org.lsst.camera.etraveler.backend.exceptions.NullYamlValue;
import org.lsst.camera.etraveler.backend.exceptions.IncompatibleChild;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.util.Verify;
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
import  java.io.Writer;
import  java.io.IOException;
import  java.util.regex.Pattern;

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
  /*
    Just use process name.  Do not allow multiple versions of a process
    step in a single traveler
  */
 
  static String formProcessKey(String name, String version) {
    return name + "_" + version;
  }
  static String formProcessKey(String name, int version) {
    return name + "_" + version;
  }
  static String formProcessKey(String name) {
    return name;
  }

  static void initKnownKeys() {
    s_knownKeys = new ArrayList<String>();
    s_knownKeys.add("Name");
    s_knownKeys.add("HardwareGroup");
    s_knownKeys.add("Version");
    s_knownKeys.add("UserVersionString");
    s_knownKeys.add("Description");
    s_knownKeys.add("ShortDescription");
    s_knownKeys.add("InstructionsURL");
    s_knownKeys.add("MaxIteration");
    s_knownKeys.add("Condition");  
    s_knownKeys.add("Clone");
    s_knownKeys.add("Sequence");
    s_knownKeys.add("Selection");
    s_knownKeys.add("Prerequisites");
    s_knownKeys.add("RequiredInputs");
    s_knownKeys.add("OptionalInputs");
    s_knownKeys.add("RelationshipTasks");
    s_knownKeys.add("TravelerActions");
    s_knownKeys.add("PermissionGroups");
    s_knownKeys.add("RefName");
    s_knownKeys.add("RefVersion");
    s_knownKeys.add("SourceDb");
    s_knownKeys.add("NewLocation");
    s_knownKeys.add("NewStatus");
    s_knownKeys.add("AddLabel");
    s_knownKeys.add("RemoveLabel");
    s_knownKeys.add("Subsystem");
    s_knownKeys.add("NCR");
    s_knownKeys.add("Jobname");
    /* Following are written by yaml export; informational only */
    s_knownKeys.add("FromSourceVersion");
    s_knownKeys.add("FromSourceId");
    s_knownKeys.add("FromSourceOriginalId");
    s_knownKeys.add("FromSourceSourceDb");
  }
  static final int NAME=0;
  static final int HARDWAREGROUP=1;
  static final int VERSION=2;
  static final int USERVERSIONSTRING=3;
  static final int DESCRIPTION=4;
  static final int SHORTDESCRIPTION=5;
  static final int INSTRUCTIONSURL=6;
  static final int MAXITERATION=7;
  static final int CONDITION=8;
  static final int CLONE=9;
  static final int SEQUENCE=10;
  static final int SELECTION=11;
  static final int PREREQUISITES=12;
  static final int REQUIREDINPUTS=13;
  static final int OPTIONALINPUTS=14;
  static final int RELATIONSHIPTASKS=15;
  static final int TRAVELERACTIONS=16;
  static final int PERMISSIONGROUPS=17;
  static final int REFNAME=18;
  static final int REFVERSION=19;
  static final int SOURCEDB=20;
  static final int NEWLOCATION=21;
  static final int NEWSTATUS=22;
  static final int ADDLABEL=23;
  static final int REMOVELABEL=24;
  static final int SUBSYSTEM=25;
  static final int NCR=26;
  static final int JOBNAME=27;
  
  static final int FROMSOURCEVERSION=28;
  static final int FROMSOURCEID=29;
  static final int FROMSOURCEORIGINALID=30;
  static final int FROMSOURCESOURCEDB=31;
  
  public ProcessNodeYaml() {}
  
  public ProcessNodeYaml(Writer wrt, String eol, String nameHandling)  {
    m_writer = wrt;
    m_eol = eol;
    m_nameHandling = nameHandling;
  }
  
  /**
   * 
   * @param yamlMap      Result of loading with snakeyaml
   * @param parent       Reference to ProcessNodeYaml parent, if any
   * @param isSelection  True only if we're in a list under Selection key.
   * @param iChild       0 if no parent.  Else positive int
   * @throws UnrecognizedYamlKey 
   */
  public boolean readYaml(Map<String, Object> yamlMap, 
                       ProcessNodeYaml parent, boolean isSelection, int iChild,
                       HashMap<String, ProcessNodeYaml> processes) 
    throws EtravelerException, Exception {
    boolean ok = true;
    if (processes == null) {  
      if (m_processes == null) {
        m_processes = new HashMap<>();
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
      if (!yamlMap.containsKey("HardwareGroup")) {
        throw new EtravelerException("Missing keyword 'HardwareGroup' in " +
                                     "root step of traveler definition");
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
      // For refs, version defaults to "last"
      m_version = getStringVal(yamlMap, "RefVersion", "last");
      // Check that version is a positive integer
      if (! m_version.equals("last")) {
        if (!(Verify.isPosInt(m_version)).isEmpty()) {
          throw new WrongTypeYamlValue("version", m_version, "Process");
        }
      }
      // If there is already a node with our name, it must be
      // another reference node with the same version
      String processKey = formProcessKey(m_name);
      if (m_processes.containsKey(processKey)) {
        ProcessNodeYaml referent = m_processes.get(processKey);
        if (!referent.m_isRef) {
          throw new
            EtravelerException("RefName value " + m_name +
                               " already is name of prior node");
        }
        if (referent.m_version != m_version) {
          throw new
          EtravelerException("Ref steps with RefName " + m_name +
                             " must all be of same version");
        }
      } else  { // make an entry for it
        m_processes.put(processKey, this);
      }
      m_edgeCondition = getStringVal(yamlMap, "Condition" );
      return ok;
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
      if (yamlMap.containsKey("Version")) {
        if (!getStringVal(yamlMap,"Version").equals("cloned")) {
          throw new YamlIncompatibleKeys("Clone", "Version");  
        }
      }
      // Set m_version below, once we find referent
    
      
      m_edgeCondition = getStringVal(yamlMap, "Condition");     
      
      /* A clone must be cloned from something appearing earlier in the
       * yaml definition, but it must not be an ancestor
       */
      String processKey = formProcessKey(m_name);  
      ProcessNodeYaml referent = m_processes.get(processKey);
      if (referent == null) {
        throw new UnknownReferent(m_name);
      }
      if (referent.m_isRef) {
        throw new EtravelerException("May not clone Reference node: "
                                     + m_name);
      }
      m_clonedFrom = referent;
      m_version = "cloned";
      referent.m_hasClones = true; 
      ProcessNodeYaml ancestor = m_parent;
      while (ancestor != null) {
        if (referent == ancestor) {
          throw new EtravelerException("May not clone ancestor: " + m_name);
        }
        ancestor = ancestor.m_parent;
      }
      m_isClone = true;
      return ok;
    }

    Iterator<String> it = yamlMap.keySet().iterator();
    List<Node> list = null;  // only used if our node has a non-scalar value
    List<Node> childList = null; // only used for Sequence or Selection
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
        m_name = v;
        if (m_nameHandling.equals("warn")) {
            ok = ok && checkName(m_name);
        }
        break;
      case HARDWAREGROUP:
        if (m_parent != null)  {
          throw new IncompatibleChild(m_parent.m_name, "this child", 
              "child may not specify hardware group");
        }
        m_hardwareGroup = v;
        break;

      case SUBSYSTEM:
        // ignore for all but top node
        if (m_parent == null) m_subsystem=v;
        break;
      case NCR:
        // ignore for all but top node
        if (m_parent == null) {
          if ((!v.equals("0")) && (!v.equals("no")) && (!v.equals("No"))
              && (!v.equals("false")) && (!v.equals("False")) )
          m_standaloneNCR="1";
        }
        break;
      case VERSION:
        m_version = v; 
        if (!m_version.equals("next")) {
          if (!(Verify.isPosInt(m_version)).isEmpty()) {
            throw new WrongTypeYamlValue("version", m_version, "Process");
          }
        }
        break;
      case JOBNAME:
        m_jobname = v;
        if (m_nameHandling.equals("warn")) {
            ok = ok && checkName(m_jobname);
        }
        break;
      case USERVERSIONSTRING:
        m_userVersionString = v; break;
      case DESCRIPTION:
        m_description = v; break;
      case SHORTDESCRIPTION:
        m_shortDescription = v; break;
      case INSTRUCTIONSURL:
        m_instructionsURL = v; break;
      case MAXITERATION:
        m_maxIteration = v; 
        try {
          int maxI = Integer.parseInt(v);
          if (maxI < 1)  {
            throw new WrongTypeYamlValue("maxIteration", v, "Process");
          }
          if ((parent == null) && (maxI != 1)) {
            throw new
              EtravelerException("MaxIteration for root step must be 1");
          }
        } catch (NumberFormatException e) {
          throw new WrongTypeYamlValue("maxIteration", v, "Process");
        }
        break;
      case NEWLOCATION:      
        m_travelerActionMask |= TravelerActionBits.SET_HARDWARE_LOCATION;
        /* several different aliases may be used for operator prompt */
        if (v.equals("(TBD)") || v.equals("(tbd)") || v.equals("(operatorPrompt)")
            || v.equals("(OPERATORPROMPT)") || v.equals("(prompt)") || v.equals("(PROMPT)") ) {
          v = "(?)";
        }
        m_newLocation = v;
        break;
      case NEWSTATUS:
        m_travelerActionMask |= TravelerActionBits.SET_HARDWARE_STATUS;
        /* several different aliases may be used for operator prompt */
        if (v.equals("(TBD)") || v.equals("(tbd)") || v.equals("(operatorPrompt)")
            || v.equals("(OPERATORPROMPT)") || v.equals("(prompt)") || v.equals("(PROMPT)") ) {
          v = "(?)";
        }
        m_newStatus = v;
        break;
      case ADDLABEL:
        m_travelerActionMask |= TravelerActionBits.ADD_LABEL;
        if (v.equals("(TBD)") || v.equals("(tbd)") || v.equals("(operatorPrompt)")
            || v.equals("(OPERATORPROMPT)") || v.equals("(prompt)") || v.equals("(PROMPT)") ) {
          v = "(?)";
        }
        m_newStatus = v;
        break;
      case REMOVELABEL:
        m_travelerActionMask |= TravelerActionBits.REMOVE_LABEL;
        m_newStatus = v;
        break;
      case CONDITION:
        m_edgeCondition = v; break;
      case SOURCEDB:
      case REFNAME:
      case REFVERSION:
        break;  /* all handled above */
      case FROMSOURCEVERSION:
      case FROMSOURCEID:
      case FROMSOURCEORIGINALID:
      case FROMSOURCESOURCEDB:
        break;   /* Informational only; nothing to do */
       
      default: // remaining keys have list values
        list = (List<Node>) yamlMap.get(foundKey);
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
                        } else  {
                          if (act.equals("RemoveLabel")) {
                           m_travelerActionMask |= TravelerActionBits.REMOVE_LABEL; 
                          } else {
                            if (act.equals("AddLabel")) {
                              m_travelerActionMask |= TravelerActionBits.ADD_LABEL;
                            } else {
                              if (act.equals("Repeatable")) {
                                m_travelerActionMask |= TravelerActionBits.REPEATABLE;
                              } else {
                                throw new UnrecognizedYamlKey(act, "TravelerActions");
                              }
                            }
                          }
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
          if (m_substeps.equals("SELECTION") ) {
            throw new ConflictingChildren("SELECTION", "SEQUENCE");
          }
          m_substeps="SEQUENCE";
          childList = (List<Node>) yamlMap.get(foundKey);
          m_nChildren = childList.size();
          break;
        case SELECTION:
          if (m_substeps.equals("SEQUENCE") ) {
            throw new ConflictingChildren("SELECTION", "SEQUENCE");
          }
          m_substeps="SELECTION";
          childList = (List<Node>) yamlMap.get(foundKey);
          m_nChildren = childList.size();
          break;
        case PREREQUISITES:
          m_nPrerequisites = list.size();
          m_prerequisites = new PrerequisiteYaml[list.size()];
          for (int iP = 0; iP < m_nPrerequisites; iP++) {
       
            Map<String, Object> prereqMap = (Map<String, Object>) list.get(iP);
            m_prerequisites[iP] = new PrerequisiteYaml();
            m_prerequisites[iP].readYaml(prereqMap, this, iP);
          }
          break;
        case RELATIONSHIPTASKS:
          m_nRelationshipTasks = list.size();
          m_relationshipTasks = new RelationshipTaskYaml[list.size()];
          for (int iRt=0; iRt < m_nRelationshipTasks; iRt++) {           
            Map<String, Object> relaMap = (Map<String, Object>) list.get(iRt);
            m_relationshipTasks[iRt] = new RelationshipTaskYaml();
            m_relationshipTasks[iRt].readYaml(relaMap, this, iRt);       
          }
          break;
        case PERMISSIONGROUPS:
          m_permissionGroups = new ArrayList<>(list.size());
          List<String> groupList = (List<String>) yamlMap.get(foundKey);
          for (String g : groupList) {
            m_permissionGroups.add(g);
          }
          break;
        case REQUIREDINPUTS: 
          m_nPrescribedResults = list.size();
          m_prescribedResults = new PrescribedResultYaml[list.size()];
          for (int iR = 0; iR < m_nPrescribedResults; iR++) {
           
            Map<String, Object> reqInputsMap = (Map<String, Object>) list.get(iR);
            m_prescribedResults[iR] = new PrescribedResultYaml();
            m_prescribedResults[iR].readYaml(reqInputsMap, this, iR);
          }
          break;
        case OPTIONALINPUTS:
          m_nOptionalResults = list.size();
          m_optionalResults = new PrescribedResultYaml[list.size()];
          for (int iR = 0; iR < m_nOptionalResults; iR++) {
            Map<String, Object> optInputsMap = (Map<String, Object>) list.get(iR);
            m_optionalResults[iR] = new PrescribedResultYaml();
            m_optionalResults[iR].setIsOptional("1");
            m_optionalResults[iR].readYaml(optInputsMap, this, iR);
          }
          // similar to prereq case
          break;
        }
      }
    }
    /* 
       At this point everything has been read in. Make adjustments for
       various fields which are in some sense defaulted or derived
       from other fields
    */
    if (m_description == null) m_description = m_shortDescription;

    // May have at most one of newStatus, addLabel, removeLabel set
    if ((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_STATUS) != 0) {
      if ((m_travelerActionMask & (TravelerActionBits.ADD_LABEL + TravelerActionBits.REMOVE_LABEL )) != 0) 
        throw new EtravelerException("Cannot set hardware status and manipulate label in same step");  
    }
    if ( ((m_travelerActionMask & TravelerActionBits.ADD_LABEL) != 0) &&
        ((m_travelerActionMask & TravelerActionBits.REMOVE_LABEL) != 0) )
      throw new EtravelerException("Cannot add and remove label in the same step");
   
    // Hardware group is inherited from parent
    if (m_parent != null) {
      m_hardwareGroup = m_parent.m_hardwareGroup;  
    }
    if (!m_isClone) {
      String processKey = formProcessKey(m_name);
      if (m_processes.containsKey(processKey)) {
        // tilt!
        throw new Exception("Duplicate process node named " + m_name);
      }
      m_processes.put(processKey, this);
    }
    // May not ask for operator input on automated steps
    if ((m_nPrescribedResults + m_nOptionalResults > 0) &&
        ((TravelerActionBits.AUTOMATABLE + TravelerActionBits.HARNESSED) 
        & m_travelerActionMask) !=0 )  {
      throw new EtravelerException("Operator inputs not allowed on automated step \""
          + m_name + "\" ");
    }
    if ((TravelerActionBits.HARNESSED & m_travelerActionMask) == 0) {
      m_jobname = null;
    } else {
      if (m_jobname == null) m_jobname = m_name;
    }

    // Finished with keys. Have handled everything except process children
    if (m_nChildren > 0) {    // Do recursion
      m_children = new ProcessNodeYaml[m_nChildren];
      boolean hasSelection = (m_substeps.equals("SELECTION"));
      for (int iC = 0; iC < m_nChildren; iC++) {
        Map<String, Object> processMap =
          (Map<String, Object>) childList.get(iC);
        m_children[iC] = new ProcessNodeYaml(m_writer, m_eol, m_nameHandling);
        
        boolean childOk =
          m_children[iC].readYaml(processMap, this, hasSelection, iC, m_processes);
        ok = ok && childOk;
      }
    }  
    return ok;
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
   * Don't throw any exceptions yet; save that for a future release. Print
   * error message if name contains unacceptable characters.
   * Return true if name is ok; false otherwise
   */
  private boolean checkName(String name) throws WrongTypeYamlValue, IOException {
    if (m_writer == null) {
        throw new IOException("checkName has no writer to complain with");
    }
    String proscribed = ".*[ ',#{}:/$&?!^=*\\\"\\[\\]\\s].*";
    String noInitialHyphen = "-.*";
    //String proscribed = "[',#{}:/$&?!]";
    boolean match = Pattern.matches(proscribed, name);
    if (match) {
      m_writer.write("ERROR!! " + m_eol);
      m_writer.write("Step name '" + name + "' contains whitespace or one of these frowned-upon characters: " + m_eol);
      m_writer.write("# : / $ & , ' \" ! ? = * ^ } { ] [ " + m_eol);
      m_writer.flush();
      return false;
    }
    if (Pattern.matches(noInitialHyphen, name)) {
        m_writer.write("ERROR!! " + m_eol);
        m_writer.write("Step name '" + name +
                       "' starts with a hyphen, which is not allowed" + m_eol);  
        m_writer.flush();
        return false;
    }
    return true;
  }

  // ProcessNode.Importer interface implementation
  public String provideId() {return null;}
  public String provideName()  {return m_name;}
  public String provideHardwareGroup() {return m_hardwareGroup;}
  public String provideVersion() {return m_version;}
  public String provideJobname() {return m_jobname;}
  public String provideUserVersionString() {return m_userVersionString;}
  public String provideDescription() {return m_description;}
  public String provideShortDescription() {return m_shortDescription;}
  public String provideInstructionsURL() {return m_instructionsURL;}
  public String provideMaxIteration() {return m_maxIteration;}
  public String provideNewLocation() {return m_newLocation;}
  public String provideNewStatus() {return m_newStatus;}
  public String provideSubsteps() {return m_substeps;}
  public int provideTravelerActionMask() {return m_travelerActionMask;}
  public ArrayList<String> providePermissionGroups() {return m_permissionGroups;}
  public String provideOriginalId() {return null;}
  public int provideNChildren() {return m_nChildren;}
  public int provideNPrerequisites() {return m_nPrerequisites;}
  public int provideNPrescribedResults() {return m_nPrescribedResults;}
  public int provideNOptionalResults() {return m_nOptionalResults;}
  public int provideNRelationshipTasks() {
    return m_nRelationshipTasks;
  }
  public boolean provideIsCloned() {return (m_clonedFrom != null); }
  public boolean provideHasClones() {return m_hasClones; }
  public boolean provideIsRef() {return m_isRef; }
  public String provideSourceDb() {return m_sourceDb;}
  public int provideEdgeStep() {return m_edgeStep;}
  public String provideEdgeCondition() {return m_edgeCondition;}
  public ProcessEdge provideParentEdge(ProcessNode parent, ProcessNode child) {
    ProcessEdge parentEdge = new ProcessEdge(parent, child, m_edgeStep, m_edgeCondition);
    return parentEdge;
  }
  public ProcessNode provideChild(ProcessNode parent, int n) throws Exception {
    return new ProcessNode(parent, m_children[n]);                      
  }
 
  public Prerequisite providePrerequisite(ProcessNode parent, int n) {
    if (n > m_nPrerequisites) return null;
    return new Prerequisite(parent, m_prerequisites[n]);
  }
  public PrescribedResult provideResult(ProcessNode parent, int n) {
    if (n > m_nPrescribedResults) return null;
    return new PrescribedResult(parent, m_prescribedResults[n]);
  }
  public PrescribedResult provideOptionalResult(ProcessNode parent, int n) {
    if (n > m_nOptionalResults) return null;
    PrescribedResult pr = new PrescribedResult(parent, m_optionalResults[n]);
    pr.setIsOptional("1");
    return pr;
  }
  public RelationshipTask provideRelationshipTask(ProcessNode parent, int n) {
    if (n > m_nRelationshipTasks) return null;
    return new RelationshipTask(parent, m_relationshipTasks[n]);
  }
  public void finishImport(ProcessNode process) {}
  public String getSubsystem() {return m_subsystem;}
  public String getStandaloneNCR() {return m_standaloneNCR;}

  public Writer getWriter() {return m_writer;}
  public String getEol() {return m_eol;}

  // Properties read in directly from yaml
  private String m_name=null;
  private String m_hardwareGroup=null;
  
  private String m_version="next";
  private String m_userVersionString=null;
  private String m_instructionsURL=null;
  private String m_description=null;
  private String m_shortDescription=null;
  private String m_maxIteration="1";
  private String m_newLocation=null;
  private String m_newStatus=null;
  private String m_edgeCondition = null;
  private String m_sourceDb = null;  // only of interest for top node
  private String m_standaloneNCR = null; // only of interest for top node
  private String m_jobname = null;
  
  private int m_nChildren = 0;
  private int m_nPrerequisites = 0;
  private int m_nPrescribedResults = 0;
  private int m_nOptionalResults = 0;
  private int m_nRelationshipTasks = 0;
  private boolean m_isClone=false;
  private boolean m_hasClones=false;
  private boolean m_isRef=false;
  /*   private int m_nInputs = 0;  same as above?? */

  // Not read in directly from yaml
  private ProcessNodeYaml m_parent=null;
  private int m_travelerActionMask=0;  
  private String m_substeps="NONE";
  private int m_edgeStep = 0;    // inferred from location in file

  private ProcessNodeYaml m_clonedFrom=null;
  private String m_subsystem=null;

  private ProcessNodeYaml[] m_children;
  private PrerequisiteYaml[] m_prerequisites;
  private PrescribedResultYaml[] m_prescribedResults;
  private PrescribedResultYaml[] m_optionalResults;
  private RelationshipTaskYaml [] m_relationshipTasks;
  private ArrayList<String>  m_permissionGroups=null;
  private Writer m_writer = null;
  private String m_nameHandling = "none";
  private String m_eol = "";
  
  // Keep track of process name/version pairs we've seen
  private HashMap<String, ProcessNodeYaml> m_processes = null; 
}
