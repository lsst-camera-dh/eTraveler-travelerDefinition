package org.lsst.camera.etraveler.backend.node.web;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 *  Clear session variables associated with making a new NCR.  Invoked when
 * user asks to clear form and when different traveler is requested.
 * @author jrb
 */
public class ClearNCRVariables extends SimpleTagSupport {
  public void doTag() {
    JspContext cxt = getJspContext();
    cxt.removeAttribute("nodePath", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("leafPath", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("folderPath", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("isLeaf", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("NCRCondition", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("exitStep", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("returnStep", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("exitOrReturn", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("exitTreeNodeId", PageContext.SESSION_SCOPE);
    cxt.removeAttribute("returnTreeNodeId", PageContext.SESSION_SCOPE);
  }
}
