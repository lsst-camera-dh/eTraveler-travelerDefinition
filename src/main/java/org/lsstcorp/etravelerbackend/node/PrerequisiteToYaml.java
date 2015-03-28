/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

import java.util.Map;

/**
 *
 * @author jrb
 */
public class PrerequisiteToYaml implements Prerequisite.ExportTarget {
  private Map<String, Object> m_data;
  private TravelerToYamlVisitor m_vis;
  
  public PrerequisiteToYaml(TravelerToYamlVisitor vis, Map<String, Object> data) {
    m_data = data;
    m_vis = vis;
  }
  
  public void acceptPrerequisiteType(String prerequisiteType) {
    m_data.put("PrerequisiteType", prerequisiteType);
  }
  public void acceptPrereqProcessVersion(String version) {
    // Don't do anything.  We want the most recent with correct userVersionString
  }
  public void acceptPrereqProcessUserVersionString(String userVersionString) {
    m_data.put("UserVersionString", userVersionString);
  }
  public void acceptPrereqName(String name) {
    m_data.put("Name", name);    
  }
  public void acceptPrereqId(String prereqId) {
    if (m_vis.getIncludeDbInternal()) {
      m_data.put("FromSourcePrereqId", prereqId);
    }
  }
  
  public void acceptPrereqParent(ProcessNode process) {
    // Don't need to do anything about this
  }
  
  public void acceptPrereqQuantity(int quantity) {
    m_data.put("Quantity", Integer.toString(quantity));    
  }
  
  public void acceptPrereqDescription(String description) {
    if (description != null) m_data.put("Description", description);
  }
  
}
