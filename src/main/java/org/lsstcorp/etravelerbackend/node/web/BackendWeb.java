/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node.web;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.lsstcorp.etravelerbackend.db.DbConnection;
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
    String nameArg = cxt.getAttribute("name").toString();
    String groupArg = cxt.getAttribute("group").toString();
    String versionArg = cxt.getAttribute("version").toString();
    String stateArg = cxt.getAttribute("state").toString();
    
    String oldwhere = " where (rootProcessId=Process.id)  and (Process.hardwareGroupId=HardwareGroup.id)" ;
   
    oldwhere += " order by Process.name, Process.version desc";
    /* Include two fake columns, values to be overwritten by displayTable decorator */
    String[] cols = {"P.name as name", "P.version as version", "HG.name as hname",
      "HG.id as hgid",
      "P.description as description", "P.createdBy", "P.creationTS", "TTS.name as state", 
      "'vw' as viewEdit", "'an' as addNCR"};
    String tableSpec = "Process P inner join TravelerType TT on P.id=TT.rootProcessId ";
    tableSpec += "inner join HardwareGroup HG on HG.id=P.hardwareGroupId ";
    tableSpec += "inner join TravelerTypeStateHistory TTSH on TT.id=TTSH.travelerTypeId ";
    tableSpec += "and TTSH.id=(select max(id) from TravelerTypeStateHistory";
    tableSpec +=" where travelerTypeId=TT.id) ";
    tableSpec += "inner join TravelerTypeState TTS on TTS.id=TTSH.travelerTypeStateId ";
    String where=" where true ";
    if (! nameArg.isEmpty()) {
      where += " and P.name like concat('%', '" + nameArg 
          + "', '%') ";
    }
    if (! groupArg.equals("all")) {
      where += " and HG.name = '" + groupArg +"' ";
    }
    if (! stateArg.equals("any"))  {
      where += " and TTS.name='" + stateArg +"' ";
    }
    if (versionArg.equals("latest")) {
      where += " and P.version=(select max(version) from Process where ";
      where += "name=P.name and hardwareGroupId=P.hardwareGroupId)";
    }
    where += " order by P.name";
    PreparedStatement qry = 
      //conn.prepareQuery("TravelerType join Process join HardwareGroup", cols,  where);
      conn.prepareQuery(tableSpec, cols, where);
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
