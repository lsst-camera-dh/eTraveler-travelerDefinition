/*
 * Utilities to check for valid input
 */
package org.lsst.camera.etraveler.backend.util;

/**
 *
 * @author jrb
 */
public class Verify {
  /**
   * Check if input corresponds to positive integer.  
   * @param value
   * @return   "" if input is ok; else error string
   */
  public static String isPosInt(String val) {
    try {
      int anInt = Integer.parseInt(val);
      if (anInt < 1) { 
        return "Supplied value " + val + " is not > 0";
      }
    } catch (NumberFormatException e) {
      return "Supplied value " + val + " is not an integer";
    }  
    return "";
  }
  
  public static String isInt(String val) {
    try {
      int anInt = Integer.parseInt(val);
    } catch (NumberFormatException e) {
      return "Supplied value " + val + " is not an integer";
    }  
    return "";
  }
  public static String isFloat(String val) {
    try {
      float aFloat = Float.parseFloat(val);
    } catch (Exception ex) {
      return "Supplied value " + val + " is not a number";
    }
    return "";
  }
  
}
