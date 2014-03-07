/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

/**
 *
 * @author jrb
 */
public class UnrecognizedYamlKey  extends EtravelerException {
  public String m_key;
  public String m_nodeType;
  UnrecognizedYamlKey(String key, String nodeType) {
    super(nodeType + " node has no key " + key);
    m_key = key;
    m_nodeType = nodeType;
  }
  
}
