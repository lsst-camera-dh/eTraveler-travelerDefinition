package org.lsst.camera.etraveler.backend.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import  org.yaml.snakeyaml.Yaml;
import  org.yaml.snakeyaml.nodes.MappingNode;
import  org.yaml.snakeyaml.nodes.Node;
import  org.yaml.snakeyaml.nodes.ScalarNode;
import  org.yaml.snakeyaml.nodes.SequenceNode;
import  org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

/**
 *
 * @author jrb
 */
public class ProcessNodeToYaml implements ProcessNode.ExportTarget {
  /* Constructor saves the map argument.  accept.. routines add things
   * that we care about to it
   * 
   */
  private Map<String, Object> m_data;
  private TravelerToYamlVisitor m_vis;
  private boolean m_isRoot=false;
  private boolean m_isCloned=false;
  private String m_substeps="NONE";
  private int m_travelerActionMask = 0;
  private String m_description=null;
  private String m_shortDescription=null;
  
  public ProcessNodeToYaml(TravelerToYamlVisitor vis, Map<String, Object> data)   {
    m_data = data;
    m_vis = vis;
  }
  public void setIsRoot(boolean isRoot) {
    m_isRoot = isRoot;
  }    
  /* Ignore id unless we were asked to keep track of internal stuff */
  public void acceptId(String id) { 
    if (m_vis.getIncludeDbInternal() ) {
      m_data.put("FromSourceId", id);
    }
  }
  public void acceptName(String name) {
    if (m_isCloned) {
     m_data.put("Clone", name); 
    } else {
     m_data.put("Name", name);
    }
  }
  public void acceptPermissionGroups(ArrayList<String> groups) {
    if (groups == null) return;
    m_data.put("PermissionGroups", groups);
  }
  public void acceptTravelerTypeLabels(ArrayList<String> labels) {
    if (labels == null) return;
    m_data.put("TravelerTypeLabels", labels);
  }

  public void acceptHardwareGroup(String hardwareGroup) {
     if (m_isRoot) m_data.put("HardwareGroup", hardwareGroup);
  }

  public void acceptVersion(String version) { 
    if (!m_isCloned) {
      m_data.put("Version", "next");
      if (m_vis.getIncludeDbInternal() ) {
        m_data.put("FromSourceVersion", version);
      }
    } else {
      m_data.put("Version", "cloned");
    }
    
  }
  public void acceptJobname(String jobname) {
    if (!m_isCloned) putIfPresent("Jobname", jobname);
  }
  public void acceptUserVersionString(String userVersionString) {
    if (!m_isCloned) putIfPresent("UserVersionString", userVersionString);
  }
  public void acceptDescription(String description) {
    if (!m_isCloned) m_description=description;
      //putIfPresent("Description", description);
  }
  public void acceptShortDescription(String desc) {
    if (!m_isCloned) {
      m_shortDescription = desc;
      putIfPresent("ShortDescription", desc);
    }
  }
  public void acceptInstructionsURL(String instructionsURL) {
    if (!m_isCloned) putIfPresent("InstructionsURL", instructionsURL);
  }
  public  void acceptMaxIteration(String maxIteration) {
    if (!m_isCloned) m_data.put("MaxIteration", maxIteration);
  }
  public void acceptNewLocation(String newLoc, String site) {
    if (newLoc != null) {
      m_data.put("NewLocation", newLoc);
    } else {
      if (site != null) {
        m_data.put("LocationSite", site);
      } else {
        if ((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_LOCATION)
            != 0 )         m_data.put("NewLocation", "(?)");
      }
    }
  }
  public void acceptNewStatus(String newStatus, String labelGroup) {
    switch (m_travelerActionMask & TravelerActionBits.STATUS_OR_LABEL) {
      case TravelerActionBits.SET_HARDWARE_STATUS:
        if (newStatus != null) {
          m_data.put("NewStatus", newStatus);
        } else {  m_data.put("NewStatus", "(?)");   }
        break;
      case TravelerActionBits.ADD_LABEL:
        if (newStatus != null) {
          m_data.put("AddLabel", newStatus);
        } else {
          if (labelGroup != null) {
            m_data.put("AddLabelInGroup", labelGroup);
          } else m_data.put("AddLabel", "(?)");
        }
        break;
      case TravelerActionBits.REMOVE_LABEL:
        if (newStatus != null)  {
          m_data.put("RemoveLabel", newStatus);
        } else {
          if (labelGroup != null) {
            m_data.put("RemoveLabelInGroup", labelGroup);
          }
        }
        break;
      default:
        return;
    }
  }

