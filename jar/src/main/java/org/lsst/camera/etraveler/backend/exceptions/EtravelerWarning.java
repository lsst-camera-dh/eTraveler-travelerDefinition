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
public class EtravelerWarning extends EtravelerException {
  public EtravelerWarning(String msg)  {
      super("WARNING: " + msg);
  }
}
