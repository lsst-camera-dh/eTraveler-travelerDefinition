/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

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
  
  public Traveler(ProcessNode root, String source, String sourceDb, 
      String subsystem) {
    m_root=root;
    m_source = source;
    m_sourceDb = sourceDb;
    m_subsystem=subsystem;
  }
  public Traveler(ProcessNode root, String source, String sourceDb) {
    m_root = root;
    m_source = source;    // check for validity?
    m_sourceDb = sourceDb;
  }
  public Traveler(ProcessNode root, String source)  {
    m_root = root;
    m_source = source;
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
}
