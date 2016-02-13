package org.lsst.camera.etraveler.backend.ui;
import org.lsst.camera.etraveler.backend.ui.DbImporter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Map;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import org.lsst.camera.etraveler.backend.db.MysqlDbConnection;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import org.lsst.camera.etraveler.backend.node.NCRSpecification;
import org.lsst.camera.etraveler.backend.node.ProcessNode;
import org.lsst.camera.etraveler.backend.node.ProcessNodeYaml;
import org.lsst.camera.etraveler.backend.node.Traveler;
import org.lsst.camera.etraveler.backend.node.TravelerToDbVisitor;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;
/**
 * Used from jsp to ingest Yaml, export to Db
 * Also used to write modified travelers; exception entries
 * @author jrb
 */
public class WriteToDb {

    public static void ingest(PageContext context) throws IOException {
  
      String redoInstructions = " Reselect file and try again <br />"; 
      boolean nameWarning=false;
      JspWriter wrt = context.getOut();
      
      Object fileContentsObj = context.getRequest().getParameter("importYamlFile"); 
      nameWarning = (context.getRequest().getParameter("strictNameChecking") != null); 
  
      if (fileContentsObj == null)  {
        wrt.write("No file selected or stale reference. <br /> "
            + redoInstructions);
        return;
      }
      String fileContents = fileContentsObj.toString();
      if (fileContents.isEmpty()) {
          wrt.write("empty file contents string<br />" + redoInstructions);
          return;
      }
      String useTransactions = "true";
      if (context.getAttribute("useTransaction") != null) {
        useTransactions = (context.getAttribute("useTransactions")).toString();
      }
      String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
          "dataSourceMode");
      String datasource = ModeSwitcherFilter.getVariable(context.getSession(),
          "etravelerDb");
     

      String action = context.getRequest().getParameter("fileAction").toString();
      
