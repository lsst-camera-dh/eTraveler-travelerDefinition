/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;
//import java.io.FileWriter;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 *
 * @author jrb
 */
public class TravelerPrintVisitor 
  implements TravelerVisitor, ProcessNode.ExportTarget, 
             Prerequisite.ExportTarget, PrescribedResult.ExportTarget, 
             RelationshipTask.ExportTarget {
  private static String s_eol = "\n";
  public static void setEol(String eol)  {s_eol = eol;}
  public static void setIndent(String indent) {s_indent = indent;}
  public TravelerPrintVisitor() { 
      m_writer=null;
      TravelerPrintVisitor.s_nIndent = 0;
  }
  public TravelerPrintVisitor(Writer wrt) {
      m_writer=wrt;
  }
  public void visit(ProcessNode process, String activity, Object cxt) throws EtravelerException {
    resetProcessScalars();
    process.exportTo(this);
    String leadingBlanks="";
    for (int i = 0; i < s_nIndent; i++) {
      leadingBlanks += s_indent;
    }
    // Print out the scalar stuff
    if (m_isCloned) {
      try {
        m_writer.write(leadingBlanks + "++" + s_eol);
        m_writer.write(leadingBlanks + "Clone: " + m_name + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
      }
      return;
    }
    if (m_isRef) {
      try {
        m_writer.write(leadingBlanks + "++" + s_eol);
        m_writer.write(leadingBlanks + "RefName: " + m_name + s_eol);
        if (m_version != null) {
          m_writer.write(leadingBlanks + "RefVersion: " + m_version + s_eol);
        }
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
      }
      return;
    }
    try {
      m_writer.write(leadingBlanks + "++" + s_eol);
      if (m_sourceDb != null)  {
        m_writer.write(leadingBlanks + "SourceDb: " + m_sourceDb + s_eol);
      }
      m_writer.write(leadingBlanks + "Name: " + m_name + s_eol);
      m_writer.write(leadingBlanks + "Version: " + m_version + s_eol);
      if (m_userVersionString != null) {
        m_writer.write(leadingBlanks+"UserVersionString: " + m_userVersionString + s_eol);
      }
      if (m_jobname != null) {
        m_writer.write(leadingBlanks+"Jobname: " + m_jobname + s_eol);
      }
      if (m_travelerActionMask != 0) {
        m_writer.write(leadingBlanks + "TravelerActionMask: " + m_travelerActionMask);
        if ((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_LOCATION) != 0) {
          if (m_newLocation != null) {
            m_writer.write(leadingBlanks + "NewLocation: " + m_newLocation + s_eol);
          }  else {
            if (m_locationSite != null) {
              m_writer.write(leadingBlanks + "NewLocationInSite: " +
                             m_locationSite + s_eol);
            } else m_writer.write(leadingBlanks + "NewLocation: (?)" + s_eol);
          }
        }
        if ((m_travelerActionMask & TravelerActionBits.SET_HARDWARE_STATUS) !=0) {
          if (m_newStatus != null) {
            m_writer.write(leadingBlanks + "NewStatus: " + m_newStatus + s_eol);
          }  else {
            m_writer.write(leadingBlanks + "NewStatus: (?)" + s_eol);
          }
        }
        if ((m_travelerActionMask & TravelerActionBits.ADD_LABEL) !=0) {
          if (m_newStatus != null) {
            m_writer.write(leadingBlanks + "AddLabel: " + m_newStatus + s_eol);
          }  else {
            if (m_labelGroup == null) {
              m_writer.write(leadingBlanks + "AddLabel: (?)" + s_eol);
            } else m_writer.write(leadingBlanks + "AddLabelInGroup: "
                                  + m_labelGroup + s_eol);
          }
        }
        // No wildcard allowed for remove label
        if ((m_travelerActionMask & TravelerActionBits.REMOVE_LABEL) !=0) {
          if (m_newStatus != null) {
            m_writer.write(leadingBlanks + "RemoveLabel: " + m_newStatus + s_eol);
          } else {
            m_writer.write(leadingBlanks + "RemoveLabelInGroup: "
                           + m_labelGroup + s_eol);
          }
        }
      }
      if ((m_permissionGroups != null))  {
        m_writer.write(leadingBlanks + "PermissionGroups:" + s_eol);
        for (String g : m_permissionGroups) {
          m_writer.write(leadingBlanks + s_indent + g + s_eol);
        }
      }
      if ((m_travelerTypeLabels != null))  {
        m_writer.write(leadingBlanks + "TravelerTypeLabels:" + s_eol);
        for (String lbl : m_travelerTypeLabels) {
          m_writer.write(leadingBlanks + s_indent + lbl + s_eol);
        }
      }
      if (m_condition != null) {
        m_writer.write(leadingBlanks + "Condition: " + m_condition + s_eol);
      }
      if (m_hardwareCondition != null) {
        m_writer.write(leadingBlanks + "HardwareTypeCondition: " +
                       m_hardwareCondition + s_eol);
      }
      m_writer.write(leadingBlanks+"Max iteration: " + m_maxIteration + s_eol);
      m_writer.write(leadingBlanks+"Short description: " + 
                     m_shortDescription + s_eol);
      m_writer.write(leadingBlanks+"Description: " + m_description + s_eol);
    } catch (IOException ex) {
      System.out.println("whoops!  " + ex.getMessage());
      return;
    }
    if (m_prerequisites != null)  {
      try {
        m_writer.write(leadingBlanks+"Prerequisites:" + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
        return;
      }
      s_nIndent++;

      for (Prerequisite prereq: m_prerequisites) {
        prereq.accept(this, activity, cxt);
      }
      m_prerequisites = null;  
      s_nIndent--;
    }
    // Similar for PrescribedResults
    if (m_results != null) {
      try {
        m_writer.write(leadingBlanks+"RequiredInputs:" + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
        return;
      }
      s_nIndent++;

      for (PrescribedResult res: m_results) {
        res.accept(this, activity, cxt);
      }
      m_results = null;
      s_nIndent--;
    }
    // Relationship tasks
    if (m_relationshipTasks != null) {
      try {
        m_writer.write(leadingBlanks+"RelationshipTasks:" + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
        return;
      }
      s_nIndent++;

      for (RelationshipTask rel: m_relationshipTasks) {
        rel.accept(this, activity, cxt);
      }
      m_relationshipTasks = null;
      s_nIndent--;
    }
    if (m_optionalResults != null) {
      try {
        m_writer.write(leadingBlanks+"OptionalInputs:" + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
        return;
      }
      s_nIndent++;

      for (PrescribedResult res: m_optionalResults) {
        res.accept(this, activity, cxt);
      }
      m_optionalResults = null;
      s_nIndent--; 
    }
    // Children are more complicated.  Will need to instantiate
    // new TravelerPrintVisitor so we don't lose context
    if (m_children != null)  {
      try {
        m_writer.write(leadingBlanks + m_substeps + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
        return;
      }
      s_nIndent++;
      TravelerVisitor childVisitor = new TravelerPrintVisitor(m_writer);
      for (ProcessNode child: m_children) {
        child.accept(childVisitor, activity, cxt);
      }
      s_nIndent--;
    }
    
 
  }
  public void visit(Prerequisite prerequisite, String activity, Object cxt) {
    // Gets prereq info, prints it, using static value s_indent
    resetPrereq();
    prerequisite.exportTo(this);
    String leadingBlanks="";
    for (int i = 0; i < s_nIndent; i++) {
      leadingBlanks += s_indent;
    }
    try {
      m_writer.write(leadingBlanks + "*" + s_eol);
      leadingBlanks += s_indent;
      m_writer.write(leadingBlanks + "Prereq name: " + m_prereqName + s_eol);
      m_writer.write(leadingBlanks + "Prereq type: " + m_prereqType + s_eol);
    } catch (IOException ex) {
      System.out.println("Exception while writing prerequisite:");
      System.out.println(ex.getMessage());
    }
  }
  public void visit(RelationshipTask rel, String activity, Object cxt) {
    resetRelationshipTask();
    rel.exportTo(this);
    String leadingBlanks="";
    for (int i = 0; i < s_nIndent; i++) {
      leadingBlanks += s_indent;
    }
    try {
      m_writer.write(leadingBlanks + "*" + s_eol);
      leadingBlanks += s_indent;
      m_writer.write(leadingBlanks + "Relationship name: " + 
                     m_relationshipName + s_eol);
      m_writer.write(leadingBlanks + "Relationship action: " + 
                     m_relationshipAction + s_eol);
      m_writer.write(leadingBlanks + "Relationship slot: " + 
                     m_relationshipSlot + s_eol);

    } catch (IOException ex) {
      System.out.println("Exception while writing relationship task:");
      System.out.println(ex.getMessage());
    }
  }
      
  public void visit(PrescribedResult result, String activity, Object cxt) {
    // prints out prescribed result info, using s_indent, m_writer
    resetResult();
    result.exportTo(this);
        String leadingBlanks="";
    for (int i = 0; i < s_nIndent; i++) {
      leadingBlanks += s_indent;
    }
    try {
      m_writer.write(leadingBlanks + "*" + s_eol);
      leadingBlanks += s_indent;
      m_writer.write(leadingBlanks + "Result label: " + m_label + s_eol);
      m_writer.write(leadingBlanks + "Result name: " + m_resultName + s_eol);
      m_writer.write(leadingBlanks + "Result semantics: " + m_semantics + s_eol);      
      m_writer.write(leadingBlanks + "Result units: " + m_units + s_eol);
      m_writer.write(leadingBlanks + "Result description: " + m_resultDescription + s_eol);
      m_writer.write(leadingBlanks + "Result min value: " + m_minValue + s_eol);
      m_writer.write(leadingBlanks + "Result max value: " + m_maxValue + s_eol);
      if (m_semantics.equals("signature")) {
        m_writer.write(leadingBlanks + "Result role: " + m_resultRole + s_eol);
      }
    } catch (IOException ex) {
      System.out.println("Exception while writing prescribed result:");
      System.out.println(ex.getMessage());
    }
  
  }
 
  public void setWriter(Writer writer)  {m_writer = writer;}
  private void resetProcessScalars() {
    m_id=null; m_name=null;   //  m_hardwareRelationshipType=null;
    m_hardwareGroup=null; m_version=null;
    m_userVersionString=null; m_jobname=null; m_description=null;
    m_maxIteration=null; m_substeps=null; m_travelerActionMask=0;
    m_originalId=null; m_condition=null; m_hardwareCondition=null;
    m_newLocation=null;
  }
  
  private void resetPrereq() {
    m_prereqType=null;  m_prereqName=null; m_prereqProcessVersion=null;
    m_prereqProcessUserVersionString=null; m_prereqQuantity=1;
    m_prereqDescription=null;
  }
  private void resetResult() {
    m_label=null; m_units=null; m_minValue=null; m_maxValue=null;
    m_resultDescription=null; m_semantics=null; m_choiceField=null;
    m_resultRole=null;
    m_resultName=null;
  }
  private void resetRelationshipTask() {
    m_relationshipName= null;
    m_relationshipAction=null;
    m_relationshipSlot=null;
  }

  // Implementation of ProcessNode.ExportTarget
  public void acceptId(String id) {m_id = id;}
  public void acceptName(String name) {m_name = name;}
  public void acceptHardwareGroup(String hardwareGroup) {m_hardwareGroup  = hardwareGroup;}

  public void acceptVersion(String version) {m_version = version;}
  public void acceptUserVersionString(String userVersionString) {
    m_userVersionString = userVersionString;}
  public void acceptJobname(String jobname) {m_jobname=jobname;}
  public void acceptDescription(String description) {m_description = description;}
  public void acceptShortDescription(String desc) 
  {m_shortDescription = desc;}
  public void acceptInstructionsURL(String url) {
     m_instructionsURL = url;
   }
  public void acceptMaxIteration(String maxIteration) {
    m_maxIteration = maxIteration;}
  public void acceptNewLocation(String newLoc, String site) {
    m_newLocation=newLoc;
    m_locationSite=site;
  }
  public void acceptNewStatus(String newStat, String group) {
    m_newStatus=newStat;
    m_labelGroup=group;
  }
  public void acceptSubsteps(String substeps) {m_substeps = substeps;}
  public void acceptTravelerActionMask(int travelerActionMask) {
    m_travelerActionMask = travelerActionMask;}
  public void acceptPermissionGroups(ArrayList<String> groups) {
    if (groups == null) return;
    m_permissionGroups = new ArrayList<String>(groups.size());
    for (String g : groups) {
      m_permissionGroups.add(g);
    }
  }
  public void acceptTravelerTypeLabels(ArrayList<String> labels) {
    if (labels == null) return;
    m_travelerTypeLabels = new ArrayList<String>(labels.size());
    for (String lbl : labels) {
      m_travelerTypeLabels.add(lbl);
    }
  }
  public void acceptOriginalId(String originalId) {m_originalId = originalId;}
  public void acceptCondition(String condition) {m_condition=condition;}
  public void acceptHardwareCondition(String condition) {
    m_hardwareCondition=condition;
  }
  public void acceptChildren(ArrayList<ProcessNode> children) {m_children=children;}
  public void acceptPrerequisites(ArrayList<Prerequisite> prereqs) {
    m_prerequisites=prereqs;
  }
  public void acceptRelationshipTasks(ArrayList<RelationshipTask> rels) {
    m_relationshipTasks=rels;
  }
  public void acceptPrescribedResults(ArrayList<PrescribedResult> res) {
    m_results=res;
  }
  public void acceptOptionalResults(ArrayList<PrescribedResult> res) {
    m_optionalResults=res;
  }
  // Implementation of Prerequisite.ExportTarget
  public void acceptPrerequisiteType(String prerequisiteType) {
    m_prereqType = prerequisiteType;
  }
  // In expected use don't need to identify parent process
  public void acceptPrereqName(String name)  {m_prereqName = name; }
  public void acceptPrereqProcessVersion(String version) {
    m_prereqProcessVersion = version; }
  public void acceptPrereqProcessUserVersionString(String userVersionString) {
    m_prereqProcessUserVersionString = userVersionString;
  }
  public void acceptPrereqQuantity(int quantity) {m_prereqQuantity=quantity;}
  public void acceptPrereqDescription(String description) {
    m_prereqDescription = description;
  }
  public void acceptClonedFrom(ProcessNode clonedFrom) {
    m_clonedFrom = clonedFrom;
  }
  public void acceptIsCloned(boolean isCloned)  {
    m_isCloned = isCloned;
  }
   public void acceptHasClones(boolean hasClones)  {
    m_hasClones = hasClones;
  }
  public void acceptIsRef(boolean isRef) {
    m_isRef = isRef;
  }
  public void acceptSourceDb(String sourceDb) {
    m_sourceDb = sourceDb;
  }

  public void acceptPrereqParent(ProcessNode process) { }
 
  
  public void acceptPrereqId(String prereqId) {}
 
  // Implementation of RelationshipTask.ExportTarget
  public void acceptRelationshipName(String name) {
    m_relationshipName=name;
  }
  public void acceptRelationshipAction(String action) {
    m_relationshipAction=action;
  }
  public void acceptRelationshipSlot(String slot) {
    m_relationshipSlot=slot;
  }
  public void acceptRelationshipParent(ProcessNode process) { }
  public void acceptRelationshipTaskId(String id) {}

  // Implementation of PrescribedResult.ExportTarget
  public void acceptSemantics(String semantics) {
    m_semantics = semantics;
  }
  public void acceptUnits(String units) {
    m_units=units;
  }
  public void acceptMinValue(String minValue) {
    m_minValue = minValue;
  }
  public void acceptMaxValue(String maxValue) {
    m_maxValue = maxValue;
  }
  public void acceptLabel(String label) {
    m_label = label;
  }

  public void acceptResultDescription(String description) {
    m_resultDescription = description;
  }
  public void acceptResultName(String name) {
    m_resultName=name;
  }
  public void acceptSignatureRole(String role) {
    m_resultRole = role;
  }
  public void acceptIsOptional(String isOptional) {}
  
  public void acceptChoiceField(String choiceField)  {
    m_choiceField = choiceField;
  }
  public void acceptEdited(boolean edited) {}
  public void exportDone() { }
  
  // Store process contents until we're ready to write
  private String m_id=null;
  private String m_name=null;
  private String m_hardwareGroup=null;

  // delete next couple lines someday
  //  private String m_hardwareRelationshipType=null;
  //  private String m_hardwareRelationshipSlot="1";
  //   .. down to here
  private String m_version=null;
  private String m_userVersionString=null;
  private String m_jobname=null;
  private String m_description=null;
  private String m_shortDescription=null;
  private String m_instructionsURL=null;
  private String m_maxIteration=null;
  private String m_newLocation=null;
  private String m_locationSite=null;
  private String m_newStatus=null;
  private String m_labelGroup=null;
  private String m_substeps=null;
  private String m_condition=null;
  private String m_hardwareCondition=null;
  private int m_travelerActionMask=0;
  private String m_sourceDb=null;
  private String m_originalId=null;
  private ProcessNode m_clonedFrom = null;
  private boolean m_isCloned = false;
  private boolean m_hasClones = false;
  private boolean m_isRef = false;
  private ArrayList<ProcessNode> m_children=null;
  private ArrayList<Prerequisite> m_prerequisites=null;
  private ArrayList<PrescribedResult> m_results=null;
  private ArrayList<PrescribedResult> m_optionalResults=null;
  private ArrayList<RelationshipTask> m_relationshipTasks=null;
  private ArrayList<String> m_permissionGroups=null;
  private ArrayList<String> m_travelerTypeLabels=null;
  
  // Store prereq. contents until we're ready to write
  private String m_prereqType=null;
  private String m_prereqName=null;
  private String m_prereqProcessVersion=null;
  private String m_prereqProcessUserVersionString=null;
  private int    m_prereqQuantity=1;
  private String m_prereqDescription=null;

  // Store relationship task contents   until we're ready to write
  private String m_relationshipName=null;
  private String m_relationshipAction=null;
  private String m_relationshipSlot=null;

  // Store prescribed result contents until we're ready to write
  private String m_label=null;
  private String m_resultName=null;
  private String m_resultDescription=null;
  private String m_units=null;
  private String m_semantics=null;
  private String m_minValue=null;
  private String m_maxValue=null;
  private String m_resultRole=null;
  private String m_choiceField=null;
  
  private static String s_indent="  ";
  //private static int s_nIndent = 0;
 
  //private static Writer s_writer=null;
  private static int s_nIndent= 0;
  private Writer m_writer=null;
  
}
