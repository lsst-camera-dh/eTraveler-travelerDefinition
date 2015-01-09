/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.rest.client;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
// import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.lsstcorp.etravelerbackend.db.DbConnection;
import org.lsstcorp.etravelerbackend.db.MysqlDbConnection;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;
/**
 *
 * @author jrb
 */
public class ListHarnessed  {
  public static RowSetDynaClass getHarnessed(PageContext context) 
throws JspException  {
    DbConnection conn = null;
    
    try {
      conn = makeConnection(context, true);
    } catch (Exception ex) {
      throw new JspException(ex);
    }
    // may need to add arg. for session scope
   
    String htype = (String) context.getRequest().getAttribute("htype");
    if ((htype == null) || (htype.isEmpty()) ) htype = "CCD";
    // Cheating for now to avoid another join
    String htid = "1";
    
    String where = " where Process.hardwareTypeId='" + htid + "' and ";
    where += " Process.id = Activity.processId and ";
    where += " (Process.travelerActionMask & 1) != 0 ";
    where += " order by Process.name, Activity.id desc";
    String[] cols={"Activity.id as id", "Process.name as jobname", 
      "userVersionString as userVersion",
      "closedBy", "end as endTS"};
    
    PreparedStatement qry = conn.prepareQuery("Process join Activity", cols, where);
    ResultSet acts;
    RowSetDynaClass rowSetDyna=null;
    try {
      acts = qry.executeQuery();
      rowSetDyna = new RowSetDynaClass(acts, false, -1, true);
      /*
      String server ="localhost:8080";
      generateHarnessedTable(acts, context.getOut(), "Raw",
          server + "/eTravelerRestful/rest/harnessOutput/" );
          */
    } catch (SQLException sqlEx) {
    } catch (Exception ex) { 
    } finally {
      conn.close();
    }
    return rowSetDyna;
  }
  /*
  private void generateHarnessedTable(ResultSet r, JspWriter wrt, String dbType, 
      String hrefPrefix) throws JspException, IOException {
    try {
      r.next();
      wrt.println("<table class='datatable'><thead><tr>");
      wrt.println("<th sortable='true'>Id</th><th sortable='true'>Name</th><th>Job Version</th>");
      wrt.println("<th class='sortable'>Closed By</th>");
      wrt.println("<th>End Time</th>");
      wrt.println("</tr></thead><tbody>");
      boolean isOdd = true;
      while (!r.isAfterLast()) {
        if (isOdd) {
          wrt.println("<tr class='odd'>");
        } else {
          wrt.println("<tr class='even'>");
        }
        isOdd = !isOdd;
        String hrefargs="?db=" + dbType;
        if (hrefPrefix.isEmpty()) {
          wrt.println("<td style='text-align:left'>" + r.getString("Activity.id"));
        }  else { 
          wrt.println("<td style='text-align:left'y><a href='" + hrefPrefix 
              +  r.getString("Activity.id") + hrefargs + "'>" +
              r.getString("Activity.id") + "</a>");
        }
        wrt.println("</td><td style='text-align:right'>" + r.getString("Process.name")
            +"</td><td style='text-align:left'>" +r.getString("Process.userVersionString")
            + "</td><td style='text-align:left'>" + r.getString("Activity.closedBy") 
            + "</td><td style='text-align:left'>" + r.getString("Activity.end") 
            +  "</td></tr>");
       
        r.next();
      }
      wrt.println("</tbody></table>");
    } catch (SQLException ex) {
      throw new JspException(ex);
    }   
    
  }
  */
/*String dbType, String datasource, */
  public static DbConnection makeConnection(PageContext context, 
                                            boolean readonly) 
      throws Exception {

    String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(),
        "etravelerDb");
    if (dbType.isEmpty()) dbType = "Raw";
    if (datasource.isEmpty() ) datasource = "jdbc/rd-lsst-camt";
    DbConnection  conn = new MysqlDbConnection();
    conn.setSourceDb(dbType);
    boolean isOpen = conn.openTomcat(datasource);
    if (!isOpen) {
      throw new Exception("failed to create db connection to " + datasource);
    }
  
    try {
      conn.setReadOnly(readonly);
    } catch (Exception ex)  {
      throw new 
          Exception("Failed to set connection readonly state with exception " +
            ex.getMessage());
    }
    
    return conn;
  }
  
}
