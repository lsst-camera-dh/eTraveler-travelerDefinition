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
import java.util.Map;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import org.lsst.camera.etraveler.backend.db.MysqlDbConnection;


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

  public int archiveYaml(HttpServletRequest req, Writer writer) {
    // Return 2 if it's inappropriate to write the files

    String dbType = ModeSwitcherFilter.getVariable(req.getSession(), 
                                                   "dataSourceMode");
    // only archive if db is Prod or Raw
    if ((!dbType.equals("Prod")) && (!dbType.equals("Raw")) ) return 2;
    String archiveDir = ModeSwitcherFilter.getVariable(req.getSession(), 
                                                       "etravelerFileStore");
    String url = (req.getRequestURL()).toString();
    if (url.contains("localhost")) {
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

    String results = "<p>Files written to " + fname + "_verbose.yaml, " + fname+ "_canonical.yaml</p>";
    boolean okStatus = true;
    try {
      File dir = new File(dirname);
      if (!dir.isDirectory())  {
        dir.mkdirs();
      }
      fileOutVerbose = new FileWriter(fname + "_verbose.yaml");
      fileOutCanon = new FileWriter(fname + "_canonical.yaml");
    } catch (Exception ex)  {
      results = "unable to open output file" + fname + " or " + fname + "_canonical";
      System.out.println(results);
      okStatus = false;
    }
    if (okStatus) {
      outputYaml(fileOutVerbose, true);
      outputYaml(fileOutCanon, false);
    }
    try {
      writer.write(results);
   
    } catch (Exception ex) {
      System.out.println("exception " + ex.getMessage() 
          + " attempting to write " + results);
      return 0;
    }
    return 1;
  }

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
      return ("outputYaml unable to close file with exception " + ioEx.getMessage());
    }
    return msg;
  }

  //
  public String writeToDb(String user, boolean useTransactions, 
                          boolean ingest, 
                          HttpServletRequest req, Writer writer)  {

    String dbType = ModeSwitcherFilter.getVariable(req.getSession(),
                                                   "dataSourceMode");
    String datasource = ModeSwitcherFilter.getVariable(req.getSession(),
                                                       "etravelerDb");
    
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
      vis.visit(m_root, "new", null);
    }  catch (Exception ex)  {
      conn.close();
      return "Failed to create xxDb classes with exception '" + 
          ex.getMessage() +"'";
    }
    try {
      vis.setSubsystem(m_subsystem);
      vis.visit(m_root, "verify", null);
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
      vis.setReason(req.getParameter("reason").trim());
      vis.setOwner(req.getParameter("owner").trim());
      vis.visit(m_root, "write", null);
    }  catch (Exception ex) {
      conn.close();
      return "Failed to write to " + dbType + 
          " db with exception '" + ex.getMessage() + "'";
    }
    //    conn.close();
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
      return "input version must be an integer!";
    }  catch (Exception ex)  {
      System.out.println(ex.getMessage());
      conn.close();
      return "Failed to read traveler " + vis.getTravelerName() + " with exception " 
                                   + ex.getMessage();
    } finally {
      conn.close();
      ProcessNodeDb.reset();
    }

    // Now have everything to call archiveYaml
    travelerFromDb.archiveYaml(req, writer);
    return "successfully wrote traveler to " + dbType + " db";
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

  //
}
