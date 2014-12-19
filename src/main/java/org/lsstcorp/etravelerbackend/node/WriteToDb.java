package org.lsstcorp.etravelerbackend.node;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import org.lsstcorp.etravelerbackend.db.DbInfo;
import org.lsstcorp.etravelerbackend.db.DbConnection;
import org.lsstcorp.etravelerbackend.db.MysqlDbConnection;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;
/**
 * Used from jsp to ingest Yaml, export to Db
 * Also used to write modified travelers; exception entries
 * @author jrb
 */
public class WriteToDb {

  // public static String ingest(String fileContents, boolean useTransactions) {
    public static String ingest(PageContext context) {
      String fileContents = context.getRequest().getParameter("importYamlFile");
      if (fileContents == null || fileContents.isEmpty())  {
        return "No file selected or stale reference. <br /> "
            + "Please hit back button, refresh page, reselect file and try again";
      }
      String useTransactions = "true";
      if (context.getAttribute("useTransaction") != null) {
        useTransactions = (context.getAttribute("useTransactions")).toString();
      }
      String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
          "dataSourceMode");
      String datasource = ModeSwitcherFilter.getVariable(context.getSession(),
          "etravelerDb");
      JspWriter writer = context.getOut();

      String action = context.getRequest().getParameter("fileAction");
      ProcessNode ingested;
      try {
        ingested = parse(fileContents);
        if (ingested == null) return "Could not parse yaml input";
      }  catch (Exception ex)  {
        //if (ingested ==  null) {
        /*
        try {
          writer.println("<p>Could not parse yaml input.  Failed with exception</br>");
          writer.println(ex.getMsg() + "</p>");
        } catch (IOException ex)  {}
        */
        return  "<b>" + ex.getMessage() + "</b>";
      }
      if (action.equals("Check")) {
        return "File successfully parsed";
      }
      String writeRet =
          writeToDb(ingested, context.getSession().getAttribute("userName").toString(),
          useTransactions.equals("true"), dbType, datasource);
      return writeRet;
  }

  private static ProcessNode parse(String fileContents) throws Exception  {    
    Yaml yaml = new Yaml();
    Map yamlMap = null;
    try {
      yamlMap = (Map<String, Object>) yaml.load(fileContents);
    } catch (Exception ex) {
      System.out.println("Failed to load yaml with exception " + ex.getMessage());
      throw new EtravelerException("Failed to load yaml with exception " 
          + ex.getMessage());
    }
    ProcessNodeYaml topYaml = new ProcessNodeYaml();
    try {
      topYaml.readYaml(yamlMap, null, false, 0, null);
    } catch (Exception ex) {
      System.out.println("Failed to process yaml with exception " + ex.getMessage());
      throw new EtravelerException("Failed to load yaml with exception " 
          + ex.getMessage());  
    }
    // System.out.println("Loaded file into Map of size  " + yamlMap.size());
    ProcessNode traveler;
    try {
      traveler = new ProcessNode(null, topYaml);
    } catch (Exception ex) {
      System.out.println("Failed to import from yaml with exception " + ex.getMessage());
      // return null;
      throw new EtravelerException("Failed to import from yaml with exception "
          + ex.getMessage());
    }
    return traveler;
  }

  public static String writeToDb(ProcessNode travelerRoot, String user,
      boolean useTransactions, String dbType, String datasource)  {

    // Try connect
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
      return "Failed to create xxDb classes with exception " + ex.getMessage();
    }
    try {
      vis.visit(travelerRoot, "verify", null);
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to verify against " + dbType + 
          " db with exception " + ex.getMessage();
    }
    try {
      vis.visit(travelerRoot, "write", null);
    }  catch (Exception ex) {
      conn.close();
      return "Failed to write to " + dbType + 
          " db with exception " + ex.getMessage();
    }
    conn.close();
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
      return "Failed to create xxDb classes with exception " + ex.getMessage();
    }
    try {
      vis.visit(ncr, "verify");
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to verify against " + dbType + 
          " db with exception " + ex.getMessage();
    }
    try {
      vis.visit(ncr, "write");
    }  catch (Exception ex) {
      conn.close();
      return "Failed to write to " + dbType + 
          " db with exception " + ex.getMessage();
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
   //String datasource = ModeSwitcherFilter.getVariable(session or request, "etravelerDb");
 

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
