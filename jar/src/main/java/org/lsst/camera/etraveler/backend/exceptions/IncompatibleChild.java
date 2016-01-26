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
public class IncompatibleChild extends EtravelerException {
  public String m_parentName;
  public String m_childName;
  public String m_reason;
  public IncompatibleChild(String parent, String child, String reason) {
    super("Child "+child+" incompatible with parent "+parent+" due to "+reason);
    m_parentName = parent;
    m_childName = child;
    m_reason = reason;
  }
}
