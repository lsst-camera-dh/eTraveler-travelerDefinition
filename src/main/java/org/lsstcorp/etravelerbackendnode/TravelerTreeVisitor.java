/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackendexceptions.EtravelerException;
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
  public TravelerTreeVisitor(boolean editable) {
    m_editable = editable;
    if (editable)  {
      m_reason = "edit";
    } else {
      m_reason = "display";
    }
    if (editable) m_editedNodes = new HashMap<ProcessTreeNode, String>();
  }
  
  public TravelerTreeVisitor(boolean editable, String reason) {
    m_editable = editable;
    m_reason = reason;
    if (editable) m_editedNodes = new HashMap<ProcessTreeNode, String>();
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
  public void visit(ProcessNode process, String activity) throws EtravelerException {
    
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
  // For the time being ignore prerequisites and results; just draw nodes & edges
  public void visit(PrescribedResult result, String activity) throws EtravelerException {
  }
  public void visit(Prerequisite prerequisite, String activity) throws EtravelerException {
  }
  public void render(PageContext context) {
    JspWriter outWriter = context.getOut();
    String href = "actionTraveler.jsp?action=" + m_reason;
    try {
      if (context.getAttribute("scriptIncluded", PageContext.PAGE_SCOPE) == null) {

        m_treeRenderer.setLeafHref(href + "&leafSelectedPath=%p");
        m_treeRenderer.setFolderHref(href + "&folderSelectedPath=%p");
        //m_treeRenderer.setLeafHref("actionTraveler.jsp?leafSelectedPath=%p");
        //m_treeRenderer.setFolderHref("actionTraveler.jsp?folderSelectedPath=%p");
        if (m_editable)  {
          m_treeRenderer.setTarget("action");
             
        }  //else {
          //m_treeRenderer.setLeafHref("processAction.jsp?nodePath=%p&action=DisplayOrig");
          //m_treeRenderer.setFolderHref("processAction.jsp?nodePath=%p&action=DisplayOrig");
        //}
        // m_treeRenderer.setTarget("action");
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
  public boolean addEdited(ProcessTreeNode node, String how)  {
    if (!m_editable) return false;
   
    m_editedNodes.put(node, how);
    return true;
  }
  public boolean undoEdited(String path) {
    if (!m_editable) return false;
    ProcessTreeNode theNode = null;
    boolean ok = false;
    Set<ProcessTreeNode> nodes = m_editedNodes.keySet();
    for (ProcessTreeNode node: nodes) {
      if (node.getPath().equals(path)) {
        theNode=node;
        ok = theNode.getProcessNode().recover(false);
        if (ok) m_editedNodes.remove(theNode);
        return ok;
      }
    }
    return false;
  }
  
  public ArrayList<EditedTreeNode> getEdited() {
    ArrayList<EditedTreeNode> edited = new ArrayList<EditedTreeNode>();
    
    Set<ProcessTreeNode> nodes = m_editedNodes.keySet();
    for (ProcessTreeNode node: nodes) {
      EditedTreeNode e = new EditedTreeNode(node.getPath(), m_editedNodes.get(node));
      edited.add(e);
    }
    return edited;
  }
  
  public int getNEdited() {
    if (m_editedNodes == null) return 0;
    return m_editedNodes.size();
  }
  
  public boolean clearModified() {
    if (!m_editable) return false;
    /* Really should be
     *    for node in HashMap
     *        restore
     *        remove from HashMap
     */
    m_editedNodes.clear();
    return true;
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
  private HashMap <ProcessTreeNode, String> m_editedNodes=null;
}
