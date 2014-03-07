/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

/**
 *
 * @author jrb
 */
public class NullYamlValue extends EtravelerException {
  public String m_key;
  public String m_nodeType;
  NullYamlValue(String key, String nodeType) {
    super("Missing value for key " + key + ", nodeType " + nodeType);
    m_key = key;
    m_nodeType = nodeType;
  }
}
