/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node.web;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import org.lsstcorp.etravelerbackend.db.DbConnection;

/**
 * Creates table of traveler types in selected db.
 * If variable ttype_action is non-empty, create links for table
 * entries to page, setting parameters for traveler name and version
 * @author jrb
 */
public class ListTravelerTypes extends SimpleTagSupport {
  public void doTag() throws JspException, IOException {
    PageContext context= (PageContext) getJspContext();
    DbConnection conn = null;
    
    try {
      conn = DbWebUtil.makeConnection(context, true);
    } catch (Exception ex) {
      throw new JspException(ex);
    }
    String where = " where (rootProcessId=Process.id) and (Process.hardwareTypeId=HardwareType.id)" ;
    where += " order by Process.name, Process.version desc";
    String[] cols = {"Process.name", "Process.version", 
      "HardwareType.name as hname", "Process.description", "Process.createdBy",
      "Process.creationTS"};
    PreparedStatement qry = 
        conn.prepareQuery("TravelerType join Process join HardwareType", cols, 
        where);
    ResultSet ttypes;
    try {
      ttypes = qry.executeQuery();
    } catch (SQLException ex) {
      throw new JspException(ex);
    }
    
    if (!generateTravelerTable(ttypes, (context.findAttribute("ttype_action")).toString(), 
        context.getOut())) {
      
    }
    conn.close();
  }
  private boolean generateTravelerTable(ResultSet r, String href, JspWriter wrt ) 
      throws JspException, IOException {
  
    
    try {
      r.next();
      wrt.println("<table class='datatable'><thead><tr>");
      wrt.println("<th sortable='true'>Name</th><th>Version</th><th class='sortable'> Hname</th>");
      wrt.println("<th>Description</th><th class='sortable'>Creator</th>");
      wrt.println("<th class='sortable'>Creation TS</th>");
      wrt.println("<th>View/edit</th><th>Add NCR</th></tr></thead><tbody>");
      boolean isOdd = true;
      while (!r.isAfterLast()) {
        if (isOdd) {
          wrt.println("<tr class='odd'>");
        } else {
          wrt.println("<tr class='even'>");
        }
        isOdd = !isOdd;
        String hrefargs="?traveler_name=" + r.getString("Process.name") 
            + "&traveler_version=" + r.getString("Process.version");
        if (href.isEmpty()) {
          wrt.println("<td style='text-align:left'>" + r.getString("Process.name"));
        }  else { 
          wrt.println("<td style='text-align:left'y><a href='" + href + hrefargs
              + "'>" + r.getString("Process.name") + "</a>");
        }
        wrt.println("</td><td style='text-align:right'>" + r.getString("Process.version")
            +"</td><td style='text-align:left'>" +r.getString("hname")
            + "</td><td style='text-align:left'>" + r.getString("Process.description") 
            + "</td><td style='text-align:left'>" + r.getString("Process.createdBy") 
            + "</td><td>" + r.getString("creationTS") + "</td><td>");
        wrt.println("<a href='editTraveler.jsp" + hrefargs + "'>View/edit</a></td><td>");
        wrt.println("<a href='addNCR.jsp" + hrefargs + "'>add NCR</a></td></tr>");
        r.next();
      }
      wrt.println("</tbody></table>");
    } catch (SQLException ex) {
      throw new JspException(ex);
    }
    
    return true;
  }
   private boolean generateTravelerTable2(ResultSet r, String href, JspWriter wrt ) 
      throws JspException, IOException {
     
     
     return true;
   }
}
