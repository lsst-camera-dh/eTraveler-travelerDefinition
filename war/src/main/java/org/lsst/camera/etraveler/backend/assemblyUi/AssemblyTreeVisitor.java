/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.assemblyUi;

// Implementation in progress.  Code in this package doesn't yet
// compile, but it's not used anywhere 

import org.lsst.camera.etraveler.backend.ui.ProcessTreeNode;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import org.freehep.webutil.tree.DefaultIconSet; // freeheptree.DefaultIconSet;
import org.freehep.webutil.tree.Tree; // freeheptree.Tree;  
//import org.lsst.camera.etraveler.backend.ui.EditedTreeNode;
import org.lsst.camera.etraveler.backend.hnode.HardwareTypeNode;
import org.lsst.camera.etraveler.backend.hnode.AssemblyTemplate;
import org.lsst.camera.etraveler.backend.node.Traveler;
import org.lsst.camera.etraveler.backend.hnode.AssemblyVisitor;

/**
 * @author jrb
 */
public class AssemblyTreeVisitor implements AssemblyVisitor { 

  public AssemblyTreeVisitor(boolean editable) throws EtravelerException {
    if (editable) {
      throw
        new EtravelerException("AssemblyTreeVisitor: editing not supported");
    }
    m_reason = "display";
  }
  
  public AssemblyTreeVisitor(String reason) throws
  EtravelerException {
    m_reason = reason;
  }
  
  public String getReason() {return m_reason;}
  
  public void setTitle(String title)  {
    m_title = title;
  }
  public void setTreeRenderer(Tree renderer) {
    m_treeRenderer = renderer;
  }
  public Tree getTreeRenderer() {
    return m_treeRenderer;
  }
  public HardwareTreeNode getTreeRoot() {
    return m_treeRoot;
  }
  public HardwareTypeNode getAssemblyRoot() {
    return m_treeRoot.getHardwareTypeNode();
  }
  public AssemblyTemplate getAssemblyTemplate() { return m_template; }
  public void setAssemblyTemplate(AssemblyTemplate t) {
    m_template=t;
    /*
    if (t.getSubsystem() != null) {
      m_title="The tree, subsystem=" + trav.getSubsystem();
    }
    */
  }
  
  public void setPath(String path) {
    m_path = path;
  }
  /**
   * If created for editing, we'll want to save root of the original
   * traveler our traveler was copied from
   * @param original 
   
  public void setCopiedFrom(HardwareTypeNode original) {
    m_original = original;
  }
  */
  public HardwareTreeNode findNodeFromPath(String path, String treeNodeId) {
    if (m_treeRoot == null) return null;  /* not built yet */

    int secondSlash = path.indexOf("/", 1);
    if (secondSlash == -1) return m_treeRoot;
    String nodePath = path;
    /* Otherwise strip off root path at the front */
    nodePath = nodePath.substring(secondSlash);
    HardwareTreeNode guess = (HardwareTreeNode) m_treeRoot.findNode(nodePath, false);
    /* The only way guess can be wrong is if there are at least two siblings with the same
     * name.  See if we've got the right one by looking at treeNodeId field
     */
    return guess.findSibling(treeNodeId);
  }

  // Implementation of AssemblyVisitor
  public void visit(HardwareTypeNode hnode, String activity, Object cxt) throws EtravelerException {
    
    HardwareTreeNode treeNode = new HardwareTreeNode(this, hnode, null);
    if (m_treeRenderer == null) {
      if (m_path == null) {
        m_treeRenderer = new Tree(new DefaultIconSet());
      } else {
        m_treeRenderer = new Tree(m_path);
      }
      //m_treeRenderer.setRootVisible(false);
      m_treeRoot = treeNode;
    }
    hnode.exportToWrapper(treeNode);   
   
    // If children
    //   Create new assembly visitor; set output stream to ours
    //   Recurse through children.  
    //    After each child, draw edge from us to it.  
  }
 
  public void render(PageContext context) {
    JspWriter outWriter = context.getOut();
    String href = "actionTraveler.jsp?action=" + m_reason;
    try {
      if (context.getAttribute("scriptIncluded", PageContext.PAGE_SCOPE) == null) {

        m_treeRenderer.setLeafHref(href + "&leafSelectedPath=%p");
        m_treeRenderer.setFolderHref(href + "&folderSelectedPath=%p");

        if (m_reason.equals("NCR")) {
          m_treeRenderer.setTarget("NCR");
        }
            
        m_treeRenderer.printStyle(outWriter);
        m_treeRenderer.printScript(outWriter);
        context.setAttribute("scriptInclude", Boolean.TRUE, PageContext.PAGE_SCOPE);
      }
      m_treeRenderer.printTree(outWriter, m_treeRoot, m_title);
    } catch (IOException ex)  {
      System.out.println("Failed to render tree with exception: " + ex.getMessage());
    }
  }
 
  
 
  int getCount() {
    m_treeNodeCount++;
    return m_treeNodeCount;
  }
  
  private Tree m_treeRenderer=null;
  private int m_treeNodeCount = 0;
  private HardwareTreeNode m_treeRoot = null;
  private HardwareTypeNode m_original = null;
  private String m_path=null;
  //private boolean m_editable=false; 
  private String m_reason=null;
  private String m_title="The tree";
  private AssemblyTemplate m_template=null;
  // private HashMap <ProcessTreeNode, String> m_editedNodes=null;
}
