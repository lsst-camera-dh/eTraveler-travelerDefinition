/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.ui;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import org.lsst.camera.etraveler.backend.db.MysqlDbConnection;
import org.lsst.camera.etraveler.backend.util.GraphViz;
import org.lsst.camera.etraveler.backend.util.Verify;
import org.freehep.webutil.tree.Tree;   //  freeheptree.Tree;
import org.freehep.webutil.tree.TreeNode; // freeheptree.TreeNode; 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import javax.management.AttributeList;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.lsst.camera.etraveler.backend.ui.EditedTreeNode;
import org.lsst.camera.etraveler.backend.node.NCRSpecification;
import org.lsst.camera.etraveler.backend.node.Prerequisite;
import org.lsst.camera.etraveler.backend.node.PrescribedResult;
import org.lsst.camera.etraveler.backend.node.ProcessNode;
import org.lsst.camera.etraveler.backend.node.ProcessNodeDb;
import org.lsst.camera.etraveler.backend.node.RelationshipTask;
import org.lsst.camera.etraveler.backend.node.StringArrayWriter;
import org.lsst.camera.etraveler.backend.node.Traveler;
import org.lsst.camera.etraveler.backend.node.TravelerDotVisitor;
import org.lsst.camera.etraveler.backend.node.TravelerPrintVisitor;
import org.lsst.camera.etraveler.backend.node.TravelerToYamlVisitor;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;

/**
 * Used from jsp to retrieve traveler description from db
 * @author jrb
 */
public class DbImporter { 
 
  static ConcurrentHashMap<String, Traveler> s_travelers =
      new ConcurrentHashMap<String, Traveler>();
 
  private static String makeKey(String name, String version) 
    {return name + "_" + version;}

  public static String makeKey(String name, String version, String hgroup, 
      String dbType) {
    return name + "_" + version + "_" + hgroup + "@" + dbType;
  }
  public static Traveler getTraveler(String name, String version, 
                                     String hgroup, String dbType, 
                                     String datasource) 
    throws EtravelerException {
    ConcurrentHashMap<String, Traveler> travelers=s_travelers;

    String key = makeKey(name, version, hgroup, dbType);
    ProcessNode travelerRoot = null;
    Traveler traveler = null;
    if (travelers.containsKey(key)) {
      traveler = travelers.get(key);
    } else {
      // Try connect
      DbConnection conn = makeConnection(dbType, datasource);
      if (conn == null) throw new EtravelerException("Failed to connect");
      try {
        int intVersion = Integer.parseInt(version);
        ProcessNodeDb travelerDb = new ProcessNodeDb(conn, name, intVersion, 
            hgroup, null, null);
        travelerRoot = new ProcessNode(null, travelerDb);
        // Need db query to get subsystem, probably belongs in ProcessNodeDb.
        // Or maybe make a TravelerDb class??
        String subsys = travelerDb.getSubsystem(conn);
        traveler = new Traveler(travelerRoot, "db", dbType, subsys);
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

  public static ProcessNode getProcess(String name, String version, 
      String hgroup, String dbType, String datasource) 
    throws EtravelerException {
    return getTraveler(name, version, hgroup, dbType, datasource).getRoot();
  }
  public static String retrieveProcess(PageContext context)  {
    return retrieveProcess(context, true);
   
  }
   public static String retrieveProcess(PageContext context, boolean printIt)  {
    // return "retrieveProcess called with name=" + name ;
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String hgroup = context.getRequest().getParameter("traveler_hgroup");
  
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), 
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, hgroup, dbType, datasource);
    } catch (EtravelerException ex) {
      return("Failed to retrieve process with exception: " + ex.getMessage() );
    }
    if (printIt)   {
      StringArrayWriter wrt =traveler.collectOutput();
      JspWriter writer = context.getOut();
      int nLines = nLinesUsed(wrt, context);
        try {
          writer.println("<pre>");
          for (int iLine = 0; iLine < nLines; iLine++) {
            writer.println(fetchLine(wrt, context, iLine));
          }
          writer.println("</pre>");
        } catch (IOException ex) {
          System.out.println("IOException from DbImporter.displayTraveler");
        }
      
    }
      return "";
  }
   
