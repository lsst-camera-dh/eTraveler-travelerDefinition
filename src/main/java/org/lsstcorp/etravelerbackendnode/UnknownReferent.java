/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

/**
 *
 * @author jrb
 */
public class UnknownReferent extends EtravelerException {
  public String m_name;
  public String m_version;
  UnknownReferent(String name, String version) {
    super("No prior process definition of name " + name + ", version " + version);
    m_name = name;
    m_version = version;
  }
}
