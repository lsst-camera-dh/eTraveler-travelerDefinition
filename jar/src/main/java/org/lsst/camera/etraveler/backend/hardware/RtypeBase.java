package org.lsst.camera.etraveler.backend.hardware;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.util.ArrayList;
/**
 *
 * @author jrb
 *
 * Base class for all representations of a hardware relationship type
 */

public class RtypeBase
{
  protected String m_rtypeName=null;
  protected String m_description=null;
  protected HtypeBase m_majorType=null;
  protected HtypeBase m_minorType=null;
  protected boolean m_singleBatch=true;
  protected int     m_nMinor=1;
  protected ArrayList<String> m_slotnames;
  
  public RtypeBase() {}

  public void acceptRtypeName(String name) {m_rtypeName = name;}
  public void acceptDescription(String desc) {m_description = desc;}
  public void acceptMajorType(HtypeBase maj) {m_majorType = maj;}
  public void acceptMinorType(HtypeBase min)  {m_minorType = min;}

  public void acceptSingleBatch(boolean singleBatch) {
    m_singleBatch=singleBatch;
  }
  public void acceptNminor(int n) {m_nMinor = n;}
  public void acceptSlotNames(ArrayList<String> slotnames) {
    m_slotnames = slotnames;
  }
  public String provideRtypeName() {return m_rtypeName;}
  public String provideDescription() {return m_description;}
  public HtypeBase provideMajorType() {return m_majorType;}
  public HtypeBase provideMinorType() {return m_minorType;}
  public boolean provideSingleBatch() {return m_singleBatch;}
  public int     provideNminor() {return m_nMinor;}
  public ArrayList<String> provideSlotNames() {return m_slotnames;}
}
