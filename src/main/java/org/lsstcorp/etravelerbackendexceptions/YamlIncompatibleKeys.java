/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendexceptions;

import org.lsstcorp.etravelerbackendexceptions.EtravelerException;

/**
 *
 * @author jrb
 */
public class YamlIncompatibleKeys extends EtravelerException {
  public String m_key1;
  public String m_key2;
  public YamlIncompatibleKeys(String key1, String key2) {
    super("Incompatible keys " + key1 + " and " + key2 + " in same node");
    m_key1 = key1;
    m_key2 = key2;
  }
}
