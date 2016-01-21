/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;

import java.util.Map;

/**
 *
 * @author jrb
 */
public class RelationshipTaskToYaml implements RelationshipTask.ExportTarget {
  private Map<String, Object> m_data;
  private TravelerToYamlVisitor m_vis;
  
  public RelationshipTaskToYaml(TravelerToYamlVisitor vis, 
                                Map<String, Object> data) {
    m_data = data;
    m_vis = vis;
  }
  
  public void acceptRelationshipName(String name) {
    m_data.put("RelationshipName", name);
  }
  public void acceptRelationshipAction(String action) {
    m_data.put("RelationshipAction", action);
  }

  public void acceptRelationshipTaskId(String id) {
    if (m_vis.getIncludeDbInternal()) {
      m_data.put("FromSourceRelationshipTaskId", id);
    }
  }
  
  public void acceptRelationshipParent(ProcessNode process) {
    // Don't need to do anything about this
  }
    
}