      if (action.equals("Import"))   {
        if (context.getRequest().getParameter("owner").trim().isEmpty() ||
            context.getRequest().getParameter("reason").trim().isEmpty() ) {
          wrt.write
              ("Must specify <b>Description</b> and <b>Responsible Person</b> "
              + " to ingest! <br />");
          return;
        }
      }
      Traveler trav;
      ProcessNode ingested;
      try {
        trav = new Traveler(fileContents, nameWarning, wrt, "<br />");
        if (trav.getRoot() == null) {
            wrt.write("Could not parse yaml input <br />");
            return;
        }
        
      }  catch (Exception ex)  {
        wrt.write("<b>" + ex.getMessage() + "</b>");
        return;
      }
      ingested = trav.getRoot();
      DbImporter.makePreviewTree(context, trav);
      if (action.equals("Check YAML")) {
   
        wrt.write("File successfully parsed <br />");
        return;
      }
      String writeRet;
      if (action.equals("Db validate")) {
        writeRet=writeToDb(trav, context.getSession().getAttribute("userName").toString(),
          useTransactions.equals("true"), dbType, datasource, action.equals("Import"),
                    "","",null, wrt); 
      } else {
      writeRet =
          writeToDb(trav, 
                    context.getSession().getAttribute("userName").toString(),
                    useTransactions.equals("true"), dbType, datasource, 
                    action.equals("Import"),
                    context.getRequest().getParameter("owner").trim(), 
                    context.getRequest().getParameter("reason").trim(),
                    (HttpServletRequest) context.getRequest(), wrt);
      }
      wrt.write(writeRet + "<br />");
      return;
    }

  public static String writeToDb(Traveler traveler, String user,
                                 boolean useTransactions, String dbType, 
                                 String datasource, boolean ingest,
                                 String owner, String reason,
                                 HttpServletRequest req, Writer writer)  {

    // Try connect
    ProcessNode travelerRoot = traveler.getRoot();
    DbConnection conn = makeConnection(dbType, datasource);
    if (conn == null) return "Failed to connect";
    conn.setSourceDb(dbType);

    TravelerToDbVisitor vis = new TravelerToDbVisitor(conn);
    vis.setUseTransactions(useTransactions);
    vis.setUser(user);

    // Convert to db-like classes, e.g. ProcessNodeDb   
    // next visit with activity "verify", then "write".
    try {
      vis.visit(travelerRoot, "new", null);
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to create xxDb classes with exception '" + 
          ex.getMessage() +"'";
    }
    try {
      vis.setSubsystem(traveler.getSubsystem());
      vis.visit(travelerRoot, "verify", null);
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to verify against " + dbType + 
          " db with exception '" + ex.getMessage() + "'";
    }
    if (!ingest) {
      conn.close();
      return "Successfully verified against " + dbType;
    }
    try {
      vis.setReason(reason);
      vis.setOwner(owner);
      vis.visit(travelerRoot, "write", null);
    }  catch (Exception ex) {
      conn.close();
      return "Failed to write to " + dbType + 
          " db with exception '" + ex.getMessage() + "'";
    }
    conn.close();
    Traveler trav=null;
    try {
      trav = 
          DbImporter.getTraveler(vis.getTravelerName(), vis.getTravelerVersion(),
          vis.getTravelerHardwareGroup(), dbType, datasource);
    } catch (Exception ex) {
      return "Failed to retrieve traveler from db with exception" + ex.getMessage();
      // add to message that file couldn't be archived
    }
    // Now have everything to call archiveYaml
    trav.archiveYaml(req, writer);
    return "successfully wrote traveler to " + dbType + " db";
  }
  
  public static String writeNCRToDb(NCRSpecification ncr, String user, 
      boolean useTransactions, String dbType, String dataSource) {
    if (!dbType.equals(ncr.getDbType())) return "db type match failure";
    DbConnection conn = makeConnection(dbType, dataSource);
    if (conn == null) return "Unable to get db connection for " + dbType;
    conn.setSourceDb(dbType);
    TravelerToDbVisitor vis = new TravelerToDbVisitor(conn);
    vis.setUseTransactions(useTransactions);
    vis.setUser(user);
  
    try {
      vis.visit(ncr, "new");
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to create xxDb classes with exception '" 
          + ex.getMessage() + "'";
    }
    try {
      vis.visit(ncr, "verify");
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to verify against " + dbType + 
          " db with exception '" + ex.getMessage() + "'";
    }
    try {
      vis.visit(ncr, "write");
    }  catch (Exception ex) {
      conn.close();
      return "Failed to write to " + dbType + 
          " db with exception '" + ex.getMessage() + "'";
    }
    conn.close();
    
    return "";
  }
  static private DbConnection makeConnection(String dbType)  {   
    // Try connect
    DbConnection conn = new MysqlDbConnection();
    String datasource = "jdbc/eTraveler-" + dbType + "-app";
    boolean isOpen = conn.openTomcat(datasource);
    if (isOpen) {
      try {
        conn.setReadOnly(false);
      } catch (Exception ex) {
        conn.close();
        System.out.println("Unable to set connection non-readonly");
        return null;
      }
      System.out.println("Successfully connected to " + datasource);
      
      return conn;
    }
    else {
      System.out.println("Failed to connect for dbType " + dbType);
      return null;
    }
  }
  static private DbConnection makeConnection(String dbType, String datasource)  {
    DbConnection conn = new MysqlDbConnection();
    conn.setSourceDb(dbType);
    boolean isOpen = conn.openTomcat(datasource);
    if (isOpen) {
      try {
        conn.setReadOnly(false);
      } catch (Exception ex) {
        conn.close();
        System.out.println("Unable to set connection non-readonly");
        return null;
      }
      System.out.println("Successfully connected to " + datasource);    
      return conn;
    }
    else {
      System.out.println("Failed to connect");
      return null;
    }
  }  
}
