/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackenddb.DbInfo;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.lsstcorp.etravelerbackenddb.MysqlDbConnection;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.jsp.PageContext;

/**
 * Used from jsp to retrieve traveler description from db
 * @author jrb
 */
public class DbImporter {
 
 
  static ConcurrentHashMap<String, ProcessNode> s_travelers =
    new ConcurrentHashMap<String, ProcessNode>();
  static ConcurrentHashMap<String, StringArrayWriter> s_writers = 
    new ConcurrentHashMap<String, StringArrayWriter>();
  private static String makeKey(String name, String version) 
    {return name + "_" + version;}
 // public static String retrieveProcess(String name, String version)  {
  public static String retrieveProcess(PageContext context)  {
    // return "retrieveProcess called with name=" + name ;
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String dbType = context.getRequest().getParameter("db");
    System.out.println("Got db parameter " + dbType);
        
    String[] args = {"rd_lsst_camt"};
    DbInfo info = new DbInfo();
    
    info.dbname = args[0];
    
    info.establish();
    
    // Try connect
    DbConnection conn = makeConnection(info, true, dbType);
    if (conn == null) return "Failed to connect";
    
    String key = makeKey(name, version);
    ProcessNode traveler=null;
    if (s_travelers.containsKey(key)) {
      traveler = s_travelers.get(key);
    } else {
      try {
        int intVersion = Integer.parseInt(version);
        conn.setReadOnly(true);
        traveler = 
            new ProcessNode(null, new ProcessNodeDb(conn, name, intVersion, 
            null, null));
        s_travelers.putIfAbsent(key, traveler);
      }  catch (NumberFormatException ex) {
        conn.close();
        return  "input version must be integer!";
      }  catch (Exception ex)  {
        System.out.println(ex.getMessage());
        return "Failed to read traveler " + name + " with exception " 
            + ex.getMessage(); 
      }
    }
    collectOutput(name, version);
    return "Successfully read in traveler " + name;
  }
  static private DbConnection makeConnection(DbInfo info, boolean usePool,
      String dbType)  {   
    // Try connect
    DbConnection conn = new MysqlDbConnection();
    boolean isOpen = false;
    String datasource = "jdbc/eTraveler-" + dbType + "-ro";
    if (usePool) {
       isOpen = conn.openTomcat(datasource);
    } else {
      datasource = info.dbname;
      isOpen = conn.open(info.host, info.user, info.pwd, info.dbname);
    }
    if (isOpen) {
      System.out.println("Successfully connected to " + datasource);    
      return conn;
    }
    else {
      System.out.println("Failed to connect");
      return null;
    }
  }
  static public void collectOutput(String name, String version)  {
    StringArrayWriter wrt = new StringArrayWriter();
    TravelerPrintVisitor vis = new TravelerPrintVisitor();
    String key = makeKey(name, version);
    if (!s_writers.containsKey(key)) {
      vis.setEol("<br />\n");
      vis.setWriter(wrt);
      vis.setIndent("&nbsp;&nbsp");
      ProcessNode trav = s_travelers.get(key);
      try {
        vis.visit(trav, "Print Html");
      }  catch (EtravelerException ex)  {
        System.out.println("Print to Html failed with exception");
        System.out.println(ex.getMessage());
        return;
      }
      
      s_writers.putIfAbsent(key, wrt);
    }
  }
  static public String nextLine(String name, String version) {
    String key = makeKey(name, version);
    StringArrayWriter wrt = s_writers.get(key);
    if (wrt == null)  {
      return "No information for traveler " + name + ", version " + version + "<br />";
    }
    return wrt.fetchNext();
  }
  static public String fetchLine(String name, String version, int i) {
    String key = makeKey(name, version);
    StringArrayWriter wrt = s_writers.get(key);
    if (wrt == null)  {
      return "No information for traveler " + name + ", version " + version + "<br />";
    }
    return wrt.fetchLine(i);
  }
  static public int nLinesUsed(String name, String version) {
    String key = makeKey(name, version);
    StringArrayWriter wrt = s_writers.get(key);
    if (wrt == null) return 0;
    return wrt.fetchNUsed();
    //return String.valueOf(wrt.fetchNUsed());
  }
  
}