  public void acceptSubsteps(String substeps) {
    m_substeps = substeps;
  }
  public void acceptTravelerActionMask(int travelerActionMask) {
     if (!m_isCloned) {
       if (travelerActionMask == 0) return;
       /* cache for use by acceptNewLocation */
       m_travelerActionMask = travelerActionMask;
       ArrayList<String> actions = new ArrayList<String>();
       for (int iBit = 0; iBit < 32; iBit++) {
         int val = 1 << iBit;
         if ((travelerActionMask &  1 << iBit) != 0)  {
           actions.add(TravelerActionBits.getYamlKey(val));
           travelerActionMask -= val;
           if (travelerActionMask == 0) break;
         }
       }
       m_data.put("TravelerActions", actions);
     }
  }
  /* Don't keep track of original id in yaml unless requested to */
  public void acceptOriginalId(String originalId) { 
   if (m_vis.getIncludeDbInternal() ) {
      m_data.put("FromSourceOriginalId", originalId);
    }
  }
  public void acceptChildren(ArrayList<ProcessNode> childNodes)  {
    if (childNodes == null) return;
    if (childNodes.size() == 0) return;
    if (!m_isCloned)  {
      ArrayList<HashMap<String, Object> > children = new ArrayList<HashMap<String, Object> >();
      try {     
        for (ProcessNode child : childNodes) {
          /* call back visitor to make new maps for each child */
          m_vis.addChild(children, child, "add");
        }
      } catch (EtravelerException ex) {
        System.out.println("Got exception in ProcessNodeToYaml.acceptChildren:");
        System.out.println(ex.getMessage());
        return;
      }
       /* 
       * At the end, depending on value of substeps,
       * do put("Sequence", theSequenceNode) or
       * put("Selection", theSequenceNode)
       * where theSequenceNode is of type 
       * org.yaml.snakeyaml.nodes.SequenceNode
       *    ... or something like
       */
      if (m_substeps.equals("SEQUENCE")) {
        m_data.put("Sequence", children);
      }
      if (m_substeps.equals("SELECTION")) {
        m_data.put("Selection", children);
      }
      if (m_substeps.equals("HARDWARE_SELECTION")) {
        m_data.put("HardwareTypeSelection", children);
      }
    } 
  }
  public void acceptPrerequisites(ArrayList<Prerequisite> prerequisites) {
    if (prerequisites == null) return;
    if (prerequisites.size() == 0) return;
    if (!m_isCloned) {
      ArrayList<HashMap<String, Object> > prereqList = new ArrayList<HashMap<String, Object> >();
      try {
        for (Prerequisite prereq : prerequisites) {
          m_vis.addPrerequisite(prereqList, prereq, "add");
        }
      } catch (EtravelerException ex)  {
        System.out.println("Got exception in ProcessNodeToYaml.acceptPrerequisites:");
        System.out.println(ex.getMessage());
        return;
      }
      m_data.put("Prerequisites", prereqList);
    }
  }
  
  public void acceptRelationshipTasks(ArrayList<RelationshipTask> rels) {
    if (rels == null) return;
    if (rels.size() == 0 ) return;
    if (!m_isCloned) {
      ArrayList<HashMap<String, Object> > relList = new ArrayList<HashMap<String, Object> >();
      try {
        for (RelationshipTask rel : rels) {
          m_vis.addRelationshipTask(relList, rel, "add");
        }
      } catch (EtravelerException ex)  {
        System.out.println("Got exception in ProcessNodeToYaml.acceptRelationshipTasks:");
        System.out.println(ex.getMessage());
        return;
      }
      m_data.put("RelationshipTasks", relList);
    }
  }
  public void acceptPrescribedResults(ArrayList<PrescribedResult> prescribedResults) {    
    ArrayList<HashMap<String, Object> >  resultList = addResults(prescribedResults);
    if (resultList != null) m_data.put("RequiredInputs", resultList);
    
  }
  public void acceptOptionalResults(ArrayList <PrescribedResult> optionalResults) {
    ArrayList<HashMap<String, Object> >  resultList = addResults(optionalResults);
    if (resultList != null)  m_data.put("OptionalInputs", resultList);
  }
  private ArrayList<HashMap<String, Object> > addResults(ArrayList <PrescribedResult> results ) {
    if (results == null) return null;
    if (results.size() == 0) return null;
    if (m_isCloned) return null;
    ArrayList<HashMap<String, Object> > resultList = 
        new ArrayList<HashMap<String, Object> > ();
    try {
      for (PrescribedResult pres : results) {
        m_vis.addPrescribedResult(resultList, pres, "add");
      }
    } catch (EtravelerException ex)  {
      System.out.println("Got exception in ProcessNodeToYaml.addResults:");
      System.out.println(ex.getMessage());
      return null;
    }
    return resultList;
  }
  /* Following is to transmit condition assoc. with parent edge. Need
   * this even if node is cloned
   */
  public void acceptCondition(String condition) {
     putIfPresent("Condition", condition);
  }
  public void acceptHardwareCondition(String condition) {
     putIfPresent("HardwareTypeCondition", condition);
  }
  public void acceptClonedFrom(ProcessNode process) {
     
  }
  public void acceptHasClones(boolean hasClones) {
     
  }
  public void acceptIsCloned(boolean isCloned) {
    m_isCloned = isCloned;
     
  }
  public void acceptIsRef(boolean isRef) {
     
  }
  public void acceptEdited(boolean edited) {
     
  }
  /**
   * Not part of ExportTarget interface.  Normally only called for top
   * node
   * @param db 
   */
  public void acceptSourceDb(String db)  {
    if (m_vis.getIncludeDbInternal() ) {
      if (db != null) {
        m_data.put("FromSourceSourceDb", db);
      }
    }
  }
  public void acceptSubsystem(String sub) {
    if (sub != null) {
      m_data.put("Subsystem", sub);
    }
  }
  public void acceptStandaloneNCR(String NCR) {
    if (NCR != null) {
      m_data.put("NCR", NCR);
    }
  }
  // Do we need anything more having to do with edges?
  // What about acceptChild ?
  // Signal to node in case it needs to do anything after contents are complete
  public void exportDone() {
    // If description == shortDescription, only output latter
    if (m_isCloned) return;
    if (m_description == null) return;
    if (m_description.isEmpty()) return;
    if (!m_description.equals(m_shortDescription)) {
      putIfPresent("Description", m_description);
    }
  }
  private void putIfPresent(String key, String value) {
    if (value != null) {
      if (!value.isEmpty()) {
        m_data.put(key, value);
      }
    }
  }
  
}
