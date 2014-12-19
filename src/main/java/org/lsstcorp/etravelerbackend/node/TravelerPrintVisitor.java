/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;
//import java.io.FileWriter;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 *
 * @author jrb
 */
public class TravelerPrintVisitor implements TravelerVisitor, 
    ProcessNode.ExportTarget, Prerequisite.ExportTarget, PrescribedResult.ExportTarget {
  private static String s_eol = "\n";
  public static void setEol(String eol)  {s_eol = eol;}
  public static void setIndent(String indent) {s_indent = indent;}
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
        s_writer.write(leadingBlanks + "++" + s_eol);
        s_writer.write(leadingBlanks + "Clone: " + m_name + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
      }
      return;
    }
    if (m_isRef) {
      try {
        s_writer.write(leadingBlanks + "++" + s_eol);
        s_writer.write(leadingBlanks + "RefName: " + m_name + s_eol);
        if (m_version != null) {
          s_writer.write(leadingBlanks + "RefVersion: " + m_version + s_eol);
        }
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
      }
      return;
    }
    try {
      s_writer.write(leadingBlanks + "++" + s_eol);
      if (m_sourceDb != null)  {
        s_writer.write(leadingBlanks + "SourceDb: " + m_sourceDb + s_eol);
      }
      s_writer.write(leadingBlanks + "Name: " + m_name + s_eol);
      s_writer.write(leadingBlanks + "Version: " + m_version + s_eol);
      if (m_userVersionString != null) {
        s_writer.write(leadingBlanks+"UserVersionString: " + m_userVersionString + s_eol);
      }
      if (m_condition != null) {
        s_writer.write(leadingBlanks + "Condition: " + m_condition + s_eol);
      }
      s_writer.write(leadingBlanks+"Max iteration: " + m_maxIteration + s_eol);
      s_writer.write(leadingBlanks+"Description: " + m_description + s_eol);
    } catch (IOException ex) {
      System.out.println("whoops!  " + ex.getMessage());
      return;
    }
    if (m_prerequisites != null)  {
      try {
        s_writer.write(leadingBlanks+"Prerequisites:" + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
        return;
      }
      s_nIndent++;
      //for (int i = 0; i < m_prerequisites.length; i++) {
      for (Prerequisite prereq: m_prerequisites) {
        prereq.accept(this, activity, cxt);
      }
      m_prerequisites = null;  
      s_nIndent--;
    }
    // Similar for PrescribedResults
    if (m_results != null) {
      try {
        s_writer.write(leadingBlanks+"RequiredInputs:" + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
        return;
      }
      s_nIndent++;
//      for (int i=0; i < m_results.length; i++) {
      for (PrescribedResult res: m_results) {
        res.accept(this, activity, cxt);
      }
      m_results = null;
      s_nIndent--;
    }
    // Children are more complicated.  Will need to instantiate
    // new TravelerPrintVisitor so we don't lose context
    if (m_children != null)  {
      try {
        s_writer.write(leadingBlanks + m_substeps + s_eol);
      } catch (IOException ex) {
        System.out.println("whoops!  " + ex.getMessage());
        return;
      }
      s_nIndent++;
      TravelerVisitor childVisitor = new TravelerPrintVisitor();
      for (ProcessNode child: m_children) {
          //(int i=0; i < m_children.size(); i++) {
        child.accept(childVisitor, activity, cxt);
        //childVisitor.visit(m_children[i]);
      }
      s_nIndent--;
    }
    
 
  }
  public void visit(Prerequisite prerequisite, String activity, Object cxt) {
    // Gets prereq info, prints it, using static values s_indent, s_writer
    resetPrereq();
    prerequisite.exportTo(this);
    String leadingBlanks="";
    for (int i = 0; i < s_nIndent; i++) {
      leadingBlanks += s_indent;
    }
    try {
      s_writer.write(leadingBlanks + "*" + s_eol);
      leadingBlanks += s_indent;
      s_writer.write(leadingBlanks + "Prereq name: " + m_prereqName + s_eol);
      s_writer.write(leadingBlanks + "Prereq type: " + m_prereqType + s_eol);
    } catch (IOException ex) {
      System.out.println("Exception while writing prerequisite:");
      System.out.println(ex.getMessage());
    }
  }
  public void visit(PrescribedResult result, String activity, Object cxt) {
    // prints out prescribed result info, using s_indent, s_writer
    resetResult();
    result.exportTo(this);
        String leadingBlanks="";
    for (int i = 0; i < s_nIndent; i++) {
      leadingBlanks += s_indent;
    }
    try {
      s_writer.write(leadingBlanks + "*" + s_eol);
      leadingBlanks += s_indent;
      s_writer.write(leadingBlanks + "Result label: " + m_label + s_eol);
      s_writer.write(leadingBlanks + "Result semantics: " + m_semantics + s_eol);      s_writer.write(leadingBlanks + "Result units: " + m_units + s_eol);
      s_writer.write(leadingBlanks + "Result description: " + m_description + s_eol);
      s_writer.write(leadingBlanks + "Result min value: " + m_minValue + s_eol);
      s_writer.write(leadingBlanks + "Result max value: " + m_maxValue + s_eol);
    } catch (IOException ex) {
      System.out.println("Exception while writing prescribed result:");
      System.out.println(ex.getMessage());
    }
  
  }
 // public static void setFileWriter(FileWriter writer)  {s_writer = writer;}
  public static void setWriter(Writer writer)  {s_writer = writer;}
  private void resetProcessScalars() {
    m_id=null; m_name=null; m_hardwareType=null; m_hardwareRelationshipType=null;
    m_version=null; m_userVersionString=null; m_description=null;
    m_maxIteration=null; m_substeps=null; m_travelerActionMask=0;
    m_originalId=null; m_condition=null; 
  }
  
  private void resetPrereq() {
    m_prereqType=null;  m_prereqName=null; m_prereqProcessVersion=null;
    m_prereqProcessUserVersionString=null; m_prereqQuantity=1;
    m_prereqDescription=null;
  }
  private void resetResult() {
    m_label=null; m_units=null; m_minValue=null; m_maxValue=null;
    m_resultDescription=null; m_semantics=null; m_choiceField=null;
  }
  // Implementation of ProcessNode.ExportTarget
  public void acceptId(String id) {m_id = id;}
  public void acceptName(String name) {m_name = name;}
  public void acceptHardwareType(String hardwareType ) {m_hardwareType  = hardwareType ;}
  public void acceptHardwareRelationshipType(String hardwareRelationshipType ) {
    m_hardwareRelationshipType  = hardwareRelationshipType;
  }
  public void acceptVersion(String version) {m_version = version;}
  public void acceptUserVersionString(String userVersionString) {
    m_userVersionString = userVersionString;}
  public void acceptDescription(String description) {m_description = description;}
  public void acceptInstructionsURL(String url) {
     m_instructionsURL = url;
   }
  public void acceptMaxIteration(String maxIteration) {
    m_maxIteration = maxIteration;}
  public void acceptSubsteps(String substeps) {m_substeps = substeps;}
  public void acceptTravelerActionMask(int travelerActionMask) {
    m_travelerActionMask = travelerActionMask;}
  public void acceptOriginalId(String originalId) {m_originalId = originalId;}
  public void acceptCondition(String condition) {m_condition=condition;}
  public void acceptChildren(ArrayList<ProcessNode> children) {m_children=children;}
  public void acceptPrerequisites(ArrayList<Prerequisite> prereqs) {
    m_prerequisites=prereqs;
  }
  public void acceptPrescribedResults(ArrayList<PrescribedResult> res) {
    m_results=res;
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
 
  // Implementation of PrescribedResult.ExportTarget
   public void acceptLabel(String label) {
     m_label = label;
   }
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
   public void acceptResultDescription(String description) {
     m_resultDescription = description;
   }
 
   
   public void acceptChoiceField(String choiceField)  {
     m_choiceField = choiceField;
   }
   public void acceptEdited(boolean edited) {}
   public void exportDone() { }
  
  // Store process contents until we're ready to write
  private String m_id=null;
  private String m_name=null;
  private String m_hardwareType=null;
  private String m_hardwareRelationshipType=null;
  private String m_version=null;
  private String m_userVersionString=null;
  private String m_description=null;
  private String m_instructionsURL=null;
  private String m_maxIteration=null;
  private String m_substeps=null;
  private String m_condition=null;
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
  
  // Store prereq. contents until we're ready to write
  private String m_prereqType=null;
  private String m_prereqName=null;
  private String m_prereqProcessVersion=null;
  private String m_prereqProcessUserVersionString=null;
  private int    m_prereqQuantity=1;
  private String m_prereqDescription=null;
  
  // Store prescribed result contents until we're ready to write
  private String m_label=null;
  private String m_units=null;
  private String m_semantics=null;
  private String m_minValue=null;
  private String m_maxValue=null;
  private String m_resultDescription=null;
  private String m_choiceField=null;
  
  private static String s_indent="  ";
  private static int s_nIndent = 0;
  // private static FileWriter s_writer=null;
  private static Writer s_writer=null;
  
}
