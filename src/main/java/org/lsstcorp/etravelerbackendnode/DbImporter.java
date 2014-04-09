/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackenddb.DbInfo;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.lsstcorp.etravelerbackenddb.MysqlDbConnection;
import org.lsstcorp.etravelerbackendutil.GraphViz;
import org.freehep.webutil.tree.Tree;
import org.freehep.webutil.tree.TreeNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
// import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

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
  public static ProcessNode getProcess(String name, String version, String dbType) 
  throws EtravelerException {
    DbInfo info = new DbInfo();
    /*
     * String[] args = {"rd_lsst_camt"};
     * info.dbName = args[0];
     * info.establish();
     */

    ConcurrentHashMap<String, ProcessNode> travelers=null;
    ConcurrentHashMap<String, StringArrayWriter> writers=null;
    if (dbType.equals("dev")) {
      travelers = s_travelers_Dev;
      writers = s_writers_Dev;
      
    } else if (dbType.equals("test")) {
      travelers = s_travelers_Test;
      writers = s_writers_Test;
    } else {
      throw new EtravelerException("No such database type " + dbType);
    }   
    
    String key = makeKey(name, version);
    ProcessNode traveler=null;
    if (travelers.containsKey(key)) {
      traveler = travelers.get(key);
    } else {
      // Try connect
      DbConnection conn = makeConnection(info, true, dbType);
      if (conn == null) throw new EtravelerException("Failed to connect");
      try {
        int intVersion = Integer.parseInt(version);
        conn.setReadOnly(true);
        ProcessNodeDb travelerDb = new ProcessNodeDb(conn, name, intVersion, null, null);
        traveler = new ProcessNode(null, travelerDb);
        travelers.putIfAbsent(key, traveler);
      }  catch (NumberFormatException ex) {
        conn.close();
        throw  new EtravelerException("input version must be integer!");
      }  catch (Exception ex)  {
        System.out.println(ex.getMessage());
        conn.close();
        throw new EtravelerException("Failed to read traveler " + name + " with exception " 
            + ex.getMessage());
      } finally {
        conn.close();
        ProcessNodeDb.reset();
      }
    }
    return traveler;
  }
  public static String retrieveProcess(PageContext context)  {
    // return "retrieveProcess called with name=" + name ;
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String dbType = context.getRequest().getParameter("db");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType);
    } catch (EtravelerException ex) {
      return("Failed to retrieve process with exception: " + ex.getMessage() );
    }
      
    ConcurrentHashMap<String, StringArrayWriter> writers=null;
    if (dbType.equals("dev")) {
      writers = s_writers_Dev;    
    } else if (dbType.equals("test")) {
      writers = s_writers_Test;
    } else {
      return "No such database";
    }   
    collectOutput(name, version, traveler, writers);
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
     ProcessNode trav, ConcurrentHashMap<String, StringArrayWriter> writers)  {
    StringArrayWriter wrt = new StringArrayWriter();
    TravelerPrintVisitor vis = new TravelerPrintVisitor();
    String key = makeKey(name, version);
    if (!writers.containsKey(key)) {
      vis.setEol("\n");
      vis.setWriter(wrt);
      vis.setIndent("&nbsp;&nbsp");
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
    ProcessNode traveler = getTraveler(context);
    TravelerDotVisitor vis = new TravelerDotVisitor();
    JspWriter writer = context.getOut();
    try {
      vis.initOutput(writer, "\n");
      // vis.initOutput(writer, "\n", context.getRequest().getParameter("db"));
      vis.visit(traveler, "dot file");
      vis.endOutput();
    } catch (EtravelerException ex) {
      return(ex.getMessage());
    }
    
    return "Called dotSource";
  }
  
  static public void dotImg(PageContext context) {
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String dbType = context.getRequest().getParameter("db");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType);
    } catch (EtravelerException ex) {
      System.out.println("Failed to retrieve process with exception: " + ex.getMessage() );
      return;
    }
    JspWriter outWriter = context.getOut();
    String nameEncoded=null;
    try {
      nameEncoded = 
          URLEncoder.encode(context.getRequest().getParameter("traveler_name"), "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      System.out.println("Bad traveler name");
      return;
    }
    try {
      outWriter.println("<img src=\"TravelerImageServlet?name=" + name + "&version=" 
          + version + "&db=" +dbType + "\" />");
    } catch  (IOException ex) {
      System.out.println("Couldn't write img line");
    }
   
  }
  static public ProcessTreeNode buildTree(PageContext context) {
     JspWriter outWriter = context.getOut();
    /* Make the map */
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String dbType = context.getRequest().getParameter("db");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType);
    } catch (EtravelerException ex) {
      System.out.println("Failed to retrieve process with exception: " + ex.getMessage() );
      return null;
    }
    TravelerTreeVisitor vis = new TravelerTreeVisitor();
    try {
      vis.visit(traveler, "build");
    } catch (EtravelerException ex) {
      System.out.println("Failed to build tree: " + ex.getMessage() );
      return null;
    }
    return vis.getTreeRoot();
  }
  static public void makeTree(PageContext context) {
    
    /* Make the map */
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String dbType = context.getRequest().getParameter("db");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType);
    } catch (EtravelerException ex) {
      System.out.println("Failed to retrieve process with exception: " + ex.getMessage() );
      return;
    }
    TravelerTreeVisitor vis = new TravelerTreeVisitor();
    HttpServletRequest request = (HttpServletRequest) context.getRequest();
    vis.setPath(request.getContextPath());
    try {
      vis.visit(traveler, "build");
    } catch (EtravelerException ex) {
      System.out.println("Failed to build tree: " + ex.getMessage() );
      return;
    }
    vis.render(context);
  }
  static public void dotImgMap(PageContext context) {
    JspWriter outWriter = context.getOut();
    /* Make the map */
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String dbType = context.getRequest().getParameter("db");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType);
    } catch (EtravelerException ex) {
      System.out.println("Failed to retrieve process with exception: " + ex.getMessage() );
      return;
    }
    
    StringWriter dotWriter = new StringWriter();
    TravelerDotVisitor vis = new TravelerDotVisitor();
    
    try {
      vis.initOutput(dotWriter, "\n");
      vis.visit(traveler, "dot file");
      vis.endOutput();
    } catch (EtravelerException ex) {
      System.out.println("Failed to make dot file: " + ex.getMessage());
    }
    GraphViz gv = new GraphViz("dot");
    ByteArrayOutputStream bytes=null;
    try {
      bytes = gv.getGraph(dotWriter.toString(), GraphViz.Format.CMAPX);
      //outWriter.println("<map name=\"Traveler\" >");
      outWriter.println(bytes.toString());
      //outWriter.println("</map>");
    } catch (IOException ex) {
      System.out.println("Failed to make or output image map");
      return;
    }
 
    /* Post to servlet to make the image */
    String encodedName=null;    
    try {
      encodedName = 
          URLEncoder.encode(context.getRequest().getParameter("traveler_name"), "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      System.out.println("Bad traveler name");
    }
 
    try {
      outWriter.println("<img src=\"TravelerImageServlet?name=" + name + "&version=" 
          + version + "&db=" +dbType + "\" usemap=\"#Traveler\"  />");
    } catch  (IOException ex) {
      System.out.println("Couldn't write img line");
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
   static private ProcessNode getTraveler(PageContext context)  {
     String name =  context.getRequest().getParameter("traveler_name");
     String version = context.getRequest().getParameter("traveler_version");
     
     String dbType = context.getRequest().getParameter("db");
     
     return getTraveler(name, version, dbType);
   }
   /**
    * getTraveler only accesses local data.  It tries to find traveler among 
    * those already stored (unlike getProcess which will go to db if necessary)
    * @param name      Process name
    * @param version   Process version
    * @param db
    * @return 
    */
   static public ProcessNode getTraveler(String name, String version, String db) {
     ConcurrentHashMap<String, ProcessNode> travelers=null;
     if (db.equals("dev")) {
       travelers = s_travelers_Dev;
     } else if (db.equals("test")) {
       travelers = s_travelers_Test;
     } else return null;                   // or throw exception?
     
     String key = makeKey(name, version);
     
     return travelers.get(key);
   }
}
