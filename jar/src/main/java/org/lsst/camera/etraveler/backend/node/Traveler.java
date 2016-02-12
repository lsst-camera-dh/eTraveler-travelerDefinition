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

  public int archiveYaml(String archiveDir, String dbType, Writer writer) {
    // Return 2 if it's inappropriate to write the files
    if (archiveDir == null) return 2;
    if (archiveDir.isEmpty()) return 2;
    if ((!dbType.equals("Prod")) && (!dbType.equals("Raw")) ) return 2;

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

}
