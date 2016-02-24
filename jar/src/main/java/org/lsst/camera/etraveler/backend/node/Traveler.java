/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.yaml.snakeyaml.Yaml;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import org.lsst.camera.etraveler.backend.db.MysqlDbConnection;
import org.lsst.camera.etraveler.backend.util.SessionData;


/**
 * Root of a process tree plus some associated information: where
 * it came from, what db (if any) it is supposed to be compatible with
 * @author jrb
 */
public class Traveler {
  private ProcessNode m_root=null;
  private String m_source=null;  // e.g. db, yaml
  private String m_sourceDb="[none]";  // dataSourceMode value or "[none]"
  private String m_subsystem=null;

  /**
   *   Create Traveler wrapper for in-memory traveler def (i.e.,
   *   process nodes are in-memory) 
   *   @param root    root process node
   *   @param source  e.g. db, yaml
   *   @param sourceDb
   *   @param subsystem
   */
  public Traveler(ProcessNode root, String source, String sourceDb, 
      String subsystem) {
    m_root=root;
    m_source = source;
    m_sourceDb = sourceDb;
    m_subsystem=subsystem;
  }
  /**
   *   Create Traveler wrapper for in-memory traveler def (i.e.,
   *   process nodes are in-memory) 
   *   @param root    root process node
   *   @param source  e.g. db, yaml
   *   @param sourceDb
   */
  public Traveler(ProcessNode root, String source, String sourceDb) {
    m_root = root;
    m_source = source;    // check for validity?
    m_sourceDb = sourceDb;
  }
  /**
   *   Create Traveler wrapper for in-memory traveler def (i.e.,
   *   process nodes are in-memory) 
   *   @param root    root process node
   *   @param source  e.g. db, yaml
   */
  public Traveler(ProcessNode root, String source)  {
    m_root = root;
    m_source = source;
  }
  /**
   *  Constructor from yaml input
   *  @param fileContents
   *  @param nameWarning  If true just warn; else call it an error
   *  @param wrt  Used to write error messages or warnings
   */
   public Traveler(String fileContents, boolean nameWarning,
                   Writer wrt, String eol)  throws IOException {    
    Yaml yaml = new Yaml(true);
    Map yamlMap = null;
    String nameHandling="none";
    if (nameWarning) nameHandling="warn";
    else nameHandling="error";
    try {
      yamlMap = (Map<String, Object>) yaml.load(fileContents);
    } catch (Exception ex) {
      System.out.println("Failed to load yaml with exception " + ex.getMessage());
      wrt.write("Failed to load yaml with exception '"
                + ex.getMessage() + "'" + eol);
      return;
    }
    ProcessNodeYaml topYaml = new ProcessNodeYaml(wrt, eol, nameHandling);
    boolean namesOk = true;
    try {
      namesOk = topYaml.readYaml(yamlMap, null, false, 0, null);
    } catch (Exception ex) {
      System.out.println("Failed to process yaml with exception '" 
          + ex.getMessage() + "'");
      wrt.write("Failed to load yaml with exception '" 
          + ex.getMessage() + "'" + eol);
      return;
    }
    if (!namesOk) {
        System.out.println("Failed to process yaml due to name errors");
        wrt.write("Failed to load yaml due to name errors" + eol);
        return;
    }

    ProcessNode rootNode;
    try {
      rootNode = new ProcessNode(null, topYaml);
      m_root = rootNode;
      m_source = "yaml";
      m_subsystem = topYaml.getSubsystem();
    } catch (Exception ex) {
      System.out.println("Failed to import from yaml with exception " + ex.getMessage());
      wrt.write("Failed to import from yaml with exception '" +
                ex.getMessage() +"'" + eol);
    }
  }

  /**
   *  Deep copy constructor
   * @param toCopy 
   */
  public Traveler(Traveler toCopy)  {
    m_source = toCopy.m_source;
    m_sourceDb= toCopy.m_sourceDb;
    m_subsystem = toCopy.m_subsystem;
    try {
      m_root = new ProcessNode(null, toCopy.m_root, 0);
    } catch (EtravelerException ex) {
      m_root =  null;
    }
  }
  public ProcessNode getRoot() {return m_root;}
  public String getSource() {return m_source;}
  public String getSourceDb() {return m_sourceDb;}
  public String getSubsystem() {return m_subsystem;}
  public String getName() {return m_root.getName();}
  public String getVersion() {return m_root.getVersion();}
  public String getHgroup() {return m_root.getHardwareGroup(); }

  public String archiveYaml(HttpServletRequest req, Writer errWriter) {
    return archiveYaml(new SessionData(req), errWriter);
  }
  
