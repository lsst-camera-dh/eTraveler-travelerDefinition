/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.db;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.ResultSet;

/**
 *
 * @author jrb
 */

public class DbTest {
 
  /**
   * This routine is designed to be called from jsp.
   * Use default db parameters, get connection from Tomcat, then do
   * a couple queries
   * @return a string containing query results
   */
  
  public static String mainFromJsp() {
    String[] args = {"rd_lsst_camt"};
    DbInfo info = new DbInfo();
    
    info.dbname = args[0];
    
    info.establish();
    
    // Try connect
    DbConnection conn = makeConnection(info, true);
    if (conn == null) return "Failed to connect";
    return doQuery(conn);
    
  }
  /**
   * @param args the command line arguments
   * Anywhere between 0 and 3 may be supplied.  If present,
   * first is dbname, second is user, third is password
   */
  public static void main(String[] args) {
    DbInfo info = new DbInfo();
    
    if (args.length > 0) { info.dbname = args[0]; }
    if (args.length > 1) { info.user = args[1];}
    if (args.length > 2) { info.pwd = args[2];}
    if (info.pwd.equals(""))  { // use more complicated strategies
      info.establish();
    }
    // Try connect
    DbConnection conn = makeConnection(info, false);
   
    String queryResult = doQuery(conn);     
  }
  static DbConnection makeConnection(DbInfo info, boolean usePool)  {   
    // Try connect
    DbConnection conn = new MysqlDbConnection();
    boolean isOpen = false;
    if (usePool) {
      isOpen = conn.openTomcat("jdbc/eTraveler-test-ro");
    } else {
      isOpen = conn.open(info.host, info.user, info.pwd, info.dbname);
    }
    if (isOpen) {
      System.out.println("Successfully connected to rd_lsst_camt");
      
      return conn;
    }
    else {
      System.out.println("Failed to connect");
      return null;
    }
  }
  static String doQuery(DbConnection conn)  {
    // try a query
    String returnValue = "";
    String[] fields = new String[3];
    fields[0] = "id";
    fields[1] = "name";
    fields[2] = "creationTS";
    String where = " WHERE id=?";
    PreparedStatement query;
    query = conn.prepareQuery("Process", fields, where);
    ResultSet rs;
    boolean failed = false;
    boolean gotRow;
    pidLoop:
    for (int pid=1; pid < 4; pid++)  {
      try {
        query.setInt(1, pid);
        rs = query.executeQuery();
        gotRow = rs.next();
        String outstr = "Process with id " + rs.getString(1) + " has name ";
        outstr += rs.getString(2) + " and was created " + rs.getString(3);
        System.out.println(outstr);
        returnValue += outstr + "<br \\>";
        rs.close();
      }
      catch (SQLException ex)  {
        returnValue = "Query failed with exception" + ex.getMessage();
        System.out.println(returnValue);
        failed = true;
        break pidLoop;
      }
    } 
    conn.close();
    return returnValue;
  }
}
