/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackendexceptions.EtravelerException;
import org.lsstcorp.etravelerbackenddb.DbInfo;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.lsstcorp.etravelerbackenddb.MysqlDbConnection;
import org.lsstcorp.etravelerbackendutil.GraphViz;
import org.lsstcorp.etravelerbackendutil.Verify;
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
import javax.servlet.ServletRequest;
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
 // private static String makeKey(String name, String version, String dbType)
 // {return name+ "_" + version + "@" + dbType;}
  private static String makeKey(String name, String version, String htype, 
      String dbType) {
    return name + "_" + version + "_" + htype + "@" + dbType;
  }
  public static ProcessNode getProcess(String name, String version, 
      String htype, String dbType, String datasource) 
    throws EtravelerException {
    DbInfo info = new DbInfo();
    /*
     * String[] args = {"rd_lsst_camt"};
     * info.dbName = args[0];
     * info.establish();
     */

    ConcurrentHashMap<String, Traveler> travelers=s_travelers;
    ConcurrentHashMap<String, StringArrayWriter> writers=s_writers;
   
    String key = makeKey(name, version, htype, dbType);
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
        ProcessNodeDb travelerDb = new ProcessNodeDb(conn, name, intVersion, 
            htype, null, null);
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
    return retrieveProcess(context, true);
   
  }
   public static String retrieveProcess(PageContext context, boolean printIt)  {
    // return "retrieveProcess called with name=" + name ;
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String htype = context.getRequest().getParameter("traveler_htype");
  
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), 
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, htype, dbType, datasource);
    } catch (EtravelerException ex) {
      return("Failed to retrieve process with exception: " + ex.getMessage() );
    }
    if (printIt)   {
      collectOutput(name, version, htype, traveler, dbType);
    }
      return "";
  }
 
  static private void collectOutput(String name, String version, String htype,
     ProcessNode trav, String dbType)  {
    StringArrayWriter wrt = new StringArrayWriter();
    TravelerPrintVisitor vis = new TravelerPrintVisitor();
    String key = makeKey(name, version, htype, dbType);
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
    String htype = context.getRequest().getParameter("traveler_htype");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), 
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, htype, dbType, datasource);
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
          + version + "&htype=" + htype + "&db=" +dbType + "\" />");
    } catch  (IOException ex) {
      System.out.println("Couldn't write img line");
    }
   
  }
  static public void displayTraveler(PageContext context) {
    String ostyle =  context.getRequest().getParameter("ostyle");
    switch (ostyle) {
      case "pprint":
      case "Pretty print":
        JspWriter writer = context.getOut();
        int nLines = nLinesUsed(context);
        try {
          writer.println("<pre>");
          for (int iLine = 0; iLine < nLines; iLine++) {
            writer.println(fetchLine(context, iLine));
          }
          writer.println("</pre>");
        } catch (IOException ex) {
          System.out.println("IOException from DbImporter.displayTraveler");
        }
        break;
      case "dot":
      case "Dot file":
        dotSource(context);
        break;
      case "img":
      case "Image":
        dotImg(context);
        break;
      case "imgMap":
      case "Image map":
        dotImgMap(context);
        break;
      case "tree":
      case "Tree":
        makeTree(context);
        break;
      default:
        break;
    }
  }

  static public void makeTree(PageContext context)  {
    makeTree(context, "view");
  } 
  /**
   * 
   * @param context  page context
   * @param reason   if it's the string "edit", we're in session scope.
   */  
  static public void makeTree(PageContext context, String reason) {
    /* Make the map */
    String name;
    String version;
    String htype;
    JspContext jspContext=null;
    jspContext = (JspContext)context;
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), "etravelerDb");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), "dataSourceMode");
    if (reason.equals("edit") || reason.equals("NCR")) {
      name = (String)(jspContext.getAttribute("traveler_name", PageContext.SESSION_SCOPE));
      version = (String)(jspContext.getAttribute("traveler_version", PageContext.SESSION_SCOPE));
      htype = (String)(jspContext.getAttribute("traveler_htype", PageContext.SESSION_SCOPE));
    } else {
      name = context.getRequest().getParameter("traveler_name");
      version = context.getRequest().getParameter("traveler_version");
      htype = context.getRequest().getParameter("traveler_htype");
    }
    ProcessNode originalTraveler = null;
   
    try {
      originalTraveler = getProcess(name, version, htype, dbType, datasource);
    } catch (EtravelerException ex) {
      try {
        context.getOut().println("Failed to retreive process with exception: " 
            + ex.getMessage());
      } catch (IOException ioex) {}
      System.out.println("Failed to retrieve process with exception: " + ex.getMessage() );
      return;
    }
    ProcessNode traveler = originalTraveler;
    TravelerTreeVisitor vis = 
        new TravelerTreeVisitor(reason.equals("edit"), reason);
    HttpServletRequest request = (HttpServletRequest) context.getRequest();
    vis.setPath(request.getContextPath());
    try {
      if (reason.equals("edit"))  { /* make a copy */
        traveler = new ProcessNode(null, originalTraveler, 0);
      }
      vis.visit(traveler, "build");
    } catch (EtravelerException ex) {
      System.out.println("Failed to build tree: " + ex.getMessage() );
      return;
    }
    if (reason.equals("edit")) {
      // Tell visitor about original traveler
      vis.setCopiedFrom(originalTraveler);
    }
    jspContext.setAttribute("treeVisitor", vis, PageContext.SESSION_SCOPE);
    //}
    vis.render(context);
  }
  static public void dotImgMap(PageContext context) {
    JspWriter outWriter = context.getOut();
    /* Make the map */
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String htype = context.getRequest().getParameter("traveler_htype");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(),
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, htype, dbType, datasource);
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
          + version + "&htype=" + htype + "&db=" +dbType 
          + "\" usemap=\"#Traveler\"  />");
    } catch  (IOException ex) {
      System.out.println("Couldn't write img line");
    }
    
  }
  static private StringArrayWriter getWriter(PageContext context)  {
    String name =  context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String htype = context.getRequest().getParameter("traveler_htype");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
                                                   "dataSourceMode");
    String key = makeKey(name, version, htype, dbType);
     
    return s_writers.get(key);
  }

  /**
   * getTraveler only accesses local data.  It tries to find traveler among 
   * those already stored (unlike getProcess which will go to db if necessary)
   * @param name      Process name
   * @param version   Process version
   * @param db
   * @return 
   */
  static public Traveler getTraveler(String name, String version, String htype,
      String db) {
    String key = makeKey(name, version, htype, db);
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
    TravelerTreeVisitor vis = 
      (TravelerTreeVisitor) jspContext.getAttribute("treeVisitor", 
                                                    PageContext.SESSION_SCOPE);
    String leafPath = (String) 
      jspContext.getAttribute("leafPath", PageContext.SESSION_SCOPE);
    String folderPath = (String) 
      jspContext.getAttribute("folderPath", PageContext.SESSION_SCOPE);   
    
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
   
  static public int getPrerequisiteCount(PageContext context) {
    JspContext jspContext = (JspContext) context;
    ProcessNode selected = getTreeNode(jspContext).getProcessNode();
    return selected.getPrerequisiteCount();
  }

  static public ArrayList<Prerequisite> getPrerequisites(PageContext context) {
    JspContext jspContext = (JspContext) context;
    ProcessNode selected = getTreeNode(jspContext).getProcessNode();
    return selected.getPrerequisites();
  }
   
  static public int getResultCount(PageContext context) {
    JspContext jspContext = (JspContext) context;
    ProcessNode selected = getTreeNode(jspContext).getProcessNode();
    return selected.getResultCount();
  }
  
  static public ArrayList<PrescribedResult> getResults(PageContext context) {
    JspContext jspContext = (JspContext) context;
    ProcessNode selected = getTreeNode(jspContext).getProcessNode();
    return selected.getResults();
  }
   
  static public String saveStep(PageContext context)  {
    JspContext jspContext = (JspContext) context;
    ProcessTreeNode selectedTreeNode = getTreeNode(jspContext);
    ProcessNode selected = selectedTreeNode.getProcessNode();
    boolean changed = false;
    
    String valid = checkStep(context, selected);
    if (!valid.isEmpty()) return "<p class='warning'" + valid + "</p>";
    
    String newVal = context.getRequest().getParameter("maxIt");
    if (!newVal.equals(selected.getMaxIteration()) ) { 
      selected.setMaxIteration(newVal);          
      changed = true;
    }
    newVal = context.getRequest().getParameter("description");
    if (!newVal.equals(selected.getDescription()) ) {
      selected.setDescription(newVal);
      changed = true;
    }
    newVal = context.getRequest().getParameter("instructionsURL");
    if (!newVal.equals(selected.getInstructionsURL()) ) {
      selected.setInstructionsURL(newVal);
      changed = true;
    }
    newVal = context.getRequest().getParameter("userVersionString");
    if (!newVal.equals(selected.getUserVersionString()) ) {
      selected.setUserVersionString(newVal);
      changed = true;
    }
    for (int iPre=0; iPre < selected.getPrerequisiteCount(); iPre++ ) {
      changed |= savePrereq(context.getRequest(), 
                            selected.getPrerequisites().get(iPre), iPre);
    }
    for (int iRes=0; iRes < selected.getResultCount(); iRes++ ) {
      changed |= saveResult(context.getRequest(), 
                            selected.getResults().get(iRes), iRes);
    }   
    
    if (changed) {
      TravelerTreeVisitor vis = 
        (TravelerTreeVisitor) jspContext.getAttribute("treeVisitor", 
                                                      PageContext.SESSION_SCOPE);
      vis.addEdited(selectedTreeNode, "modified");
      selected.newVersion();
    } else {
      return "<p class='warning'>Step unchanged; nothing to save</p>";
    }
    return null;
  }
  /**
   * Handle requests for actions concerning entire traveler currently
   * being edited.
   * @param context 
   */
  //  static public AttributeList listEdited(PageContext context) {
  static public ArrayList<EditedTreeNode>
    listEdited(PageContext context) {
    JspContext jspContext = (JspContext)context;
    TravelerTreeVisitor vis = (TravelerTreeVisitor) 
      jspContext.getAttribute("treeVisitor", 
                              PageContext.SESSION_SCOPE);   
    return vis.getEdited();  
  }
   
  static public void ingestEdited(PageContext context) {
    JspContext jspContext = (JspContext)context;
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
                                                   "dataSourceMode");
    String datasource = 
      ModeSwitcherFilter.getVariable(context.getSession(), "etravelerDb");
    TravelerTreeVisitor vis = 
      (TravelerTreeVisitor) jspContext.getAttribute("treeVisitor", 
                                                    PageContext.SESSION_SCOPE);
    ProcessNode travelerRoot = vis.getTravelerRoot();
    if (vis.getNEdited() == 0) {
      try {
        context.getOut().println("<p class='warning'>Traveler has not been modified. Will not ingest</p>");
        return;
      } catch (IOException ex) {
        System.out.println("DbImporter.ingestEdited: JspWriter failed to write");
      }
    }
    String msg = 
      WriteToDb.writeToDb(travelerRoot, 
                         context.getSession().getAttribute("userName").toString(),
                         true, dbType, datasource);
    try {
      context.getOut().println(msg);
    } catch (IOException ex) {
      System.out.println("DbImporter.ingestEdited: JspWriter failed to write");
    }
  }
  static public void adjustList(PageContext context, String path) {
    JspContext jspContext = (JspContext) context;
    TravelerTreeVisitor vis = 
        (TravelerTreeVisitor) jspContext.getAttribute("treeVisitor", 
        PageContext.SESSION_SCOPE);

    boolean ok = vis.undoEdited(path);
    /* Maybe need to re-render if successful? */
  }
  
  static public void revertEdited(PageContext context) {
    
  }
  
  static public void makeNCR(PageContext context, String ncrId)  {
     
    /* Get the tree and traveler */
    TravelerTreeVisitor vis = 
        (TravelerTreeVisitor) ((JspContext) context).getAttribute("treeVisitor", 
        PageContext.SESSION_SCOPE);
    
    ProcessTreeNode treeRoot = vis.getTreeRoot();
    ProcessNode travelerRoot = vis.getTravelerRoot();
    
    /* Use session vars for exit & return steps to find corr. process nodes */
    String exitPath = context.getSession().getAttribute("exitStep").toString();
    String returnPath = 
      context.getSession().getAttribute("returnStep").toString();
    String treeNodeId = context.getSession().getAttribute("treeNodeId").toString();
    String ncrCondition = 
      context.getSession().getAttribute("NCRCondition").toString();
    ProcessNode exitProcess = 
        (vis.findNodeFromPath(exitPath, treeNodeId)).getProcessNode();
    ProcessNode returnProcess = 
      (vis.findNodeFromPath(returnPath, treeNodeId)).getProcessNode();

    /* Get a suitable (write) db connection */
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), 
        "etravelerDb");
    
    /* Have to get ncrProcessId from context somehow */
    /*
    ServletRequest rqst = context.getRequest();
    Object ncrIdObj = rqst.getAttribute("ncrTraveler");
    if (ncrIdObj == null) {
      return;    // rats!
    }
    */
    NCRSpecification ncrSpec = NCRSpecification.makeNCRSpecification(
     treeRoot.getProcessNode(), exitProcess, returnProcess, ncrId, 
     ncrCondition, dbType);

    /* Invoke a "do it" routine, passing 
     *        root of traveler
     *        exit node
     *        return node
     *        condition string
     *        db connection info
     */
    WriteToDb.writeNCRToDb(ncrSpec, 
        context.getSession().getAttribute("userName").toString(), true, 
        dbType, datasource);
  }
  
  
  /*  Private from here on out  */
  /**
   * Make a connection using Tomcat pool
   *  @param dbType       One of "Prod", "Dev", "Test", "Raw"
   *  @param datasource   datasource name associated with the type, e.g.
   *                      jdbc/rd-lsst-cam
   */
  static private DbConnection makeConnection(String dbType, String datasource){
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
  
  static private Traveler getTraveler(PageContext context)  {
    String name =  context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String htype = context.getRequest().getParameter("traveler_htype");
    
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
                                                   "dataSourceMode");
    return getTraveler(name, version, htype, dbType);
  }
    
  static private String checkStep(PageContext context, ProcessNode selected) {
    String newVal = context.getRequest().getParameter("maxIt");
    String ret="";
    if (!newVal.equals(selected.getMaxIteration()) ) {
      ret = Verify.isPosInt(newVal);
      if (!ret.isEmpty()) return ret;
    }
    // Validate Prerequisites if any
    if (selected.getPrerequisiteCount() > 0) {
      for (int i=0; i < selected.getPrerequisiteCount(); i++) {
        String param = genId("count", i);
        newVal = context.getRequest().getParameter(param);
        ret = Verify.isPosInt(newVal);
        if (!ret.isEmpty()) return ret;
      }
    }
    // Validate PrescribedResults if any
    if (selected.getResultCount() > 0) {
      for (int i=0; i < selected.getResultCount(); i++) {
        if (selected.getResults().get(i).numberSemantics()) {
          String minValue = context.getRequest().getParameter(genId("min", i));
          if (minValue == null) minValue="";
          String maxValue = context.getRequest().getParameter(genId("max", i));
          if (maxValue == null) maxValue="";
          if (minValue.isEmpty() && maxValue.isEmpty()) continue;
          boolean haveBoth = !(minValue.isEmpty() || maxValue.isEmpty());
          String ok="";
          if (selected.getResults().get(i).getSemantics().equals("int")) {
            if (!minValue.isEmpty()) ok = Verify.isInt(minValue);
            if (ok.isEmpty() && !maxValue.isEmpty()) ok = Verify.isInt(maxValue);
            if (haveBoth) {
              if (Integer.parseInt(minValue) > Integer.parseInt(maxValue)) {
                return "max must be greater than min";
              }
            }
          }   else { // must be float
            if (!minValue.isEmpty()) ok = Verify.isFloat(minValue);
            if (ok.isEmpty() && !maxValue.isEmpty()) {
              ok = Verify.isFloat(maxValue);
            }
            if (haveBoth) {
              if (Float.parseFloat(minValue) > Float.parseFloat(maxValue)) {
                return "max must be greater than min";
              }
            }
          }
        }    
      }
    }
    return "";
  }
  /**
   * Update stored Prerequisite according to form input 
   * @param req   servlet request 
   * @param pre   Prerequisite to be updated
   * @param iPre  prerequisite number, needed to form ids for form fields
   * @return   true if Prerequisite object was changed by request input
   */
  static private boolean savePrereq(ServletRequest req, Prerequisite pre, int iPre)  {
    boolean changed = false;
    
    String newVal = req.getParameter(genId("prereqDescrip", iPre));
    changed |= pre.setDescription(newVal);
    
    newVal = req.getParameter(genId("count", iPre));
    changed |= pre.setQuantity(newVal);
    
    if ((pre.getType()).equals("PROCESS_STEP")) {
      newVal = req.getParameter(genId("userVersion", iPre));
      changed |= pre.setUserVersionString(newVal);
    }
    return changed;
  }
  static private boolean saveResult(ServletRequest req, 
                                    PrescribedResult result, int iRes)  {
    boolean changed = false;
    String newVal = req.getParameter(genId("resultDescrip", iRes));
    changed |= result.setDescription(newVal);
    
    if (result.numberSemantics()) {
      newVal = req.getParameter(genId("units", iRes));
      changed |= result.setUnits(newVal);
      
      newVal = req.getParameter(genId("min", iRes));
      changed |= result.setMinValue(newVal);
      
      newVal = req.getParameter(genId("max", iRes));
      changed |= result.setMaxValue(newVal);
    }
    return changed;
  }
  static private String genId(String nm, int i) {
    return nm + "_" + Integer.toString(i);
  }
 
  static private ProcessTreeNode getTreeNode(JspContext jspContext) {
    TravelerTreeVisitor vis = (TravelerTreeVisitor) 
      jspContext.getAttribute("treeVisitor", 
                              PageContext.SESSION_SCOPE);

     /*
     String nodePath = (String) jspContext.getAttribute("leafPath", PageContext.SESSION_SCOPE);
     if (nodePath.equals("") ) {
       nodePath = (String) jspContext.getAttribute("folderPath", PageContext.SESSION_SCOPE);
     }
     if (nodePath.equals("") ) { */
    String nodePath=(String) jspContext.getAttribute("nodePath", 
                                                     PageContext.SESSION_SCOPE);
    String treeNodeId = (String) jspContext.getAttribute("treeNodeId", 
        PageContext.SESSION_SCOPE);
    /* } */
     /*
    ProcessTreeNode rootTreeNode = vis.getTreeRoot();
    int secondSlash = nodePath.indexOf("/", 1);
    if (secondSlash == -1) return rootTreeNode;
    */
    /* Otherwise strip off root path at the front */
    /*
    nodePath = nodePath.substring(secondSlash);
    return (ProcessTreeNode) rootTreeNode.findNode(nodePath, false);
    */
    return vis.findNodeFromPath(nodePath, treeNodeId);
  }
}
