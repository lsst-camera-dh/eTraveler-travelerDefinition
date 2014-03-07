/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import org.lsstcorp.etravelerbackenddb.DbInfo;
import org.lsstcorp.etravelerbackenddb.DbConnection;
import org.lsstcorp.etravelerbackenddb.MysqlDbConnection;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.jsp.PageContext;
/**
 * Used from jsp to ingest Yaml, export to Db
 * @author jrb
 */
public class YamlToDb {
 /*
    public static void newingest(PageContext context) {
      String fileContents = context.getRequest().getParameter("importYamlFile");

      Object useTransactions = context.getAttribute("useTransactions"); 
      
      System.out.println("The fileContents are "+fileContents);
      
      System.out.println("Use transactions "+useTransactions);
      
      System.out.println("And the user is "+context.getSession().getAttribute("userName"));
      
    }
*/
  // public static String ingest(String fileContents, boolean useTransactions) {
    public static String ingest(PageContext context) {
      String fileContents = context.getRequest().getParameter("importYamlFile");
      String useTransactions = (context.getAttribute("useTransactions")).toString();
      ProcessNode ingested = parse(fileContents);
      if (ingested ==  null) {
        return "Could not parse yaml input";
      }
      return writeToDb(ingested, context.getSession().getAttribute("userName").toString(),
          useTransactions.equals("true"));
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

  private static String writeToDb(ProcessNode traveler, String user,
      boolean useTransactions)  {

    // Try connect
    DbConnection conn = makeConnection();
    if (conn == null) return "Failed to connect";

    TravelerToDbVisitor vis = new TravelerToDbVisitor(conn);
    vis.setUseTransactions(useTransactions);
    vis.setUser(user);

    // Convert to db-like classes, e.g. ProcessNodeDb

    
    // next visit with activity "verify", then "write".
    try {
      vis.visit(traveler, "new");
    }  catch (Exception ex)  {
      return "Failed to create xxDb classes with exception " + ex.getMessage();
    }
    try {
      vis.visit(traveler, "verify");
    }  catch (Exception ex)  {
      return "Failed to verify against db with exception " + ex.getMessage();
    }
    try {
      vis.visit(traveler, "write");
    }  catch (Exception ex) {
      return "Failed to write to db with exception " + ex.getMessage();
    }
    return "successfully verified traveler";


    // These probably can throw exceptions
  }
  static private DbConnection makeConnection()  {   
    // Try connect
    DbConnection conn = new MysqlDbConnection();
    boolean isOpen = conn.openTomcat("jdbc/eTraveler-test-app");
    if (isOpen) {
      System.out.println("Successfully connected to rd_lsst_camt");
      
      return conn;
    }
    else {
      System.out.println("Failed to connect");
      return null;
    }
  }
  
}