  static private String nextLine(StringArrayWriter wrt, PageContext context) {   
    if (wrt == null)  {
      return "No information for requested name, version combination <br />";
    }
    return wrt.fetchNext();
  }
  static private String fetchLine(StringArrayWriter wrt, PageContext context, int i) {
    if (wrt == null)  {
      return "No information for requested name, version combination <br />";
    }
    return wrt.fetchLine(i);
  }
  static private int nLinesUsed(StringArrayWriter wrt, PageContext context) {
    if (wrt == null) return 0;
    return wrt.fetchNUsed();
  }
 
  static public void displayTraveler(PageContext context) {
    String ostyle =  context.getRequest().getParameter("ostyle");
    switch (ostyle) {
      case "pprint":
      case "Pretty print":
          /*   Shouldn't get here any more */
        break;
   
      case "tree":
      case "Tree":
        makeTree(context);
        break;
      case "Yaml":
        /*
         * Write both canonical and complete versions 
         * to 'archive' area, but only for LSST-CAMERA
         * NOTE: This is deprecated; No way to get to this code any more
         * from web form.
         * Instead these files are written automatically upon ingest when
         * archiving is appropriate
         */      
        String experiment = ModeSwitcherFilter.getVariable(context.getSession(), 
        "experiment");
    
        if (!(experiment.equals("LSST-CAMERA")) ) {
          String msg = "Archive export of Yaml file only available for LSST-CAMERA experiment";
          msg += "<br />Use Yaml-debug button to write local file";
          try {
             context.getOut().write("<p>" + msg + "</p>");
          } catch (IOException ex) {
            System.out.println("IOException writing results from DbImporter.displayTraveler");
          }
          return;
        }
        outputYaml(context, false);
        break;
    case "Yaml-canonical": 
    case "Yaml-verbose":
        outputYaml(context, true );
        break;
      default:
        break;
    }
  }
  
