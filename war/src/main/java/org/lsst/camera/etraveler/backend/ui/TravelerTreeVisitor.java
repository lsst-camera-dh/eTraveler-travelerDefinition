/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.ui;
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
import org.lsst.camera.etraveler.backend.node.Prerequisite;
import org.lsst.camera.etraveler.backend.node.PrescribedResult;
import org.lsst.camera.etraveler.backend.node.ProcessNode;
import org.lsst.camera.etraveler.backend.node.RelationshipTask;
import org.lsst.camera.etraveler.backend.node.Traveler;
import org.lsst.camera.etraveler.backend.node.TravelerVisitor;

/**
 * Create text file (or byte stream) for input to GraphViz
 * 
 * (Maybe also provide services to invoke GraphViz?)
 * 
 * @author jrb
 */
public class TravelerTreeVisitor implements TravelerVisitor { 
  /**
   * Store vital information about a edited ProcessTreeNode in case
   * we later want to restore. It's a pure data class.
   
  private class TreeNodeEditInfo {
    public ProcessNode m_process;
    public int         m_myId;
    public String      m_editType;
  }
  */
  public TravelerTreeVisitor(boolean editable) throws EtravelerException {
    if (editable) {
      throw
        new EtravelerException("TravelerTreeVisitor: editing not supported");
    }
    m_editable = editable;
    m_reason = "display";
  }
  
  public TravelerTreeVisitor(boolean editable, String reason) throws
  EtravelerException {
    if (editable) {
      throw
        new EtravelerException("TravelerTreeVisitor: editing not supported");
    }
    m_editable = editable;
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
  public ProcessTreeNode getTreeRoot() {
    return m_treeRoot;
  }
  public ProcessNode getTravelerRoot() {
    return m_treeRoot.getProcessNode();
  }
  public Traveler getTraveler() { return m_traveler; }
  public void setTraveler(Traveler trav) {
    m_traveler=trav;
    if (trav.getSubsystem() != null) {
      m_title="The tree, subsystem=" + trav.getSubsystem();
      if (trav.getStandaloneNCR() != null)
        m_title += ", Standalone NCR";
    }
  }
  
  public void setPath(String path) {
    m_path = path;
  }
  /**
   * If created for editing, we'll want to save root of the original
   * traveler our traveler was copied from
   * @param original 
   */
  public void setCopiedFrom(ProcessNode original) {
    m_original = original;
  }

  public ProcessTreeNode findNodeFromPath(String path, String treeNodeId) {
    if (m_treeRoot == null) return null;  /* not built yet */

    int secondSlash = path.indexOf("/", 1);
    if (secondSlash == -1) return m_treeRoot;
    String nodePath = path;
    /* Otherwise strip off root path at the front */
    nodePath = nodePath.substring(secondSlash);
    ProcessTreeNode guess = (ProcessTreeNode) m_treeRoot.findNode(nodePath, false);
    /* The only way guess can be wrong is if there are at least two siblings with the same
     * name.  See if we've got the right one by looking at treeNodeId field
     */
    return guess.findSibling(treeNodeId);
  }

  // Implementation of TravelerVisitor
  public void visit(ProcessNode process, String activity, Object cxt) throws EtravelerException {
    
    ProcessTreeNode treeNode = new ProcessTreeNode(this, process, null);
    if (m_treeRenderer == null) {
      if (m_path == null) {
        m_treeRenderer = new Tree(new DefaultIconSet());
      } else {
        m_treeRenderer = new Tree(m_path);
      }
      //m_treeRenderer.setRootVisible(false);
      m_treeRoot = treeNode;
    }
    process.exportToWrapper(treeNode);   
   
    //   For now ignore prereqs and results
    // If children
    //   Create new travelever visitor; set output stream to ours
    //   Recurse through children.  
    //    After each child, draw edge from us to it.  Maybe differentiate
    //    between selection / sequence children with color or line style
   
  }
  // For the time being ignore prerequisites, results and relationship tasks; 
  // just draw nodes & edges
  public void visit(PrescribedResult result, String activity, Object cxt) 
      throws EtravelerException {
  }
  public void visit(Prerequisite prerequisite, String activity, Object cxt) 
      throws EtravelerException {
  }
  public void visit(RelationshipTask rel, String activity, Object cxt) 
      throws EtravelerException {
  }
  public void render(PageContext context) {
    JspWriter outWriter = context.getOut();
    String href = "actionTraveler.jsp?action=" + m_reason;
    try {
      if (context.getAttribute("scriptIncluded", PageContext.PAGE_SCOPE) == null) {

        m_treeRenderer.setLeafHref(href + "&leafSelectedPath=%p");
        m_treeRenderer.setFolderHref(href + "&folderSelectedPath=%p");

        // if (m_editable)  {
        //   m_treeRenderer.setTarget("action");
             
        // }

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
  private ProcessTreeNode m_treeRoot = null;
  private ProcessNode m_original = null;
  private String m_path=null;
  private boolean m_editable=false; 
  private String m_reason=null;
  private String m_title="The tree";
  private Traveler m_traveler=null;
  // private HashMap <ProcessTreeNode, String> m_editedNodes=null;
}
