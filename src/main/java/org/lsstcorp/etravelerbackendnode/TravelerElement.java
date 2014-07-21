/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import org.lsstcorp.etravelerbackendexceptions.EtravelerException;

/**
 *
 * @author jrb
 */
public interface TravelerElement {
  public interface ExportTarget { }
  void accept(TravelerVisitor visitor, String activity) throws EtravelerException;
  void exportTo(TravelerElement.ExportTarget target);
}
