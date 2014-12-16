/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

/**
 * Abstract interface for all complex eTraveler
 * components: regular process nodes (ProcessNode), 
 * cloned nodes, prerequisites, and manual inputs. 
 * @author jrb
 */
import org.yaml.snakeyaml.nodes.Node;
public interface BaseNode {
    int readSerialized(Node yNode);
    int writeDb();
}
