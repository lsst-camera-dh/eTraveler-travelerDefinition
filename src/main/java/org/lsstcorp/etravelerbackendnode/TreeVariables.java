/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Set up session variables depending on selected node in tree
 * @author jrb
 */
public class TreeVariables extends SimpleTagSupport {
  public void doTag() {
    JspContext cxt = getJspContext();
    PageContext page = (PageContext) cxt;
    cxt.removeAttribute("nodePath", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("isLeaf", PageContext.SESSION_SCOPE);
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
  }
}
