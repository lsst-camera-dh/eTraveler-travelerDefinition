/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.exceptions;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

/**
 *
 * @author jrb
 */
public class UnrecognizedYamlKey  extends EtravelerException {
  public String m_key;
  public String m_nodeType;
  public UnrecognizedYamlKey(String key, String nodeType) {
    super("Unrecognized key " + key + " for node type " + nodeType);
    m_key = key;
    m_nodeType = nodeType;
  }
  
}
