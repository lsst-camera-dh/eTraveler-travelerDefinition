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
import org.freehep.webutil.tree.DefaultIconSet;
import org.freehep.webutil.tree.Tree;
import org.freehep.webutil.tree.TreeNode;


/**
 * Create text file (or byte stream) for input to GraphViz
 * 
 * (Maybe also provide services to invoke GraphViz?)
 * 
 * @author jrb
 */
public class TravelerTreeVisitor implements TravelerVisitor { 
  /*    ProcessNode.ExportTarget, Prerequisite.ExportTarget, PrescribedResult.ExportTarget {   */
  
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
      m_treeRoot = treeNode;
    }
    process.exportTo(treeNode);
    
    
    /*
     * Don't think we need this.  Recursion is done inside ProcessTreeNode.
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
      for (int i=0; i < m_children.length; i++) {
        m_children[i].accept(childVisitor, activity);
        m_dotWriter.write(m_indent +"\"" + m_name + "\"->\"" + m_children[i].getName() + "\" ");
        if (seq) {
          m_dotWriter.write(edgeAtts + String.valueOf(i+1));
        } else {
          m_dotWriter.write(edgeAtts + m_children[i].getCondition());
        }
        m_dotWriter.write("\"]" + m_eol);
      }    
    }  catch (IOException ex) {
      throw new EtravelerException("Failed to write edge: " + ex.getMessage());
    } 
    */
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
        m_treeRenderer.printStyle(outWriter);
        m_treeRenderer.printScript(outWriter);
        context.setAttribute("scriptInclude", Boolean.TRUE, PageContext.PAGE_SCOPE);
      }
      m_treeRenderer.printTree(outWriter, m_treeRoot, "The tree");
    } catch (IOException ex)  {
      System.out.println("Failed to render tree with exception: " + ex.getMessage());
    }
  }
  
  // Write text to be input to GraphViz
  private Tree m_treeRenderer=null;
  private ProcessTreeNode m_treeRoot = null;
  private String m_path=null;
}
