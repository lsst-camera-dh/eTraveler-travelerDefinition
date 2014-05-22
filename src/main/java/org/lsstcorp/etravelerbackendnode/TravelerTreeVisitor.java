/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.net.URLEncoder;
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
  
  public void setTreeRenderer(Tree renderer) {
    m_treeRenderer = renderer;
  }
  public Tree getTreeRenderer() {
    return m_treeRenderer;
  }
  public ProcessTreeNode getTreeRoot() {
    return m_treeRoot;
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
        m_treeRenderer.setLeafHref("actionTraveler.jsp?leafSelectedPath=%p");
        m_treeRenderer.setFolderHref("actionTraveler.jsp?folderSelectedPath=%p");
        m_treeRenderer.setTarget("action");
            
        m_treeRenderer.printStyle(outWriter);
        m_treeRenderer.printScript(outWriter);
        context.setAttribute("scriptInclude", Boolean.TRUE, PageContext.PAGE_SCOPE);
      }
      m_treeRenderer.printTree(outWriter, m_treeRoot, "The tree");
    } catch (IOException ex)  {
      System.out.println("Failed to render tree with exception: " + ex.getMessage());
    }
  }
  
  private Tree m_treeRenderer=null;
  private ProcessTreeNode m_treeRoot = null;
  private String m_path=null;
}
