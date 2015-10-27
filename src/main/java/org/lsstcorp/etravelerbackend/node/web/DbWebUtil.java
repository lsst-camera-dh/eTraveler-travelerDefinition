/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node.web;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import  org.lsstcorp.etravelerbackend.db.MysqlDbConnection;
import  org.lsstcorp.etravelerbackend.db.DbConnection;
import  java.util.Map;
import  javax.servlet.jsp.JspContext;
import  javax.servlet.jsp.PageContext;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;

/**
 *
 * @author jrb
 */
public class DbWebUtil {
  /**
   * Get db connection to 'current' db based on information in page context
   * @param context
   * @param readonly
   * @return   the connection
   * @throws EtravelerException 
   */
  public static DbConnection makeConnection(PageContext context, boolean readonly) 
      throws EtravelerException {
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(),
        "etravelerDb");
    DbConnection  conn = new MysqlDbConnection();
    conn.setSourceDb(dbType);
    boolean isOpen = conn.openTomcat(datasource);
    if (!isOpen) {
      throw new EtravelerException("failed to create db connection to " + dbType);
    }
    if (!readonly)  {
      try {
        conn.setReadOnly(false);
      } catch (Exception ex)  {
        throw new 
            EtravelerException("Failed to set connection writable with exception " +
            ex.getMessage());
      }
    }
    return conn;
  }

  /**
     Make connection to db specified in dbType
   */
  public static DbConnection makeConnection(PageContext context, 
                                            String dbType, boolean readonly) 
      throws EtravelerException {

    java.util.Map varValues = 
      ModeSwitcherFilter.getVariableValues("etravelerDb", context);

    String datasource = (varValues.get(dbType)).toString();

    DbConnection  conn = new MysqlDbConnection();
    conn.setSourceDb(dbType);
    boolean isOpen = conn.openTomcat(datasource);
    if (!isOpen) {
      throw new EtravelerException("failed to create db connection to " + dbType);
    }
    if (!readonly)  {
      try {
        conn.setReadOnly(false);
      } catch (Exception ex)  {
        throw new 
            EtravelerException("Failed to set connection writable with exception " +
            ex.getMessage());
      }
    }
    return conn;
  }
  
}
