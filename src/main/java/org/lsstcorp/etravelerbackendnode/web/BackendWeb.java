/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode.web;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.lsstcorp.etravelerbackenddb.DbConnection;
/**
 *
 * @author jrb
 * misc. static function utilities invoked from jsp 
 */
public class BackendWeb {
  public static RowSetDynaClass getTravelerTypeInfo(PageContext cxt)  {
    DbConnection conn;
     try {
      conn = DbWebUtil.makeConnection(cxt, true);
      conn.setReadOnly(true);
    } catch (Exception ex) {
      System.out.println("Db connection exception " + ex.getMessage());
      return null;
    }
    String where = " where (rootProcessId=Process.id) and (Process.hardwareTypeId=HardwareType.id)" ;
    where += " order by Process.name, Process.version desc";
    /* Include two fake columns, values to be overwritten by displayTable decorator */
    String[] cols = {"Process.name as name", "Process.version as version", 
      "HardwareType.name as hname", "Process.description as description", 
      "Process.createdBy",
      "Process.creationTS", "'vw' as viewEdit", "'an' as addNCR"};
    PreparedStatement qry = 
        conn.prepareQuery("TravelerType join Process join HardwareType", cols, 
        where);
    ResultSet ttypes;
    RowSetDynaClass rowSetDyna = null;
    try {
      ttypes = qry.executeQuery();
      rowSetDyna = new RowSetDynaClass(ttypes, false, -1, true);
    } catch (SQLException ex) {
      System.out.println("SQL Exception " + ex.getMessage());
    } finally {
      conn.close();
    }
    return rowSetDyna;
  }
}
