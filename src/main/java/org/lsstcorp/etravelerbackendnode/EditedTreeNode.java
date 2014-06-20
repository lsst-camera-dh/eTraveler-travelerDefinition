/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

/**
 *
 * @author jrb
 */
public class EditedTreeNode {

  EditedTreeNode(String p, String e) {
    path = p;
    editType = e;
    if (e.equals("modified")) undo = "revert";
    if (e.equals("deleted")) undo = "restore";
    if (e.equals("added")) undo = "remove";
  }
  EditedTreeNode(String p, String e, String u) {
    path = p; editType = e; undo = u;
  }
  String path = "";
  String editType = "";
  String undo = "";
  ProcessNode source = null;
  
  public String getPath() {return path;}
  public String getEditType() {return editType;}
  public String getUndo() {return undo;}
}
