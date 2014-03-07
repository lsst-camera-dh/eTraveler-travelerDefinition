/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

/**
 *
 * @author jrb
 */
public class DbContentException extends IllegalStateException {
  DbContentException(String msg)  {
    super("Bad database content: "  + msg);
  }
}
