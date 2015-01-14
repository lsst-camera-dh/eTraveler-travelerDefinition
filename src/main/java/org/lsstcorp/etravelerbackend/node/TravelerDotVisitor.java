/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Create text file (or byte stream) for input to GraphViz
 * 
 * (Maybe also provide services to invoke GraphViz?)
 * 
 * @author jrb
 */
public class TravelerDotVisitor implements TravelerVisitor, 
    ProcessNode.ExportTarget, Prerequisite.ExportTarget, PrescribedResult.ExportTarget { 
  
  public void setDotWriter(Writer writer) {
    m_dotWriter = writer;
  }
  public void setIndentEol(String indent, String eol) {
    m_indent = indent;
    m_eol = eol;
  }
  public void initOutput(Writer writer, String eol) throws EtravelerException {
    m_eol = eol;
    if (writer != null) {
      setDotWriter(writer);
    }
    else {
      setDotWriter(new StringWriter());
    }
    try {
      m_dotWriter.write(m_eol + "digraph Traveler {" + m_eol);
      m_dotWriter.write(m_indent + "node [fontsize=10]" + m_eol);
            m_dotWriter.write(m_indent + "edge [fontsize=10]" + m_eol);
      m_dotWriter.write(m_indent + "node [height=0.3]" + m_eol);
    } catch (IOException ex) {
      throw new EtravelerException("Failed to init dot ouput: " +ex.getMessage());
    }
  }
  
  public void endOutput() throws EtravelerException {
    if (m_dotWriter == null)  {
      throw new EtravelerException("Cannot end output on uninitialized dot visitor");
    }
    try {
      m_dotWriter.write("}" + m_eol);
    } catch (IOException ex) {
      throw new EtravelerException("Failed to close dot ouput: " +ex.getMessage());
    }
  }
  public Writer getDotWriter() {
    return m_dotWriter;
  }
  static String s_edgeDefault="edge [color=black style=solid]";
  
  // Implementation of TravelerVisitor
  public void visit(ProcessNode process, String activity, Object cxt) throws EtravelerException {
    if (m_dotWriter == null) {
      m_dotWriter = new StringWriter();
    }
    process.exportTo(this);
    // Write out our node
    // Hoping that DisplayProcess.jsp  will have root traveler name, version and db
    // in page context
    try {
      String encodedName = URLEncoder.encode(m_name, "UTF-8");
      m_dotWriter.write(m_indent + "\""+ m_name + "\" [URL=\"DisplayProcess.jsp?process="
          + encodedName + "&version=" + m_version +  
          "\" tooltip=\"" + m_name + "\" ]" + m_eol);
  
    } catch (IOException ex) {
      throw new EtravelerException("Failed to write node to dot file: "+ex.getMessage());
    }
    
    if (m_substeps.equals("NONE")) return;
  
    String edgeAtts = " [color=black style=solid label=\"";
    boolean seq = true;
    if (m_substeps.equals("SELECTION"))  {
      seq = false;
      edgeAtts = " [color=magenta style=bold label=\"";
    }
    try {
      TravelerDotVisitor childVisitor = new TravelerDotVisitor();
      childVisitor.setIndentEol(m_indent + "  ", m_eol);
      childVisitor.setDotWriter(m_dotWriter);
      for (int i=0; i < m_children.size(); i++) {
        m_children.get(i).accept(childVisitor, activity, cxt);
        m_dotWriter.write(m_indent +"\"" + m_name + "\"->\"" + m_children.get(i).getName() + "\" ");
        if (seq) {
          m_dotWriter.write(edgeAtts + String.valueOf(i+1));
        } else {
          m_dotWriter.write(edgeAtts + m_children.get(i).getCondition());
        }
        m_dotWriter.write("\"]" + m_eol);
      }    
    }  catch (IOException ex) {
      throw new EtravelerException("Failed to write edge: " + ex.getMessage());
    } 
    
       //   For now ignore prereqs and results
    // If children
    //   Create new travelever visitor; set output stream to ours
    //   Recurse through children.  
    //    After each child, draw edge from us to it.  Maybe differentiate
    //    between selection / sequence children with color or line style
   
  }
  // For the time being ignore prerequisites and results; just draw nodes & edges
  public void visit(PrescribedResult result, String activity, Object cxt) 
      throws EtravelerException {
  }
  public void visit(Prerequisite prerequisite, String activity, Object cxt) 
      throws EtravelerException {
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
  public void acceptInstructionsURL(String url) {m_instructionsURL = url;}
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
  public void acceptHasClones(boolean hasClones) {
    m_hasClones = hasClones;
  }
  // Dot visitor never deals with ref nodes
  public void acceptIsRef(boolean isRef) {}  
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
  public void exportDone() {}
  public String getName() {return m_name;}
  
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
  private String m_originalId=null;
  private ProcessNode m_clonedFrom = null;
  private boolean m_isCloned = false;
  private boolean m_hasClones = false;
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
  
  // Write text to be input to GraphViz
  private Writer m_dotWriter=null;
  private String m_indent="  ";
  private String m_eol="";
}