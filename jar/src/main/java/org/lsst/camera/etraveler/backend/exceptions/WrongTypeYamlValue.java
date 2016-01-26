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
public class WrongTypeYamlValue extends EtravelerException {
  public String m_key;
  public String m_value;
  public String m_nodeType;
  
  public WrongTypeYamlValue(String key, String value, String nodeType)  {
    super("Illegal value " + value + " for key " + key + " for node of type " + nodeType);
    m_key = key;
    m_value = value;
    m_nodeType = nodeType;
  }
}