  static public String outputYaml(PageContext context, boolean localOutput) {
    HttpServletRequest request = (HttpServletRequest) context.getRequest();
    retrieveProcess(context, false);
    Traveler trav;
    try {   trav = getCachedTraveler(context); } catch (EtravelerException ex) {
      System.out.println(ex.getMessage());
      return ex.getMessage();
    }
 
    if (localOutput) {
      String key = makeKey(trav.getName(), trav.getVersion(),
                           trav.getHgroup(), trav.getSourceDb());
      HttpServletResponse response = (HttpServletResponse) context.getResponse();
      /*
       redirect to DownloadYamlServlet
      */
      try {
        String fullpath = request.getContextPath() + 
            "/servlet/DownloadYamlServlet?key=" + key +
          "&ostyle=" + context.getRequest().getParameter("ostyle");

        response.sendRedirect(fullpath);       
        return "";
      } catch (Exception ex) {
        System.out.println("could not redirect");
        return ("could not redirect");
      }
    } 
   
    JspWriter jwriter = context.getOut();

    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), "dataSourceMode");

    String archiveStatus = trav.archiveYaml((HttpServletRequest)
                                         context.getRequest(), jwriter);
    if (archiveStatus.isEmpty()) return "Wrong conditions for archiving";
    else  return archiveStatus; 
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
    String hgroup;
    JspContext jspContext=null;
    jspContext = (JspContext)context;
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), "etravelerDb");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), "dataSourceMode");
    if (reason.equals("edit") || reason.equals("NCR")) {
      name = (String)(jspContext.getAttribute("traveler_name", PageContext.SESSION_SCOPE));
      version = (String)(jspContext.getAttribute("traveler_version", PageContext.SESSION_SCOPE));
      hgroup = (String)(jspContext.getAttribute("traveler_hgroup", PageContext.SESSION_SCOPE));
    } else {
      name = context.getRequest().getParameter("traveler_name");
      version = context.getRequest().getParameter("traveler_version");
      hgroup = context.getRequest().getParameter("traveler_hgroup");
    }
    Traveler originalTraveler = null;
    ProcessNode originalTravelerRoot = null;

  
    try {
      originalTraveler = 
        getTraveler(name, version, hgroup, dbType, datasource);
      originalTravelerRoot = originalTraveler.getRoot();
    } catch (EtravelerException ex) {
      try {
        context.getOut().println("Failed to retreive process with exception: " 
            + ex.getMessage());
      } catch (IOException ioex) {}
      System.out.println("Failed to retrieve process with exception: " + ex.getMessage() );
      return;
    }
    Traveler traveler = originalTraveler;
    ProcessNode travelerRoot = originalTravelerRoot;   
    TravelerTreeVisitor vis = 
        new TravelerTreeVisitor(reason.equals("edit"), reason);
    HttpServletRequest request = (HttpServletRequest) context.getRequest();
    vis.setPath(request.getContextPath());
    try {
      if (reason.equals("edit"))  { /* make a copy */
        traveler = new Traveler(originalTraveler);
        travelerRoot = traveler.getRoot();
      }
      if (travelerRoot ==  null) {
        System.out.println("Failed to copy traveler for editing");
        return;
      }
      vis.setTraveler(traveler);
      vis.visit(travelerRoot, "build", null);
    } catch (EtravelerException ex) {
      System.out.println("Failed to build tree: " + ex.getMessage() );
      return;
    }
    if (reason.equals("edit")) {
      // Tell visitor about original traveler
      vis.setCopiedFrom(originalTravelerRoot);
    }
    jspContext.setAttribute("treeVisitor", vis, PageContext.SESSION_SCOPE);
    //}
    vis.render(context);
  }
  /**
   * In order to display traveler after yaml loading but before writing to db
   * For now, no editing allowed
   */
  static public void makePreviewTree(PageContext context, Traveler traveler) {
    TravelerTreeVisitor vis = 
        new TravelerTreeVisitor(false, "view");
    vis.setTitle("Preview tree from YAML check, subsystem='" 
        + traveler.getSubsystem() + "'");
    HttpServletRequest request = (HttpServletRequest) context.getRequest();
    vis.setPath(request.getContextPath());
    try {
      vis.visit(traveler.getRoot(), "build", null);
    } catch (EtravelerException ex) {
      System.out.println("Failed to build tree: " + ex.getMessage() );
      return;
    }
    ((JspContext)context).setAttribute("treeVisitor", vis, 
        PageContext.SESSION_SCOPE);
    vis.render(context);
  }
  static public void dotImgMap(PageContext context) {
    JspWriter outWriter = context.getOut();
    /* Make the map */
    String name = context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String hgroup = context.getRequest().getParameter("traveler_hgroup");
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(),
        "etravelerDb");
    ProcessNode traveler = null;
    try {
      traveler = getProcess(name, version, hgroup, dbType, datasource);
    } catch (EtravelerException ex) {
      System.out.println("Failed to retrieve process with exception: " + ex.getMessage() );
      return;
    }
    
    StringWriter dotWriter = new StringWriter();
    TravelerDotVisitor vis = new TravelerDotVisitor();
    
    try {
      vis.initOutput(dotWriter, "\n");
      vis.visit(traveler, "dot file", null);
      vis.endOutput();
    } catch (EtravelerException ex) {
      System.out.println("Failed to make dot file: " + ex.getMessage());
    }
    GraphViz gv = new GraphViz("dot");
    ByteArrayOutputStream bytes=null;
    try {
      bytes = gv.getGraph(dotWriter.toString(), GraphViz.Format.CMAPX);
      outWriter.println(bytes.toString());
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
          + version + "&hgroup=" + hgroup + "&db=" +dbType 
          + "\" usemap=\"#Traveler\"  />");
    } catch  (IOException ex) {
      System.out.println("Couldn't write img line");
    }
    
  }
 
  /**
   * getTraveler only accesses local data.  It tries to find traveler among 
   * those already stored (unlike getProcess which will go to db if necessary)
   * @param name      Process name
   * @param version   Process version
   * @param db
   * @return 
   */
  static public Traveler 
    getCachedTraveler(String name, String version, String hgroup, String db) 
    throws EtravelerException {
    if ((name == null) || (version == null) || (hgroup == null) || (db == null)) {
      throw new EtravelerException("Incomplete input to getCachedTraveler(name, version, hgroup, db)");
    }
    String key = makeKey(name, version, hgroup, db);
    return s_travelers.get(key);
  }
  
  static public Traveler getTravelerFromKey(String key) {
    Traveler trav = s_travelers.get(key);
    return trav;
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
    ProcessNode selected = getSelected(context);
    return selected.getAttributes();
  }
   
  static public int getPrerequisiteCount(PageContext context) {
    ProcessNode selected = getSelected(context);
    return selected.getPrerequisiteCount();
  }

  static public ArrayList<Prerequisite> getPrerequisites(PageContext context) {
    ProcessNode selected = getSelected(context);
    return selected.getPrerequisites();
  }
   
  static public int getResultCount(PageContext context) {
    ProcessNode selected = getSelected(context);
    return selected.getResultCount();
  }
  
  static public int getOptionalResultCount(PageContext context) {
    ProcessNode selected = getSelected(context);
    return  selected.getOptionalResultCount();
  }
  static public ArrayList<PrescribedResult> getResults(PageContext context) {
    ProcessNode selected = getSelected(context);
    return selected.getResults();
  }

  static public ArrayList<PrescribedResult> getOptionalResults(PageContext context) {
    ProcessNode selected = getSelected(context);
    return selected.getOptionalResults();
  }
  
  static public int getRelationshipTaskCount(PageContext context) {
    ProcessNode selected = getSelected(context);
    return selected.getRelationshipTaskCount(); 
  }
  
  static public ArrayList<RelationshipTask> getRelationshipTasks(PageContext context) {
    ProcessNode selected = getSelected(context);
    return selected.getRelationshipTasks();
  }
  
  static public boolean selectedIsRoot(PageContext context) {
    return (getSelected(context).getParent() == null);
  }
  static public boolean selectedHasChildren(PageContext context) {
    return (getSelected(context).hasChildren());
  }
  /**
   * 
   * @return true if selected node is a clone or has clones, else false 
   */
  static public boolean selectedClone(PageContext context) {
    ProcessNode selected = getSelected(context);
    return (selected.isCloned() || selected.hasClones());
  }

  static private ProcessNode getSelected(PageContext context) {
    JspContext jspContext = (JspContext) context;
    return getTreeNode(jspContext).getProcessNode();
  }
   
  static public String saveStep(PageContext context)  {
    JspContext jspContext = (JspContext) context;
    ProcessTreeNode selectedTreeNode = getTreeNode(jspContext);
    ProcessNode selected = getSelected(context);

    boolean changeAll = false;
    String submitValue =context.getRequest().getParameter("save");
    if (submitValue.endsWith("all instances")) {
      changeAll = true;
      if (selected.isCloned())  { //modify the big brother node first
        selected = selected.clonedFrom();
      }
    }

    // check validity of proposed changes
    String valid = checkStep(context, selected);
    if (!valid.isEmpty()) return "<p class='warning'" + valid + "</p>";

    // See if there actually *are* any changes and, if so, make them to selected
    boolean changed = makeChanges(context, selected);

    if (changed) {
      TravelerTreeVisitor vis = 
        (TravelerTreeVisitor) jspContext.getAttribute("treeVisitor", 
                                                      PageContext.SESSION_SCOPE);
      if (submitValue.equals("Save edit")) {
        vis.addEdited(selectedTreeNode, "modified");
        selected.newVersion();
      } else       if (submitValue.endsWith("all instances")) {
        vis.addEdited(selectedTreeNode, "modified all");
        selected.newVersion();
        selected.updateBuddies(false);
      } else       if (submitValue.endsWith("this instance")) {
         // not yet supported
      }
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
  static public ArrayList<EditedTreeNode>
    listEdited(PageContext context) {
    JspContext jspContext = (JspContext)context;
    TravelerTreeVisitor vis = (TravelerTreeVisitor) 
      jspContext.getAttribute("treeVisitor", 
                              PageContext.SESSION_SCOPE);   
    return vis.getEdited();  
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
    String exitTreeNodeId = context.getSession().getAttribute("exitTreeNodeId").toString();
      String returnTreeNodeId = context.getSession().getAttribute("returnTreeNodeId").toString();
    String ncrCondition = 
      context.getSession().getAttribute("NCRCondition").toString();
    ProcessNode exitProcess = 
        (vis.findNodeFromPath(exitPath, exitTreeNodeId)).getProcessNode();
    ProcessNode returnProcess = 
      (vis.findNodeFromPath(returnPath, returnTreeNodeId)).getProcessNode();

    /* Get a suitable (write) db connection */
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
        "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(context.getSession(), 
        "etravelerDb");
    
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
  
  static private Traveler getCachedTraveler(PageContext context) 
    throws EtravelerException {
    String name =  context.getRequest().getParameter("traveler_name");
    String version = context.getRequest().getParameter("traveler_version");
    String hgroup = context.getRequest().getParameter("traveler_hgroup");
    
    String dbType = ModeSwitcherFilter.getVariable(context.getSession(), 
                                                   "dataSourceMode");
    return getCachedTraveler(name, version, hgroup, dbType);
  }
    
  /* Probably need to do something here for relationship tasks */
  static private String checkStep(PageContext context, ProcessNode selected) {
    String newVal = null;
    String ret="";
    if (selected.getParent() != null)  {
      newVal = context.getRequest().getParameter("maxIt");
      if (!newVal.equals(selected.getMaxIteration()) ) {
        ret = Verify.isPosInt(newVal);
        if (!ret.isEmpty()) return ret;
      }
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
    String resCheck;
    if (selected.getResultCount() > 0) {
      resCheck = checkResults(context, selected.getResults(), false);
      if (!resCheck.isEmpty()) return resCheck;
    }
    // Need to do the same for the optional ones
    if (selected.getOptionalResultCount() > 0) {
      resCheck = checkResults(context, selected.getOptionalResults(), true);
      if (!resCheck.isEmpty()) return resCheck;
    }
    return "";
  }

  private static String checkResults(PageContext context, 
                                     ArrayList<PrescribedResult> results,
                                     boolean optional) {
    String suffix="";
    if (optional) suffix="Optional";
    for (int i=0; i < results.size(); i++) {
      
      if (results.get(i).numberSemantics()) {
        String minValue = 
          context.getRequest().getParameter(genId("min"+suffix, i));
        if (minValue == null) minValue="";
        String maxValue = 
          context.getRequest().getParameter(genId("max"+suffix, i));
        if (maxValue == null) maxValue="";
        if (minValue.isEmpty() && maxValue.isEmpty()) continue;
        boolean haveBoth = !(minValue.isEmpty() || maxValue.isEmpty());
        String ok="";
        if (results.get(i).getSemantics().equals("int")) {
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
    return "";
  }

  private static boolean makeChanges(PageContext context, 
                                     ProcessNode selected)  {
    String newVal;
    boolean changed = false;

    if (!selectedIsRoot(context)) {
      newVal = context.getRequest().getParameter("maxIt");
      if (!newVal.equals(selected.getMaxIteration()) ) { 
        selected.setMaxIteration(newVal);          
        changed = true;
      }
    }
    newVal = context.getRequest().getParameter("description");
    if (!newVal.equals(selected.getDescription()) ) {
      selected.setDescription(newVal);
      changed = true;
    }
    newVal = context.getRequest().getParameter("shortDescription");
    if (!newVal.equals(selected.getShortDescription()) ) {
      selected.setShortDescription(newVal);
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
  
    int cnt = selected.getPrerequisiteCount();
    boolean rmPrereq = false;
    ArrayList<Integer> changedPrereq =
        new ArrayList<Integer>(cnt);
    for (int iPre=0; iPre < cnt; iPre++ ) {
      changedPrereq.add(iPre, 
          savePrereq(context.getRequest(), selected.getPrerequisites().get(iPre), iPre));
      changed |= (changedPrereq.get(iPre) > 0);
      rmPrereq |= (changedPrereq.get(iPre) > 1);
    }

    cnt = selected.getResultCount();
    boolean rmResult = false;
    ArrayList<Integer> changedResult =
        new ArrayList<Integer>(cnt);
    for (int iRes=0; iRes < cnt; iRes++ ) {
      changedResult.add(iRes, 
          saveResult(context.getRequest(),selected.getResults().get(iRes),
                     iRes, false));
      changed |= (changedResult.get(iRes) > 0);
      rmResult |= (changedResult.get(iRes) > 1);
    }   

    //


    cnt = selected.getOptionalResultCount();
    boolean rmOptionalResult = false;
    ArrayList<Integer> changedOptionalResult =
        new ArrayList<Integer>(cnt);
    for (int iOptRes=0; iOptRes < cnt; iOptRes++ ) {
      changedOptionalResult.add(iOptRes, 
          saveResult(context.getRequest(),
                     selected.getOptionalResults().get(iOptRes),iOptRes, true));
      changed |= (changedOptionalResult.get(iOptRes) > 0);
      rmResult |= (changedOptionalResult.get(iOptRes) > 1);
    }   

    //
    if (rmPrereq) {
      selected.rmPrereqs(changedPrereq);
    }
    if (rmResult) {
      selected.rmResults(changedResult);
    }
    if (rmOptionalResult) {
      selected.rmOptionalResults(changedOptionalResult);
    }
    return changed;
  }

  /**
   * Update stored Prerequisite according to form input 
   * @param req   servlet request 
   * @param pre   Prerequisite to be updated
   * @param iPre  prerequisite number, needed to form ids for form fields
   * @return   0 if prereq not changed; 1 if modified; 2 if deleted 
   */
  static private int savePrereq(ServletRequest req, Prerequisite pre, int iPre)  {
    boolean changed = false;
     
    String newVal = req.getParameter(genId("removePrereq", iPre));
    if (newVal != null) {
      if (!newVal.isEmpty()) return 2;
    }  
    newVal = req.getParameter(genId("prereqDescrip", iPre));
    changed |= pre.setDescription(newVal);
    
    newVal = req.getParameter(genId("count", iPre));
    changed |= pre.setQuantity(newVal);
    
    if ((pre.getType()).equals("PROCESS_STEP")) {
      newVal = req.getParameter(genId("userVersion", iPre));
      changed |= pre.setUserVersionString(newVal);
    }
    return (changed ? 1 : 0);
  }
  static private int saveResult(ServletRequest req, PrescribedResult result, 
                                int iRes, boolean isOptional)  {
    boolean changed = false;
    String suffix="";
    if (isOptional) suffix="Optional";
    
    String newVal = req.getParameter(genId("removeResult"+suffix, iRes));
    if (newVal != null) {
      if (!newVal.isEmpty()) return 2;
    }
    newVal = req.getParameter(genId("resultDescrip"+suffix, iRes));
    changed |= result.setDescription(newVal);
    
    if (result.numberSemantics()) {
      newVal = req.getParameter(genId("units"+suffix, iRes));
      changed |= result.setUnits(newVal);
      
      newVal = req.getParameter(genId("min"+suffix, iRes));
      changed |= result.setMinValue(newVal);
      
      newVal = req.getParameter(genId("max"+suffix, iRes));
      changed |= result.setMaxValue(newVal);
      
    }
    return (changed ? 1 : 0);
  }
  static private String genId(String nm, int i) {
    return nm + "_" + Integer.toString(i);
  }
 
  static private ProcessTreeNode getTreeNode(JspContext jspContext) {
    TravelerTreeVisitor vis = (TravelerTreeVisitor) 
      jspContext.getAttribute("treeVisitor", 
                              PageContext.SESSION_SCOPE);

    String nodePath=(String) jspContext.getAttribute("nodePath", 
                                                     PageContext.SESSION_SCOPE);
    String treeNodeId = (String) jspContext.getAttribute("treeNodeId", 
        PageContext.SESSION_SCOPE);

    return vis.findNodeFromPath(nodePath, treeNodeId);
  }
}
