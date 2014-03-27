/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackenddb.DbInfo;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.lsstcorp.etravelerbackenddb.MysqlDbConnection;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;

/**
 * Used from jsp to retrieve traveler description from db
 * @author jrb
 */
public class DbImporter {
 
 
  static ConcurrentHashMap<String, ProcessNode> s_travelers_Test =
    new ConcurrentHashMap<String, ProcessNode>();
  static ConcurrentHashMap<String, StringArrayWriter> s_writers_Test = 
    new ConcurrentHashMap<String, StringArrayWriter>();
  static ConcurrentHashMap<String, ProcessNode> s_travelers_Dev =
    new ConcurrentHashMap<String, ProcessNode>();
  static ConcurrentHashMap<String, StringArrayWriter> s_writers_Dev = 
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
    ConcurrentHashMap<String, ProcessNode> travelers=null;
    ConcurrentHashMap<String, StringArrayWriter> writers=null;
    if (dbType.equals("dev")) {
      travelers = s_travelers_Dev;
      writers = s_writers_Dev;
      
    } else if (dbType.equals("test")) {
      travelers = s_travelers_Test;
      writers = s_writers_Test;
    } else {
      return "No such database";
    }
    
    // Try connect
    DbConnection conn = makeConnection(info, true, dbType);
    if (conn == null) return "Failed to connect";
    
    String key = makeKey(name, version);
    ProcessNode traveler=null;
    if (travelers.containsKey(key)) {
      traveler = travelers.get(key);
    } else {
     
      try {
        int intVersion = Integer.parseInt(version);
        conn.setReadOnly(true);
        ProcessNodeDb travelerDb = new ProcessNodeDb(conn, name, intVersion, null, null);
        traveler = new ProcessNode(null, travelerDb);
        travelers.putIfAbsent(key, traveler);
      }  catch (NumberFormatException ex) {
        conn.close();
        return  "input version must be integer!";
      }  catch (Exception ex)  {
        System.out.println(ex.getMessage());
        conn.close();
        return "Failed to read traveler " + name + " with exception " 
            + ex.getMessage(); 
      } finally {
        conn.close();
        ProcessNodeDb.reset();
      }
    }
    collectOutput(name, version, travelers, writers);
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
  static public void collectOutput(String name, String version,
      ConcurrentHashMap<String, ProcessNode> travelers, 
      ConcurrentHashMap<String, StringArrayWriter> writers)  {
    StringArrayWriter wrt = new StringArrayWriter();
    TravelerPrintVisitor vis = new TravelerPrintVisitor();
    String key = makeKey(name, version);
    if (!writers.containsKey(key)) {
      vis.setEol("<br />\n");
      vis.setWriter(wrt);
      vis.setIndent("&nbsp;&nbsp");
      ProcessNode trav = travelers.get(key);
      try {
        vis.visit(trav, "Print Html");
      }  catch (EtravelerException ex)  {
        System.out.println("Print to Html failed with exception");
        System.out.println(ex.getMessage());
        return;
      }
      
      writers.putIfAbsent(key, wrt);
    }
  }
  // static public String nextLine(String name, String version) {
  static public String nextLine(PageContext context) {
   
 
    StringArrayWriter wrt = getWriter(context);
    if (wrt == null)  {
      return "No information for requested name, version combination <br />";
    }
    return wrt.fetchNext();
  }
  static public String fetchLine(PageContext context, int i) {
    StringArrayWriter wrt = getWriter(context);
    if (wrt == null)  {
      return "No information for requested name, version combination <br />";
    }
    return wrt.fetchLine(i);
  }
  static public int nLinesUsed(PageContext context) {
    StringArrayWriter wrt = getWriter(context);
    if (wrt == null) return 0;
    return wrt.fetchNUsed();
    //return String.valueOf(wrt.fetchNUsed());
  }
  
  static public String dotSource(PageContext context) {
    return "Called dotSource";
  }
  
  static public void dotGif(PageContext context) {
    JspWriter jspW = context.getOut();
    try {
      jspW.println("Writing from dotGif");
    } catch (IOException x) {
      
    }
  }
  static private StringArrayWriter getWriter(PageContext context)  {
       String name =  context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
     
    String dbType = context.getRequest().getParameter("db");
    ConcurrentHashMap<String, StringArrayWriter> writers=null;
    if (dbType.equals("dev")) {
      writers = s_writers_Dev;
    } else {
      writers = s_writers_Test;
    }
    String key = makeKey(name, version);
     
    return writers.get(key);
  }
  
}
