/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import  org.yaml.snakeyaml.Yaml;
import  org.yaml.snakeyaml.nodes.MappingNode;
import  org.yaml.snakeyaml.nodes.Node;
import  org.yaml.snakeyaml.nodes.ScalarNode;
import  org.yaml.snakeyaml.nodes.SequenceNode;
import  org.lsstcorp.etravelerbackend.exceptions.EtravelerException;

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
  public void acceptHardwareType(String hardwareType) {
    /* Obsolete field */
  }
  public void acceptHardwareGroup(String hardwareGroup) {
     if (m_isRoot) m_data.put("HardwareGroup", hardwareGroup);
  }
  public void acceptHardwareRelationshipType(String hardwareRelationshipType) {
    if (!m_isCloned) putIfPresent("HardwareRelationshipType", hardwareRelationshipType);
  
  }
  public void acceptHardwareRelationshipSlot(String hardwareRelationshipSlot) {
    if (!m_isCloned) putIfPresent("HardwareRelationshipSlot", hardwareRelationshipSlot); 
  }
  public void acceptVersion(String version) {
    m_data.put("Version", "next");
    if (!m_isCloned) {
      if (m_vis.getIncludeDbInternal() ) {
        m_data.put("FromSourceVersion", version);
      }
    }
  }
  public void acceptUserVersionString(String userVersionString) {
    if (!m_isCloned) putIfPresent("UserVersionString", userVersionString);
  }
  public void acceptDescription(String description) {
    if (!m_isCloned) putIfPresent("Description", description);
  }
  public void acceptInstructionsURL(String instructionsURL) {
    if (!m_isCloned) putIfPresent("InstructionsURL", instructionsURL);
  }
  public  void acceptMaxIteration(String maxIteration) {
    if (!m_isCloned) m_data.put("MaxIteration", maxIteration);
  }
  public void acceptSubsteps(String substeps) {
    m_substeps = substeps;
  }
  public void acceptTravelerActionMask(int travelerActionMask) {
     if (!m_isCloned) {
       /* Have to make a new ArrayList; 
        * go to some trouble to translate bits in mask to strings 
        */
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
     /* Ultimately need to do something similar to what is done for
      * children, after PrerequisiteToYaml class is defined
      */
    }
  }
  public void acceptPrescribedResults(ArrayList<PrescribedResult> prescribedResults) {
    if (prescribedResults == null) return;
    if (prescribedResults.size() == 0) return;
    if (!m_isCloned) {
      ArrayList<HashMap<String, Object> > resultList = 
          new ArrayList<HashMap<String, Object> > ();
      try { 
        for (PrescribedResult pres : prescribedResults) {
          m_vis.addPrescribedResult(resultList, pres, "add");
        }
      } catch (EtravelerException ex)  {
        System.out.println("Got exception in ProcessNodeToYaml.acceptPrescribedResults:");
        System.out.println(ex.getMessage());
        return;
      }
      m_data.put("RequiredInputs", resultList);
     /* Ultimately need to do something similar to what is done for
      * children, after PrescribedResultToYaml class is defined
      */
    }
  }
  /* Following is to transmit condition assoc. with parent edge. Need
   * this even if node is cloned
   */
  public void acceptCondition(String condition) {
     putIfPresent("Condition", condition);
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
  // Do we need anything more having to do with edges?
  // What about acceptChild ?
  // Signal to node in case it needs to do anything after contents are complete
  public void exportDone() {
    
  }
  private void putIfPresent(String key, String value) {
    if (value != null) {
      if (!value.isEmpty()) {
        m_data.put(key, value);
      }
    }
  }
  
}
