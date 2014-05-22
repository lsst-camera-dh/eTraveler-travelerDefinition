package org.lsstcorp.etravelerbackendnode;

import org.freehep.webutil.tree.DefaultTreeNode; // freeheptree.DefaultTreeNode;

public class ProcessTreeNode extends DefaultTreeNode 
  implements ProcessNode.Wrapper {


  ProcessTreeNode(TravelerTreeVisitor vis, ProcessNode processNode, 
                  ProcessTreeNode treeParent) {
    super(processNode.getName(), treeParent);
 
    m_vis = vis;
    m_processNode = processNode;
  }

  public ProcessNode getProcessNode() { return m_processNode;}

  private ProcessTreeNode m_treeParent=null;

  /* Implementation of ProcessNode.ExportTarget
     Is there any good reason for storing all this stuff?
     Could probably turn most of the accept.. methods into no-ops
  */
 
  public void acceptName(String name) {
    //m_name = name;
    setLabel(name);
  }
  public void acceptChildren(ProcessNode[] children) {
    // m_children=children;
    if (children == null) return;

    // Do recursion here
    if (children.length > 0) {
      m_treeChildren = new ProcessTreeNode[children.length];
      for (int i = 0; i < children.length; i++) {
        /* int edgeStep = i +1;
        if (m_substeps.equals("SELECTION")) { edgeStep = -edgeStep; } */
        // m_treeChildren[i] = new ProcessTreeNode(m_vis, this, edgeStep);
        m_treeChildren[i] = new ProcessTreeNode(m_vis, children[i], this);
        children[i].exportToWrapper(m_treeChildren[i]);
      }
    }
  }
  /*
  public void acceptId(String id) {}
  public void acceptHardwareType(String hardwareType ) {}
  public void acceptHardwareRelationshipType(String hardwareRelationshipType ) {
    //m_hardwareRelationshipType  = hardwareRelationshipType;
  }
  public void acceptVersion(String version) {}
  public void acceptUserVersionString(String userVersionString) {}
//    m_userVersionString = userVersionString;}
  public void acceptDescription(String description) {}
  public void acceptInstructionsURL(String url) {}
  public void acceptMaxIteration(String maxIteration) {}
  //  m_maxIteration = maxIteration;}
  public void acceptSubsteps(String substeps) {m_substeps = substeps;}
  public void acceptTravelerActionMask(int travelerActionMask) {}
   // m_travelerActionMask = travelerActionMask;}
  public void acceptOriginalId(String originalId) {}
  public void acceptCondition(String condition) {}
    //m_condition=condition;}

  public void acceptPrerequisites(Prerequisite[] prereqs) {
    //m_prerequisites=prereqs;
  }
  public void acceptPrescribedResults(PrescribedResult[] res) {
    //m_results=res;
  }
  // Implementation of Prerequisite.ExportTarget
  public void acceptPrerequisiteType(String prerequisiteType) {
  //  m_prereqType = prerequisiteType;
  }
  // In expected use don't need to identify parent process
  public void acceptPrereqName(String name)  {
    // m_prereqName = name; 
  }
  public void acceptPrereqProcessVersion(String version) {
    //m_prereqProcessVersion = version; 
  }
  public void acceptPrereqProcessUserVersionString(String userVersionString) {
    //m_prereqProcessUserVersionString = userVersionString;
  }
  public void acceptPrereqQuantity(int quantity) {
   // m_prereqQuantity=quantity;
  }
  public void acceptPrereqDescription(String description) {
    // m_prereqDescription = description;
  }
  public void acceptClonedFrom(ProcessNode clonedFrom) {
    // m_clonedFrom = clonedFrom;
  }
  public void acceptIsCloned(boolean isCloned)  {
    // m_isCloned = isCloned;
  }
  public void acceptIsRef(boolean isRef) {}
  public void acceptPrereqParent(ProcessNode process) { }
 
  
  public void acceptPrereqId(String prereqId) {}
 
  // Implementation of PrescribedResult.ExportTarget
  public void acceptLabel(String label) {
    // m_label = label;
  }
  public void acceptSemantics(String semantics) {
    // m_semantics = semantics;
  }
  public void acceptUnits(String units) {
    // m_units=units;
  }
  public void acceptMinValue(String minValue) {
   // m_minValue = minValue;
  }
  public void acceptMaxValue(String maxValue) {
    // m_maxValue = maxValue;
  }
  public void acceptResultDescription(String description) {}
    // m_resultDescription = description;
 
  
  public void acceptChoiceField(String choiceField)  {}
    // m_choiceField = choiceField;
    
    */
  
  public void exportDone() {
    // invoke setHref
    // invoke setTarget
  }
  public String getName() {return m_processNode.getName();}
  
  public boolean isSelected() {return m_selected;}
  private String m_substeps=null;
  
  /* Don't think we need these.  Info is in assoc. ProcessNode
  private String m_id=null;
  private String m_name=null;
  private String m_hardwareType=null;
  private String m_hardwareRelationshipType=null;
  private String m_version=null;
  private String m_userVersionString=null;
  private String m_description=null;
  private String m_instructionsURL=null;
  private String m_maxIteration=null;

  private String m_condition=null;
  private int m_travelerActionMask=0;
  private String m_originalId=null;
  private ProcessNode m_clonedFrom = null;
  private boolean m_isCloned = false;
  private ProcessNode[] m_children=null;
  private Prerequisite[] m_prerequisites=null;
  private PrescribedResult[] m_results=null;
  
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
  */

  private TravelerTreeVisitor m_vis=null;
  /*
   * m_processNode is ProcessNode from which this ProcessTreeNode is derived
   */
  private ProcessNode m_processNode=null;
  private ProcessTreeNode[] m_treeChildren=null;
  private int m_edgeStep = 0;
  private String m_edgeCondition = null;
  private boolean m_selected = false;
}