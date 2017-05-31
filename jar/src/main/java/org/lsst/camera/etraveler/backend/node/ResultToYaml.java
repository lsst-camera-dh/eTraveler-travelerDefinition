
package org.lsst.camera.etraveler.backend.node;

import java.util.Map;

/**
 *
 * @author jrb
 */
public class ResultToYaml implements PrescribedResult.ExportTarget {
  private Map<String, Object> m_data;
  private TravelerToYamlVisitor m_vis;
  
  public ResultToYaml(TravelerToYamlVisitor vis, Map<String, Object> data) {
    m_data = data;
    m_vis = vis;
  }
  
  public void acceptLabel(String label) {
    m_data.put("Label", label);
  }
  public void acceptSemantics(String semantics) {
    m_data.put("InputSemantics", semantics);
  }
  public void acceptUnits(String units) {
    if (units != null) m_data.put("Units", units);
  }
  public void acceptMinValue(String minValue) {
    if (minValue != null) m_data.put("MinValue", minValue);
  }
  public void acceptMaxValue(String maxValue) {
    if (maxValue != null) m_data.put("MaxValue", maxValue);
  }
  
  /*
   *  Assuming for now the container (RequiredInputs or OptionalInputs)
   *  is used to determine if an input is optional or not
   */
  public void acceptIsOptional(String isOptional) {}
  
  public void acceptResultDescription(String description) {
    if (description != null) m_data.put("Description", description);
  }
  public void acceptResultName(String name) {
    if (name != null) m_data.put("Name", name);
  }

  public void acceptSignatureRole(String role) {
    if (role != null) m_data.put("Role", role);
  }
  public void acceptChoiceField(String choiceField) {}
}