  public String archiveYaml(SessionData sessionData, Writer errWriter) {
    String dbType = sessionData.getDbType();

    // only archive if db is Prod or Raw
    if ((!dbType.equals("Prod")) && (!dbType.equals("Raw")) ) {
      return "";
    }
    String archiveDir = sessionData.getFileStore();
    if (sessionData.getLocalhost()) {
        System.out.println("Official archiveDir was " + archiveDir);
        archiveDir = System.getenv("HOME") + "/localET";
    }

    FileWriter fileOutCanon = null;
    FileWriter fileOutVerbose = null;

    String dirname = archiveDir;
    dirname += "/yaml/";
    dirname += dbType;
    String fname =  dirname + "/" + this.getName() + "_" +
      this.getVersion() + "_" + this.getHgroup() + "_" + this.getSourceDb();
    String[ ] fnames = new String[2];

    String summary = " archiveYaml: Files written to " + fname + "_verbose.yaml, " + fname+ "_canonical.yaml";
    String msg = "";
    try {
      File dir = new File(dirname);
      if (!dir.isDirectory())  {
        dir.mkdirs();
      }
      fileOutVerbose = new FileWriter(fname + "_verbose.yaml");
      fileOutCanon = new FileWriter(fname + "_canonical.yaml");
    } catch (Exception ex)  {
      msg = " archiveYaml: unable to open output file" + fname + " or " + fname + "_canonical";
      System.out.println(msg);
      return msg;
    }
    if (msg.isEmpty()) msg = outputYaml(fileOutVerbose, true);
    if (msg.isEmpty()) msg = outputYaml(fileOutCanon, false);
    
    if (msg.isEmpty()) {
      return summary;
    } else {
      return " archiveYaml failure: " + msg;
    }
  }
  /**
   *  Output yaml representation of traveler to supplied writer
   */
  public String outputYaml(Writer writer, boolean includeDebug)  {
    TravelerToYamlVisitor vis = new TravelerToYamlVisitor(m_sourceDb);
    vis.setIncludeDbInternal(includeDebug);
    vis.setSubsystem(this.getSubsystem());
    try {
      vis.visit(this.getRoot(), "", null);
    } catch (EtravelerException ex) {
      return("outputYaml failed with exception" + ex.getMessage());
    } 
 
    String msg = vis.dump(writer);
    try {
      writer.close();
    } catch (IOException ioEx) {
      msg = "outputYaml unable to close file with exception "
        + ioEx.getMessage();
    }
    return msg;
  }

  /**
   *  @param user  user id of person requesting write
   *  @param useTransactions  normally true
   *  @param ingest    true if traveler is to be ingested; else only
   *                   validate against db
   *  @param reason    Purpose of traveler or traveler update.
   *                   Stored in db if ingest is true, else not used
   *  @param owner     Person responsible for traveler content
   *                   Stored in db if ingest is true, else not used
   *  @param sessionData
   *  @param errWriter Used for error output
   *  @return          Summary status string     
   */
  public String writeToDb(String user, boolean useTransactions, 
                          boolean ingest, String reason, String owner,
                          HttpServletRequest req, Writer errWriter) 
  throws IOException {
    return writeToDb(user, useTransactions, ingest, reason, owner,
                     new SessionData(req), errWriter);
  }

