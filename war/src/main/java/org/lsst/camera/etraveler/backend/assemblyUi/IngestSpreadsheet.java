package org.lsst.camera.etraveler.backend.assemblyUi;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
/*        import javax.servlet.http.HttpSession; */
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
/**
 * Set up session variables depending on selected node in tree
 * @author jrb
 */
public class IngestSpreadsheet extends SimpleTagSupport {
  public void doTag() throws IOException {
    JspContext cxt = getJspContext();
    PageContext page = (PageContext) cxt;
    String redoInstructions = " Reselect file and try again <br />"; 
    /*  
         Should be invoked from a page which asks user for
         input file path, stuffs in parameter "importSpreadsheet"
         Also fileAction parameter (syntax check or also check against db)
     */
    JspWriter wrt = cxt.getOut();
    HttpServletRequest req = (HttpServletRequest) page.getRequest();

    Object fileContentsObj = req.getParameter("importSpreadsheet");

    if (fileContentsObj == null)  {
      wrt.write("No file selected or stale reference. <br /> "
                + redoInstructions);
      return;
    }
    String fileContents = fileContentsObj.toString();
    if (fileContents.isEmpty()) {
      wrt.write("empty file contents string<br />" + redoInstructions);
      return;
    }

    /* Read in the table */
    /*
      Idea is to pass the fileContents string to a method in
      org.lsst.camera.etraveler.backend.hnode.HardwareAssemblyTable
      which can make a Workbook out of the string.  But the apache
      methods making a Workbook take either a File or an InputStream.   
      We'd have to
      rewrite the string to a local file for the first.  Or we can
      make a  StringBufferInputStream out of the string, but this 
      is deprecated.  StringReader is recommended instead, but that
      doesn't quite give us an InputStream.  *sigh*

      Assuming we got past this hurdle, the method in HardwareAssemblyTable
      should make checks for internal consistency and to make sure the
      spreadsheet meets minimal requirements for Assembly template description.
  
      Then, if we're also supposed to validate against db, make sure
      referenced hardware types exist.
     */
    /*
    cxt.removeAttribute("nodePath", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("isLeaf", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("treeNodeId", PageContext.SESSION_SCOPE);
    if (page.getRequest().getParameter("leafSelectedPath") != null) {
      String leafRqst = page.getRequest().getParameter("leafSelectedPath");
      cxt.setAttribute("leafPath", leafRqst, PageContext.SESSION_SCOPE);
      cxt.setAttribute("nodePath", leafRqst, PageContext.SESSION_SCOPE);
      cxt.removeAttribute("folderPath", PageContext.SESSION_SCOPE);
      cxt.setAttribute("isLeaf", "1", PageContext.SESSION_SCOPE);
    }  
    if (page.getRequest().getParameter("folderSelectedPath") != null) {
      //if (cxt.getAttribute("folderSelectedPath", PageContext.REQUEST_SCOPE) != null) {
      String folderRqst = page.getRequest().getParameter("folderSelectedPath");
      cxt.setAttribute("folderPath", folderRqst, PageContext.SESSION_SCOPE);
      cxt.setAttribute("nodePath", folderRqst, PageContext.SESSION_SCOPE);
      cxt.removeAttribute("leafPath", PageContext.SESSION_SCOPE);
      cxt.removeAttribute("isLeaf", PageContext.SESSION_SCOPE);
      cxt.setAttribute("isLeaf", "0", PageContext.SESSION_SCOPE);
    }  
    if (page.getRequest().getParameter("treeNodeId") != null) {
      String treeNodeId = page.getRequest().getParameter("treeNodeId");
      cxt.setAttribute("treeNodeId", treeNodeId, PageContext.SESSION_SCOPE);
    }
    */
  }
}
