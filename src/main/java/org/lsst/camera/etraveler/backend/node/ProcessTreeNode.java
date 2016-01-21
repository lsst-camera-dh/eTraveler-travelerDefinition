package org.lsst.camera.etraveler.backend.node;

import java.util.ArrayList;
import org.freehep.webutil.tree.DefaultTreeNode; // freeheptree.DefaultTreeNode;

public class ProcessTreeNode extends DefaultTreeNode 
  implements ProcessNode.Wrapper {


  ProcessTreeNode(TravelerTreeVisitor vis, ProcessNode processNode, 
                  ProcessTreeNode treeParent) {
    super(processNode.getName(), treeParent);
 
    m_vis = vis;
    m_myId = vis.getCount();
    m_processNode = processNode;
    m_treeParent = treeParent;
  }

  public ProcessNode getProcessNode() { return m_processNode;}



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
      m_treeChildren = new ArrayList<ProcessTreeNode>(children.size());
      for (int i = 0; i < children.size(); i++) {
        /* int edgeStep = i +1;
        if (m_substeps.equals("SELECTION")) { edgeStep = -edgeStep; } */
        // m_treeChildren[i] = new ProcessTreeNode(m_vis, this, edgeStep);
        m_treeChildren.add(new ProcessTreeNode(m_vis, children.get(i), this));
        children.get(i).exportToWrapper(m_treeChildren.get(i));
      }
    }
  }

  
  public void exportDone() {
    // invoke setHref
    // invoke setTarget
  }
  public boolean checkId(String id) {
    return (id.equals(Integer.toString(m_myId) ) );
  }
  public ProcessTreeNode findSibling(String id)  {
    if (checkId(id) ) return this;
    if (m_treeParent != null )  {
      for (ProcessTreeNode child : m_treeParent.m_treeChildren) {
        if (child.checkId(id)) return child;
      }
    }
    return null;
  }
  public String getName() {return m_processNode.getName();}
  
  public boolean isSelected() {return m_selected;}
  public int getMyId() {return m_myId; }
  public String getMyStringId() {return Integer.toString(m_myId);}

  public String processHref(String href) {
    return href + "&treeNodeId=" + Integer.toString(m_myId);
  }
  private String m_substeps=null;
  private TravelerTreeVisitor m_vis=null;
  /*
   * m_processNode is ProcessNode from which this ProcessTreeNode is derived
   */
  private ProcessNode m_processNode=null;
  private ProcessTreeNode m_treeParent=null;
  private ArrayList<ProcessTreeNode> m_treeChildren=null;
  // private int m_edgeStep = 0;
  // private String m_edgeCondition = null;
  private boolean m_selected = false;
  private int m_myId = -1;
}