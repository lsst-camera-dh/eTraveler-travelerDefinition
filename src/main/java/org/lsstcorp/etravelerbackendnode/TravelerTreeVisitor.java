/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
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
  public TravelerTreeVisitor(boolean editable) {
    m_editable = editable;
    if (editable) m_editedNodes = new HashMap<ProcessTreeNode, String>();
  }
  
  public TravelerTreeVisitor(boolean editable, String title) {
    m_editable = editable;
    m_title=title;
    if (editable) m_editedNodes = new HashMap<ProcessTreeNode, String>();
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
    try {
      if (context.getAttribute("scriptIncluded", PageContext.PAGE_SCOPE) == null) {
        if (m_editable)  {
          m_treeRenderer.setLeafHref("actionTraveler.jsp?leafSelectedPath=%p");
          m_treeRenderer.setFolderHref("actionTraveler.jsp?folderSelectedPath=%p");
          m_treeRenderer.setTarget("action");
             
        } else {
          m_treeRenderer.setLeafHref("processAction.jsp?nodePath=%p&action=DisplayOrig");
          m_treeRenderer.setFolderHref("processAction.jsp?nodePath=%p&action=DisplayOrig");
        }
        // m_treeRenderer.setTarget("action");
            
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
  
  public AttributeList getEdited() {
    AttributeList edited = new AttributeList();
    
    Set<ProcessTreeNode> nodes = m_editedNodes.keySet();
    for (ProcessTreeNode node: nodes) {
      edited.add(new Attribute(node.getPath(), m_editedNodes.get(node)));
    }
    return edited;
  }
  
  public boolean clearModified() {
    if (!m_editable) return false;
    m_editedNodes.clear();
    return true;
  }
  
  private Tree m_treeRenderer=null;
  private ProcessTreeNode m_treeRoot = null;
  private String m_path=null;
  private boolean m_editable=false; 
  private String m_title="The tree";
  private HashMap <ProcessTreeNode, String> m_editedNodes=null;
}
