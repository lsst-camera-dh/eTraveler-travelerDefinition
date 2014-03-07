/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import org.yaml.snakeyaml.nodes.Node;

/**
 *
 * @author jrb
 */
public class AuxNode implements BaseNode {
  public int readSerialized(Node yNode)  {
    return 0;
  }
  public int writeDb() {
    return 0;
  }
  ProcessNode m_parent;
}
