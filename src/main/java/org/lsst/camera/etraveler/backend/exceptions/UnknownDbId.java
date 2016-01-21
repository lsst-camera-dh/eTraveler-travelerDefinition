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
public class UnknownDbId extends EtravelerException {
  public String m_id;
  public String m_table;
  public UnknownDbId(String id, String table) {
    super("No such id  " + id + " in table " + table);
    m_id = id;
    m_table = table;
  }
}
