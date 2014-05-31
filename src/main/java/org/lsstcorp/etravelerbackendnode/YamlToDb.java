/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Map;
import org.lsstcorp.etravelerbackenddb.DbInfo;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.lsstcorp.etravelerbackenddb.MysqlDbConnection;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.jsp.PageContext;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;
/**
 * Used from jsp to ingest Yaml, export to Db
 * @author jrb
 */
public class YamlToDb {

  // public static String ingest(String fileContents, boolean useTransactions) {
    public static String ingest(PageContext context) {
      String fileContents = context.getRequest().getParameter("importYamlFile");
      String useTransactions = (context.getAttribute("useTransactions")).toString();
      String dbType = ModeSwitcherFilter.getVariable(context.getSession(),
          "dataSourceMode");
      String datasource = ModeSwitcherFilter.getVariable(context.getSession(),
          "etravelerDb");
      // String dbType = context.getRequest().getParameter("db");
      ProcessNode ingested = parse(fileContents);
      if (ingested ==  null) {
        return "Could not parse yaml input";
      }
      return writeToDb(ingested, context.getSession().getAttribute("userName").toString(),
          useTransactions.equals("true"), dbType, datasource);
  }

  private static ProcessNode parse(String fileContents)  {    
    Yaml yaml = new Yaml();
    Map yamlMap = null;
    try {
      yamlMap = (Map<String, Object>) yaml.load(fileContents);
    } catch (Exception ex) {
      System.out.println("failed to load yaml with exception " + ex.getMessage());
      return null;
    }
    ProcessNodeYaml topYaml = new ProcessNodeYaml();
    try {
      topYaml.readYaml(yamlMap, null, false, 0, null);
    } catch (Exception ex) {
      System.out.println("failed to process yaml with exception " + ex.getMessage());
      return null;
    }
    System.out.println("Loaded file into Map of size  " + yamlMap.size());
    ProcessNode traveler;
    try {
      traveler = new ProcessNode(null, topYaml);
    } catch (Exception ex) {
      System.out.println("failed to import from yaml with exception " + ex.getMessage());
      return null;
    }
    return traveler;
  }

  private static String writeToDb(ProcessNode travelerRoot, String user,
      boolean useTransactions, String dbType, String datasource)  {

    // Try connect
    DbConnection conn = makeConnection(dbType, datasource);
    conn.setSourceDb(dbType);
    if (conn == null) return "Failed to connect";

    TravelerToDbVisitor vis = new TravelerToDbVisitor(conn);
    vis.setUseTransactions(useTransactions);
    vis.setUser(user);

    // Convert to db-like classes, e.g. ProcessNodeDb

    
    // next visit with activity "verify", then "write".
    try {
      vis.visit(travelerRoot, "new");
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to create xxDb classes with exception " + ex.getMessage();
    }
    try {
      vis.visit(travelerRoot, "verify");
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to verify against " + dbType + 
          " db with exception " + ex.getMessage();
    }
    try {
      vis.visit(travelerRoot, "write");
    }  catch (Exception ex) {
      conn.close();
      return "Failed to write to " + dbType + 
          " db with exception " + ex.getMessage();
    }
    conn.close();
    return "successfully verified traveler";


    // These probably can throw exceptions
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
