/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.exceptions;

/**
 *
 * @author jrb
 */
public class DbContentException extends IllegalStateException {
  public DbContentException(String msg)  {
    super("Bad database content: "  + msg);
  }
}
