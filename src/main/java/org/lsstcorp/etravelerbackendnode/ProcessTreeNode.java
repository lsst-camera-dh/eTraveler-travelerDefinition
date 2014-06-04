package org.lsstcorp.etravelerbackendnode;

import java.util.ArrayList;
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
  public void acceptChildren(ArrayList<ProcessNode> children) {
    // m_children=children;
    if (children == null) return;

    // Do recursion here
    if (children.size() > 0) {
      m_treeChildren = new ProcessTreeNode[children.size()];
      for (int i = 0; i < children.size(); i++) {
        /* int edgeStep = i +1;
        if (m_substeps.equals("SELECTION")) { edgeStep = -edgeStep; } */
        // m_treeChildren[i] = new ProcessTreeNode(m_vis, this, edgeStep);
        m_treeChildren[i] = new ProcessTreeNode(m_vis, children.get(i), this);
        children.get(i).exportToWrapper(m_treeChildren[i]);
      }
    }
  }

  
  public void exportDone() {
    // invoke setHref
    // invoke setTarget
  }
  public String getName() {return m_processNode.getName();}
  
  public boolean isSelected() {return m_selected;}
  private String m_substeps=null;
  


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