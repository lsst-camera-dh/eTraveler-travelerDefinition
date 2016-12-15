package org.lsst.camera.etraveler.backend.hardware;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

import java.util.ArrayList;
/**
 *
 * @author jrb
 *
 * Base class for all representations of a hardware assembly type
 */
public interface AtypeInterface
{
  public abstract void addRelationType(RAtypeInterface r);
  public abstract ArrayList<RAtypeInterface> getRelationTypes();
  public abstract RAtypeInterface getRelationType(int i);
  public abstract HtypeBase getHardwareType();
}
