package org.lsst.camera.etraveler.backend.assemblyUi;

// Implementation in progress.  Code in this package doesn't yet
// compile, but it's not used anywhere 

import java.util.ArrayList;
import org.freehep.webutil.tree.DefaultTreeNode; // freeheptree.DefaultTreeNode;
import org.lsst.camera.etraveler.backend.hnode.HardwareTypeNode;
import org.lsst.camera.etraveler.backend.assemblyUi.AssemblyTreeVisitor;

public class HardwareTreeNode extends DefaultTreeNode 
  implements HardwareTypeNode.Wrapper {


  HardwareTreeNode(AssemblyTreeVisitor vis, HardwareTypeNode hnode, 
                   HardwareTreeNode treeParent) {
    super(hnode.getName(), treeParent);
 
    m_vis = vis;
    m_myId = vis.getCount();
    m_hnode = hnode;
    m_treeParent = treeParent;
  }

  public HardwareTypeNode getHardwareTypeNode() { return m_hnode;}


  /* Implementation of HardwareTypeNode.ExportTarget
     Is there any good reason for storing all this stuff?
     Could probably turn most of the accept.. methods into no-ops
  */
 
  public void acceptName(String name) {
    setLabel(name);
  }
  public void acceptChildren(ArrayList<HardwareTypeNode> children) {
    if (children == null) return;

    // Do recursion here
    if (children.size() > 0) {
      m_treeChildren = new ArrayList<HardwareTreeNode>(children.size());
      for (int i = 0; i < children.size(); i++) {

        m_treeChildren.add(new HardwareTreeNode(m_vis, children.get(i), this));
        children.get(i).exportToWrapper(m_treeChildren.get(i));
      }
    }
  }

  
  public void exportDone() {

  }
  public boolean checkId(String id) {
    return (id.equals(Integer.toString(m_myId) ) );
  }
  public HardwareTreeNode findSibling(String id)  {
    if (checkId(id) ) return this;
    if (m_treeParent != null )  {
      for (HardwareTreeNode child : m_treeParent.m_treeChildren) {
        if (child.checkId(id)) return child;
      }
    }
    return null;
  }
  public String getName() {return m_hnode.getName();}
  
  public boolean isSelected() {return m_selected;}
  public int getMyId() {return m_myId; }
  public String getMyStringId() {return Integer.toString(m_myId);}

  public String processHref(String href) {
    return href + "&treeNodeId=" + Integer.toString(m_myId);
  }
  private String m_substeps=null;
  private AssemblyTreeVisitor m_vis=null;
  /*
   * m_hnode is HardwareTypeNode from which this ProcessTreeNode is derived
   */
  private HardwareTypeNode m_hnode=null;
  private HardwareTreeNode m_treeParent=null;
  private ArrayList<HardwareTreeNode> m_treeChildren=null;
  private boolean m_selected = false;
  private int m_myId = -1;
}