  /**
   *  @param user  user id of person requesting write
   *  @param useTransactions  normally true
   *  @param ingest    true if traveler is to be ingested; else only
   *                   validate against db
   *  @param reason    Purpose of traveler or traveler update.
   *                   Stored in db if ingest is true, else not used
   *  @param owner     Person responsible for traveler content
   *                   Stored in db if ingest is true, else not used
   *  @param sessionData
   *  @param errWriter Used for error output
   *  @return          Summary status string     
   */
  public String writeToDb(String user, boolean useTransactions, 
                          boolean ingest, String reason, String owner,
                          SessionData sessionData, Writer errWriter) 
  throws IOException {

    String dbType = sessionData.getDbType();
    
    // Try connect
    DbConnection conn = makeConnection(sessionData);
    if (conn == null){
      errWriter.write("Failed to connect to db " + dbType);
      return "Ingest failed";
    }
    conn.setSourceDb(dbType);

    TravelerToDbVisitor vis = new TravelerToDbVisitor(conn);
    vis.setUseTransactions(useTransactions);
    vis.setUser(user);

    // Convert to db-like classes, e.g. ProcessNodeDb   
    // next visit with activity "verify", then "write".
    try {
      vis.visit(m_root, "new", null);
    }  catch (Exception ex)  {
      conn.close();
      errWriter.write("Failed to create xxDb classes with exception '" + 
                      ex.getMessage() + "'");
      return "Ingest failed";
    }
    try {
      vis.setSubsystem(m_subsystem);
      vis.visit(m_root, "verify", null);
    }  catch (Exception ex)  {
      conn.close();
      errWriter.write("Failed to verify against " + dbType + 
                      " db with exception '" + ex.getMessage() + "'");
      return "Ingest failed";
    }
    if (!ingest) {
      conn.close();
      return "Successfully verified against " + dbType;
    }
    try {
      vis.setReason(reason);
      vis.setOwner(owner);
      vis.visit(m_root, "write", null);
    }  catch (Exception ex) {
      conn.close();
      errWriter.write("Failed to write to " + dbType + 
                      " db with exception '" + ex.getMessage() + "'");
      return "Ingest failed";
    }

    //  Traveler is now in db.  Retrieve for purpose of archiving
    Traveler travelerFromDb=null;
    try {
      int intVersion = Integer.parseInt(vis.getTravelerVersion());
      ProcessNodeDb travelerDb = 
        new ProcessNodeDb(conn, vis.getTravelerName(), intVersion, 
                          vis.getTravelerHardwareGroup(), null, null);
      ProcessNode travelerRoot = new ProcessNode(null, travelerDb);
      String subsys = travelerDb.getSubsystem(conn);
      travelerFromDb = new Traveler(travelerRoot, "db", dbType, subsys);
    }  catch (NumberFormatException ex) {
      conn.close();
      errWriter.write("input version must be an integer!");
      return "Ingest failed";
    }  catch (Exception ex)  {
      System.out.println(ex.getMessage());
      conn.close();
      errWriter.write("Failed to read traveler " + vis.getTravelerName() + " with exception " + ex.getMessage());
      return "Ingest failed";
    } finally {
      conn.close();
      ProcessNodeDb.reset();
    }

    // Now have everything to call archiveYaml
    String archiveRet = travelerFromDb.archiveYaml(sessionData, errWriter);
    return "Ingest succeeded " + archiveRet;
  }

  static public Map<String, String>
    ingest(HttpServletRequest req, Map<String, String> parms) {
    return ingest(new SessionData(req), parms);
  }
  
  static public Map<String, String>
    ingest(SessionData sessionData, Map<String, String> parms) {

    String summary = "";
    String acknowledge = null;
    String contents=null;
    String reason= "";
    String operator= "null";
    String responsible= "";
    String validateOnly= "null";
    boolean imp = false;
    HashMap<String, String> retMap = new HashMap<String, String>();
    retMap.put("summary", "");  // overwrite if successful ingest
    retMap.put("acknowledge", acknowledge); // overwrite if fail

    // Check for reasonable arguments
    contents = unpackMap("contents", parms, retMap);
    if (contents == null) return retMap;
    operator = unpackMap("operator", parms, retMap);
    if (operator == null) return retMap;
    validateOnly = unpackMap("validateOnly", parms, retMap);
    if (validateOnly == null) return retMap;
    if (validateOnly.equals("0") || validateOnly.equals("False")) {
      reason = unpackMap("reason", parms, retMap);
      if (reason == null) return retMap;
      responsible = unpackMap("responsible", parms, retMap);
      if (responsible == null) return retMap;
      imp = true;
    }
    StringWriter wrt = new StringWriter(200);
    String dbType = sessionData.getDbType();
    String datasource = sessionData.getDatasource();

    Traveler trav = null;
    try {
      trav = new Traveler(contents, true, wrt, "\n");
      if (trav.getRoot() == null) {
        wrt.write("Could not parse yaml input\n");
        retMap.put("acknowledge", wrt.toString());
        return retMap;
      }
        
    }  catch (Exception ex)  {
      wrt.write(ex.getMessage() + "\n");
      retMap.put("acknowledge", wrt.toString());
      return retMap;
    }
    try {
      String retStatus = trav.writeToDb(operator, true, imp, reason, 
        responsible, sessionData, wrt);
      retMap.put("acknowledge", wrt.toString());
      retMap.put("summary", retStatus);
    } catch (IOException ex) {
        retMap.put("acknowledge", "Error " + wrt.toString() + 
          " followed by IOException " + ex.getMessage());
        retMap.put("summary", "Failure");
        return retMap;
    }
    return retMap;
  }
  static private String unpackMap(String key, Map<String, String> parms,
                                  Map<String, String> out) {
    if (parms.containsKey(key)) return parms.get(key);
    out.put("acknowledge", "Bad input: missing " + key);
    return null;
  }

  static private DbConnection makeConnection(SessionData sd)  {
    DbConnection conn = new MysqlDbConnection();
    conn.setSourceDb(sd.getDbType());
    boolean isOpen = conn.open(sd);
    if (isOpen) {
      try {
        conn.setReadOnly(false);
      } catch (Exception ex) {
        conn.close();
        System.out.println("Unable to set connection non-readonly");
        return null;
      }    
      return conn;
    }
    else {
      System.out.println("Failed to connect");
      return null;
    }
  }  
}
