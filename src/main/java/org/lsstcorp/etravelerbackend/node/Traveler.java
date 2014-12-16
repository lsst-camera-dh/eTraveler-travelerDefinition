/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

/**
 * Root of a process tree plus some associated information: where
 * it came from, what db (if any) it is supposed to be compatible with
 * @author jrb
 */
public class Traveler {
  private ProcessNode m_root=null;
  private String m_source=null;  // e.g. db, yaml
  private String m_sourceDb="[none]";  // dataSourceMode value or "[none]"
  

  public Traveler(ProcessNode root, String source, String sourceDb) {
    m_root = root;
    m_source = source;    // check for validity?
    m_sourceDb = sourceDb;
  }
  public Traveler(ProcessNode root, String source)  {
    m_root = root;
    m_source = source;
  }
  public ProcessNode getRoot() {return m_root;}
  public String getSource() {return m_source;}
  public String getSourceDb() {return m_sourceDb;}
  public String getName() {return m_root.getName();}
  public String getVersion() {return m_root.getVersion();}
}
