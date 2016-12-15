/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.hardware;

import java.util.ArrayList;
/**
 *
 * @author jrb
 */
public class AtypeSpreadsheet extends HtypeBase implements AtypeInterface {
  private ArrayList<RAtypeInterface> m_subrelations = null;
  public AtypeSpreadsheet() {
    super();
  }
  public void addRelationType(RAtypeInterface r) {
      m_subrelations.add(r);
  }
  public ArrayList<RAtypeInterface> getRelationTypes() {
    return m_subrelations;
  } 
  public RAtypeInterface getRelationType(int i) {
    if ((i < 0) || (i >= m_subrelations.size())) {
      return null;
    }
    return m_subrelations.get(i);
  }
  public HtypeBase getHardwareType() {
    return (HtypeBase) this;
  }
}
