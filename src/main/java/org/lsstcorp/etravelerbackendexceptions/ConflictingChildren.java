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
public class ConflictingChildren  extends EtravelerException {
  public String m_childType1;
  public String m_childType2;
  public ConflictingChildren(String childType1, String childType2) {
    super("Process may not have both " + childType1 + " and " + childType2 + " children");
    m_childType1 = childType1;
    m_childType2 = childType2;
  }
  
}
