package org.lsst.camera.etraveler.backend.hardware;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

import java.util.ArrayList;
/**
 *
 * @author jrb
 *
 * Base class for all representations of a hardware assembly type.
 * Properties associated with the hardware (not assembly) come
 * from the underlying HtypeBase object, accessible via getHardwareType
 * This interface otherwise only concerns relations for which the
 * hardware type is the parent component type
 */
public interface AtypeInterface
{
  public abstract void addRelationType(RAtypeInterface r);
  public abstract ArrayList<RAtypeInterface> getRelationTypes();
  public abstract RAtypeInterface getRelationType(int i);
  public abstract HtypeBase getHardwareType();
}
