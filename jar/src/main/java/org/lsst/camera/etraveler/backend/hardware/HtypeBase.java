package org.lsst.camera.etraveler.backend.hardware;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

/**
 *
 * @author jrb
 *
 * Base class for all representations of a hardware type.  Includes
 * get/set methods corresponding to columns in HardwareType table.
 */

public class HtypeBase
{
  protected String m_htypeName=null;
  protected String m_description=null;
  protected String m_subsystem="Default";
  protected int    m_autoSequenceWidth=0;
  protected boolean m_isBatched=false;
  
  public HtypeBase() {}

  public void acceptHtypeName(String name) {m_htypeName = name;}
  public void acceptDescription(String desc) {m_description = desc;}
  public void acceptSubsystem(String sub) {m_subsystem = sub;}
  public void acceptAutoSequenceWidth(int width) {m_autoSequenceWidth=width;}
  public void acceptIsBatched(boolean isBatched) {m_isBatched=isBatched;}

  public String provideHtypeName() {return m_htypeName;}
  public String provideDescription() {return m_description;}
  public String provideSubsystem() {return m_subsystem;}
  public int provideAutoSequenceWidth() {return m_autoSequenceWidth;}
  public boolean provideIsBatched() {return m_isBatched;}
}
