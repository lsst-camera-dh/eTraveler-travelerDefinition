/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

/**
 *
 * @author jrb
 */
public interface TravelerElement {
  public interface ExportTarget { }
  void accept(TravelerVisitor visitor, String activity, Object cxt) throws EtravelerException;
  void exportTo(TravelerElement.ExportTarget target);
}
