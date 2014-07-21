/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import org.lsstcorp.etravelerbackendexceptions.EtravelerException;

/**
 * Abstract out navigation of a traveler
 * @author jrb
 */
public interface TravelerVisitor {
  void visit(ProcessNode process, String activity) throws EtravelerException;
  void visit(PrescribedResult result, String activity) throws EtravelerException;
  void visit(Prerequisite prerequisite, String activity) throws EtravelerException;
}
