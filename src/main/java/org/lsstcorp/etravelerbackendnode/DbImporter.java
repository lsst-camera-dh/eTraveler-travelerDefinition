/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackenddb.DbInfo;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.lsstcorp.etravelerbackenddb.MysqlDbConnection;
import org.lsstcorp.etravelerbackendutil.GraphViz;
import org.freehep.webutil.tree.Tree;   //  freeheptree.Tree;
import org.freehep.webutil.tree.TreeNode; // freeheptree.TreeNode; 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import javax.management.AttributeList;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;

/**
 * Used from jsp to retrieve traveler description from db
 * @author jrb
 */
public class DbImporter { 
 
  static ConcurrentHashMap<String, Traveler> s_travelers =
      new ConcurrentHashMap<String, Traveler>();
  static ConcurrentHashMap<String, StringArrayWriter> s_writers =
      new ConcurrentHashMap<String, StringArrayWriter>();
 /* static ArrayList<String> s_editActions = new ArrayList<String>(); */
 
  private static String makeKey(String name, String version) 
    {return name + "_" + version;}
  private static String makeKey(String name, String version, String dbType)
  {return name+ "_" + version + "@" + dbType;}
  public static ProcessNode getProcess(String name, String version, String dbType,
      String datasource) 
  throws EtravelerException {
    DbInfo info = new DbInfo();
    /*
     * String[] args = {"rd_lsst_camt"};
     * info.dbName = args[0];
     * info.establish();
     */

    ConcurrentHashMap<String, Traveler> travelers=s_travelers;
    ConcurrentHashMap<String, StringArrayWriter> writers=s_writers;
   
    String key = makeKey(name, version, dbType);
    ProcessNode travelerRoot = null;
    Traveler traveler = null;
    if (travelers.containsKey(key)) {
      traveler = travelers.get(key);
      travelerRoot = traveler.getRoot();
    } else {
      // Try connect
      DbConnection conn = makeConnection(dbType, datasource);
      if (conn == null) throw new EtravelerException("Failed to connect");
      try {
        int intVersion = Integer.parseInt(version);
        conn.setReadOnly(true);
        ProcessNodeDb travelerDb = new ProcessNodeDb(conn, name, intVersion, null, null);
        travelerRoot = new ProcessNode(null, travelerDb);
        traveler = new Traveler(travelerRoot, "db", dbType);
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
    return travelerRoot;
  }
  public static String retrieveProcess(PageContext context)  {
    // return "retrieveProcess called with name=" + name ;
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
  
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), 
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType, datasource);
    } catch (EtravelerException ex) {
      return("Failed to retrieve process with exception: " + ex.getMessage() );
    }
      
