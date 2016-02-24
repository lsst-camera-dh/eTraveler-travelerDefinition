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
//import javax.servlet.ServletRequest;
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

  public static void ingest(PageContext cxt) throws IOException {
  
    String redoInstructions = " Reselect file and try again <br />"; 
    boolean nameWarning=true;
    JspWriter wrt = cxt.getOut();
    HttpServletRequest req = (HttpServletRequest) cxt.getRequest();
      
    Object fileContentsObj = req.getParameter("importYamlFile");
    //nameWarning = (cxt.getRequest().getParameter("strictNameChecking") != null); 
  
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
    if (cxt.getAttribute("useTransaction") != null) {
      useTransactions = (cxt.getAttribute("useTransactions")).toString();
    }
    String dbType = ModeSwitcherFilter.getVariable(cxt.getSession(),
                                                   "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(cxt.getSession(),
          "etravelerDb");
     
    String action = req.getParameter("fileAction").toString();
      
    if (action.equals("Import"))   {
      if (req.getParameter("owner").trim().isEmpty() ||
          req.getParameter("reason").trim().isEmpty() ) {
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
    DbImporter.makePreviewTree(cxt, trav);
    if (action.equals("Check YAML")) {
      wrt.write("File successfully parsed <br />");
      return;
    }
    String writeRet;
    if (action.equals("Db validate")) {
      writeRet=
        trav.writeToDb(cxt.getSession().getAttribute("userName").toString(),
                       useTransactions.equals("true"),action.equals("Import"),
                       "", "", req, wrt);
      if (writeRet.isEmpty()) {
        writeRet = "Traveler successfully verified against db " + dbType;
      }  else  {
          wrt.write("<br />" + writeRet + "<br />");
          return;
      }
    } else {
      String reason = req.getParameter("reason").trim();
      String owner = req.getParameter("owner").trim();
        
      writeRet =
        trav.writeToDb(cxt.getSession().getAttribute("userName").toString(),
                       useTransactions.equals("true"),action.equals("Import"),
                       reason, owner, req, wrt);
      if (writeRet.isEmpty()) {
        writeRet = "Traveler successfully ingested into " + dbType + " db";
      }
      wrt.write("<br />" + writeRet + "<br />");
      return;
    }
  }

  public static String writeNCRToDb(NCRSpecification ncr, String user, 
                                    boolean useTransactions, String dbType,
                                    String dataSource) {
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

  static private DbConnection makeConnection(String dbType, String datasource)
  {
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
