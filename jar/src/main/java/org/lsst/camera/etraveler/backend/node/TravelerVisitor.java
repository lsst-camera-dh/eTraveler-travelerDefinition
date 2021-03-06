/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

/**
 * Abstract out navigation of a traveler
 * @author jrb
 */
public interface TravelerVisitor {
  void visit(ProcessNode process, String activity, Object context) throws EtravelerException;
  void visit(PrescribedResult result, String activity, Object context) throws EtravelerException;
  void visit(Prerequisite prerequisite, String activity, Object context) throws EtravelerException;
  void visit(RelationshipTask rel, String activity, Object context) throws EtravelerException;
}