    collectOutput(name, version, traveler, dbType);
    return "Successfully read in traveler " + name;
  }
  /**
   * Make a connection using Tomcat pool
   *  @param dbType       One of "Prod", "Dev", "Test", "Raw"
   *  @param datasource   datasource name associated with the type, e.g.
   *                      jdbc/rd-lsst-cam
   */
  static private DbConnection makeConnection(String dbType, String datasource)  {
    DbConnection conn = new MysqlDbConnection();
    conn.setSourceDb(dbType);
    boolean isOpen = conn.openTomcat(datasource);
    if (isOpen) {
      System.out.println("Successfully connected to " + datasource);    
      return conn;
    }
    else {
      System.out.println("Failed to connect");
      return null;
    }
  }
  /**
   * Make a connection with supplied information
   * @param info   Data structure containing part of connection info
   * @param dbType  One of "Prod", "Dev", "Test", "Raw"
   * @return 
   */
  static private DbConnection makeConnection(DbInfo info, String dbType)  {   
    // Try connect
    DbConnection conn = new MysqlDbConnection();
    conn.setSourceDb(dbType);
    
  
    // String datasource = "jdbc/eTraveler-" + dbType + "-ro";
    String datasource = info.dbname;
    boolean  isOpen = conn.open(info.host, info.user, info.pwd, info.dbname);
  
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
     ProcessNode trav, String dbType)  {
    StringArrayWriter wrt = new StringArrayWriter();
    TravelerPrintVisitor vis = new TravelerPrintVisitor();
    String key = makeKey(name, version, dbType);
    if (!s_writers.containsKey(key)) {
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
      
      s_writers.putIfAbsent(key, wrt);
    }
  }
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
    ProcessNode travelerRoot = getTraveler(context).getRoot();
    TravelerDotVisitor vis = new TravelerDotVisitor();
    JspWriter writer = context.getOut();
    try {
      vis.initOutput(writer, "\n");
      // vis.initOutput(writer, "\n", context.getRequest().getParameter("db"));
      vis.visit(travelerRoot, "dot file");
      vis.endOutput();
    } catch (EtravelerException ex) {
      return(ex.getMessage());
    }
    
    return "Called dotSource";
  }
  
  static public void dotImg(PageContext context) {
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), 
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType, datasource);
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
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), 
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType, datasource);
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
  /**
   * 
   * @param context  page context
   * @param reason   if it's the string "edit", we're in session scope.
   *                 save visitor
   */
   
  static public void makeTree(PageContext context)  {
    makeTree(context, "view");
  } 
  static public void makeTree(PageContext context, String reason) {
    
    /* Make the map */
    String name;
    String version;
    JspContext jspContext=null;
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), "etravelerDb");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), "dataSourceMode");
    if (reason.equals("edit") ) {
      jspContext = (JspContext)context;
      name = (String)(jspContext.getAttribute("traveler_name", PageContext.SESSION_SCOPE));
      version = (String)(jspContext.getAttribute("traveler_version", PageContext.SESSION_SCOPE));
    } else {
      name = context.getRequest().getParameter("traveler_name");
      version = context.getRequest().getParameter("traveler_version");
    }
    ProcessNode originalTraveler = null;
   
    try {
      originalTraveler = getProcess(name, version, dbType, datasource);
    } catch (EtravelerException ex) {
      System.out.println("Failed to retrieve process with exception: " + ex.getMessage() );
      return;
    }
    ProcessNode traveler = originalTraveler;
    TravelerTreeVisitor vis = new TravelerTreeVisitor();
    HttpServletRequest request = (HttpServletRequest) context.getRequest();
    vis.setPath(request.getContextPath());
    if (reason.equals("edit"))  { /* make a copy */
      traveler = new ProcessNode(null, originalTraveler, 0);
    }
    try {
      vis.visit(traveler, "build");
    } catch (EtravelerException ex) {
      System.out.println("Failed to build tree: " + ex.getMessage() );
      return;
    }
    if (reason.equals("edit")) {
      // Save the visitor
      jspContext.setAttribute("treeVisitor", vis, PageContext.SESSION_SCOPE);
    }
    vis.render(context);
  }
  static public void dotImgMap(PageContext context) {
    JspWriter outWriter = context.getOut();
    /* Make the map */
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(),
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, dbType, datasource);
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
     String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
         "dataSourceMode");
   
     String key = makeKey(name, version, dbType);
     
     return s_writers.get(key);
   }
   static private Traveler getTraveler(PageContext context)  {
     String name =  context.getRequest().getParameter("traveler_name");
     String version = context.getRequest().getParameter("traveler_version");
     
     String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
         "dataSourceMode");
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
   static public Traveler getTraveler(String name, String version, String db) {
  
     String key = makeKey(name, version, db);

     return s_travelers.get(key);
   }
   /**
    * Do selected action on current traveler/process step. 
    * Can find tree visitor (hence traveler) and selected step path
    * from session variables
    * @param action 
    */
   static public void doAction(PageContext pageContext, String action) {
     if (action == null) return;
     if (action.isEmpty()) return;
     JspContext jspContext = (JspContext) pageContext;
     TravelerTreeVisitor vis = (TravelerTreeVisitor) jspContext.getAttribute("treeVisitor", PageContext.SESSION_SCOPE);
     String leafPath = (String) jspContext.getAttribute("leafPath", PageContext.SESSION_SCOPE);
     String folderPath = (String) jspContext.getAttribute("folderPath", PageContext.SESSION_SCOPE);
     
     
     try {
       JspWriter outWriter = jspContext.getOut();
       outWriter.println("Action passed in is " + action + "<br />" );
       
       if (vis.equals(null)) {
         outWriter.println("<p>Tree visitor was null</p>");
         return;
       }    
      
       if (leafPath.equals("")) {
         outWriter.println("<p>leafPath was empty</p>");
       } else {
         outWriter.println("<p>leafPath was " + leafPath + "</p>");
       }
       if (folderPath.equals("")) {
         outWriter.println("<p>folderPath was empty</p>");
       } else {
         outWriter.println("<p>folderPath was " + folderPath + "</p>");
       }
   
       // ProcessTreeNode selectedTreeNode = getTreeNode(context);
    
       switch(action) {
         case "Display": {
           outWriter.println("<p>Display case should be handled elsewhere </p>");
           break;
         }
         case "Edit":
         case "LeafSibling":
         case "SubfolderSibling":
         case "LeafChild":
         case "SubfolderChild":
         case "Remove":
           outWriter.println("<p>Appropriate action seen</p>");
           break;
         default:
           outWriter.println("Unknown action");
       }
     } catch (IOException ex) {
       System.out.println("Failed to write from DbImporter:doAction");
     }
   }
   static public AttributeList selectedNodeAttributes(PageContext context) {
  
     JspContext jspContext = (JspContext) context;
     ProcessTreeNode selectedTreeNode = getTreeNode(jspContext);
     ProcessNode selected = selectedTreeNode.getProcessNode();
     
     return selected.getAttributes();
   }
   
   static public void saveStep(PageContext context)  {
     JspContext jspContext = (JspContext) context;
     ProcessTreeNode selectedTreeNode = getTreeNode(jspContext);
     ProcessNode selected = selectedTreeNode.getProcessNode();
     selected.setDescription(context.getRequest().getParameter("description"));
   }
   
   static private ProcessTreeNode getTreeNode(JspContext jspContext)  {
     TravelerTreeVisitor vis = (TravelerTreeVisitor) jspContext.getAttribute("treeVisitor", PageContext.SESSION_SCOPE);
     String nodePath = (String) jspContext.getAttribute("leafPath", PageContext.SESSION_SCOPE);
     if (nodePath.equals("")) {
       nodePath = (String) jspContext.getAttribute("folderPath", PageContext.SESSION_SCOPE);
     }
     
     ProcessTreeNode rootTreeNode = vis.getTreeRoot();
     int secondSlash = nodePath.indexOf("/", 1);
     if (secondSlash == -1) return rootTreeNode;
     
     /* Otherwise strip off root path at the front */
     nodePath = nodePath.substring(secondSlash);
     return (ProcessTreeNode) rootTreeNode.findNode(nodePath, false);
   }
}
