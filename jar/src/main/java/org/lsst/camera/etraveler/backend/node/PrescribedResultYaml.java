/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;

import org.lsst.camera.etraveler.backend.exceptions.UnrecognizedYamlKey;
import org.lsst.camera.etraveler.backend.exceptions.NullYamlValue;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.io.Writer;
import java.io.IOException;

/**
 *
 * @author jrb
 */
public class PrescribedResultYaml implements PrescribedResult.Importer {
  private static ArrayList<String> s_knownKeys = null;
  static final int LABEL=0;
  static final int SEMANTICS=1;
  static final int UNITS=2;
  static final int MINVALUE=3;
  static final int MAXVALUE=4;
  static final int DESCRIPTION=5;
  static final int ROLE=6;
    
  public void readYaml(Map<String, Object> yamlMap, ProcessNodeYaml parent, int iPre) 
  throws EtravelerException {
    if (s_knownKeys == null)  {
      s_knownKeys = new ArrayList<String>(DESCRIPTION + 1);
      s_knownKeys.add(LABEL, "Label");
      s_knownKeys.add(SEMANTICS, "InputSemantics");
      s_knownKeys.add(UNITS, "Units");
      s_knownKeys.add(MINVALUE, "MinValue");
      s_knownKeys.add(MAXVALUE, "MaxValue");
      s_knownKeys.add(DESCRIPTION, "Description");
      s_knownKeys.add(ROLE, "Role");
    }
    m_parent = parent;
    
    Iterator<String> it = yamlMap.keySet().iterator();
   
    while (it.hasNext()) {
      String foundKey = it.next();
      int keyIx = s_knownKeys.indexOf(foundKey);
      if (keyIx == -1) {
        throw new UnrecognizedYamlKey(foundKey, "RequiredInputs");
      }
      
    
      if (yamlMap.get(foundKey) == null)  {
        throw new NullYamlValue(foundKey, "RequiredInputs", "");
      }
      String v = yamlMap.get(foundKey).toString();
      switch (keyIx)  {
      case LABEL:
        m_label = v; break;
      case SEMANTICS:
        m_semantics = v; 
        if (m_semantics.equals("signature") & (m_isOptional.equals("1"))) {
          throw new EtravelerException("Signatures not allowed in OptionalInputs");
        }
        break;
      case DESCRIPTION:
        m_description = v; break;
      case UNITS:
        m_units = v; break;
      case MINVALUE:
        m_minValue = v; 
        // If semantics indicates int or float, could check that value
        // is of correct type.  Similarly for MAXVALUE case.
        break;
      case MAXVALUE:
        m_maxValue = v; break; 
      case ROLE:
        if (m_isOptional.equals("1")) {
          throw new EtravelerException("Optional inputs may not include Role: keyword ");
        }
        m_role = v;
        if (v.isEmpty()) v = "(?)";
        break;
      }
    } 
    if (!m_semantics.equals("signature")) {
      if (!m_role.isEmpty()) {
        try {
          parent.getWriter().write("WARNING: Role keyword does not apply to semantic type ");
          parent.getWriter().write(m_semantics + ". Ignored." + parent.getEol());
          m_role ="";
        } catch (IOException ex) {
          
        }
      }
    }
  }
  public String provideLabel() {return m_label;}
  public String provideSemantics() {return m_semantics;}
  public String provideUnits() {return m_units;}
  public String provideMinValue() {return m_minValue;}
  public String provideMaxValue() {return m_maxValue;}
  public String provideDescription() {return m_description;}
  public String provideChoiceField() {return m_choiceField;}
  public String provideIsOptional() {return m_isOptional;}
  public String provideRole() {return m_role;}
  public void setIsOptional(String isOptional) {m_isOptional = isOptional;}
  private String m_label=null;
  private String m_semantics=null;
  private String m_units="";
  private String m_minValue="";
  private String m_maxValue="";
  private String m_description="";
  private String m_choiceField="";
  private String m_isOptional="0";
  private String m_role="";
  
  private ProcessNodeYaml m_parent=null;
}