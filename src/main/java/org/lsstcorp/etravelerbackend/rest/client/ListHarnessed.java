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
    //where += " Process.id = Activity.processId and ";
    where += " ((Process.travelerActionMask & 1) != 0) ";
    where += " and (closedBy is not null)";
    where += " order by Process.name, Activity.id desc";
    String[] cols={"Activity.id as id", "Process.name as jobname", 
      "userVersionString as userVersion", "HardwareType.name as hardwareType",
      "closedBy", "end as endTS"};
    String tableSpec = "(Process inner join Activity on Process.id = Activity.processId)";
    tableSpec += " inner join HardwareType on HardwareType.id = Process.hardwareTypeId";
    PreparedStatement qry = conn.prepareQuery(tableSpec, cols, where);
    ResultSet acts;
    RowSetDynaClass rowSetDyna=null;
    try {
      acts = qry.executeQuery();
      rowSetDyna = new RowSetDynaClass(acts, false, -1, true);
     
    } catch (SQLException sqlEx) {
    } catch (Exception ex) { 
    } finally {
      conn.close();
    }
    return rowSetDyna;
  }

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
