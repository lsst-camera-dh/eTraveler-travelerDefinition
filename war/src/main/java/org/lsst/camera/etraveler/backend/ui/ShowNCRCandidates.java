package org.lsst.camera.etraveler.backend.ui;

import org.lsst.camera.etraveler.backend.ui.DbWebUtil;
import java.io.IOException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import org.lsst.camera.etraveler.backend.node.ProcessNode;
import org.lsst.camera.etraveler.backend.ui.TravelerTreeVisitor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// import java.sql.Statement;

/**
 *  Fetch process name, version and id for all travelers of same hardware
 * type as current traveler.  These are NCR traveler candidates.
 * @author jrb
 */
public class ShowNCRCandidates extends SimpleTagSupport {
  public void doTag() throws JspException, IOException {
    PageContext context= (PageContext) getJspContext();
    DbConnection conn = null;
    TravelerTreeVisitor vis = (TravelerTreeVisitor) 
        context.getSession().getAttribute("treeVisitor");
    ProcessNode baseTraveler = vis.getTravelerRoot(); 
    try {
      conn = DbWebUtil.makeConnection(context, true);
    } catch (Exception ex) {
      throw new JspException(ex);
    }
    String where = " where name='" + baseTraveler.getHardwareGroup() + "'";
    String hgroupId = conn.fetchColumn("HardwareGroup", "id", where);
    if (hgroupId == null) {
      throw new JspException("Unknown base traveler hardware group");
    }
    String[] cols = {"Process.name", "Process.version", "Process.id"};
    PreparedStatement qry = 
        conn.prepareQuery("TravelerType join Process", cols, 
        " where rootProcessId=Process.id and hardwareGroupId=" + hgroupId);
    ResultSet candidates;
    try {
      candidates = qry.executeQuery();
    } catch (SQLException ex) {
      throw new JspException(ex);
    }
    
    if (!generateSelection(candidates, baseTraveler.getProcessId(), context.getOut())) {
      
    }
    conn.close();
  }
  private boolean generateSelection(ResultSet r, String processId, JspWriter wrt)
      throws JspException, IOException {
    String optStart = "<option value='";
    String optMid = "'>";
    String optEnd = "</option>";
    int nCandidates = 0;
  
    try {
      r.next();
      wrt.println("<tr>");
      wrt.println("<td><label for='ncrTraveler'><b>NCR traveler:</b></label></td> ");
      wrt.println("<td><select name='ncrTraveler'>");
      while (!r.isAfterLast()) {
        String label = r.getString("Process.name") + " v" + r.getString("Process.version");
        String value = r.getString("Process.id");
        if (!value.equals(processId)) {
          wrt.println(optStart + value + optMid + label + optEnd);
          nCandidates++;
        }
        r.next();
      }
      wrt.println("</select></td>");
    } catch (SQLException ex) {
      throw new JspException(ex);
    }
    if (nCandidates == 0)  {
      wrt.println("<tr><td colspan='2'>");
      wrt.println("<p class='redError'>No candidate NCR travelers found!");
      wrt.println("Create appropriate NCR procedure, then try again.</p></td></tr>");
      return false;
    }
    return true;
  } 
}
