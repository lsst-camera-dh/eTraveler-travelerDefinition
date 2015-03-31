/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

import java.util.ArrayList;


/**
 *
 * @author jrb
 */
public class TravelerActionBits {
  public static final int HARNESSED=1;
  public static final int MAKE_HARDWARE_RELATIONSHIP=2;
  public static final int BREAK_HARDWARE_RELATIONSHIP=4;
  public static final int SET_HARDWARE_STATUS=8;
  public static final int SET_HARDWARE_LOCATION=16;
  public static final int ASYNC=32;
  public static final int AUTOMATABLE=64;
  
  public static String getYamlKey(int actionBit) {
    switch (actionBit) {
      case HARNESSED:
        return "HarnessedJob";
      case MAKE_HARDWARE_RELATIONSHIP:
        return "MakeHardwareRelationship";
      case BREAK_HARDWARE_RELATIONSHIP:
        return "BreakHardwareRelationship";
      case SET_HARDWARE_STATUS:
        return "SetHardwareStatus";
      case SET_HARDWARE_LOCATION:
        return "SetHardwareStatus";
      case ASYNC:
        return "Async";
      case AUTOMATABLE:
        return "Automatable";
      default:
        throw new IllegalArgumentException("Unknown traveler action bit");
    }
  }
}
