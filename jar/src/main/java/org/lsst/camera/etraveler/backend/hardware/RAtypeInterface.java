package org.lsst.camera.etraveler.backend.hardware;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.util.ArrayList;
/**
 *
 * @author jrb
 *
 * Interface to be implemented for all representations of a relationship type
 * as embedded in an assembly.
 */

public interface RAtypeInterface 
{
  public abstract void addSubassembly(AtypeInterface a);
  public abstract ArrayList<AtypeInterface> getSubassemblies();
  public abstract AtypeInterface getSubassembly(int i);
  public abstract RtypeBase getRelationType();
}
