/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.hnode;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

/**
 * Abstract out navigation of an assembly template
 * @author jrb
 */
public interface AssemblyVisitor {
  void visit(HardwareTypeNode hnode, String activity, Object context) throws EtravelerException;
}
