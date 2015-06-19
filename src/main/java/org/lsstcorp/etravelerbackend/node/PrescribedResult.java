/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import javax.management.Attribute;
import javax.management.AttributeList;

/**
 *  Internal representation of result template for a particular Process;
 * e.g., description of measured quantity operator must enter
 * @author jrb
 */
public class PrescribedResult  implements TravelerElement {
  public void accept(TravelerVisitor visitor, String activity, Object cxt) 
      throws EtravelerException {
    visitor.visit(this, activity, cxt);
  }
  public void exportTo(TravelerElement.ExportTarget target) {
    if (target instanceof PrescribedResult.ExportTarget) {
      PrescribedResult.ExportTarget ptarget = (PrescribedResult.ExportTarget) target;
      //ptarget.acceptPrescribedResultParent(m_parent);
      ptarget.acceptLabel(m_label);
      ptarget.acceptSemantics(m_semantics);
      ptarget.acceptUnits(m_units);
      ptarget.acceptMinValue(m_minValue);
      ptarget.acceptMaxValue(m_maxValue);
      ptarget.acceptIsOptional(m_isOptional);
      ptarget.acceptResultDescription(m_description);
    }
  }
 
  public interface Importer {
    String provideLabel();
    String provideSemantics();
    String provideDescription();
    String provideUnits();
    String provideMinValue();
    String provideMaxValue();
    String provideChoiceField();
    String provideIsOptional();
  }
  public interface ExportTarget extends TravelerElement.ExportTarget {
    void acceptLabel(String label);
    void acceptSemantics(String semantics);
    void acceptUnits(String units);
    void acceptMinValue(String minValue);
    void acceptMaxValue(String maxValue);
    void acceptResultDescription(String description);
    void acceptChoiceField(String choiceField);
    void acceptIsOptional(String isOptional);
  }
  public PrescribedResult(ProcessNode parent, PrescribedResult.Importer imp) {
    m_parent = parent;
    m_label = imp.provideLabel();
    m_semantics = imp.provideSemantics();
    m_description = imp.provideDescription();
    m_units = imp.provideUnits();
    m_minValue = imp.provideMinValue();
    m_maxValue = imp.provideMaxValue();
    m_choiceField = imp.provideChoiceField();
    m_isOptional = imp.provideIsOptional();
  }
  // Copy constructor
  public PrescribedResult(ProcessNode parent, PrescribedResult orig) {
    m_parent = parent;
    m_label = new String(orig.m_label);
    m_semantics = new String(orig.m_semantics);
    m_description = new String(orig.m_description);
    m_units = new String(orig.m_units);
    m_isOptional = new String(orig.m_isOptional);
    if (orig.m_minValue != null) m_minValue = new String(orig.m_minValue);
    if (orig.m_maxValue != null) m_maxValue = new String(orig.m_maxValue);
    if (orig.m_choiceField != null) m_choiceField = new String(orig.m_choiceField);
  }

  public AttributeList getAttributes() {
    AttributeList atts = new AttributeList(6);
    atts.add(new Attribute("label", m_label));
    atts.add(new Attribute("type", m_semantics));
    atts.add(new Attribute("description", m_description));
    atts.add(new Attribute("isOptional", m_isOptional));
    if (!m_units.equals("")) {
      atts.add(new Attribute("units", m_units));
    }
    if (!m_minValue.equals("")) {
      atts.add(new Attribute("min value", m_minValue));
    }
    if (!m_maxValue.equals("")) {
      atts.add(new Attribute("max value", m_maxValue));
    }
    return atts;
  }
    
  public String getLabel() {return m_label;}
  public String getSemantics() {return m_semantics;}
  public String getDescription() {return m_description;}
  public String getUnits() {return m_units;}
  public String getMinValue() {return m_minValue;}
  public String getMaxValue() {return m_maxValue;}
  public String getIsOptional() {return m_isOptional;}
  public void setIsOptional(String isOpt) {m_isOptional = isOpt;}
  public boolean setDescription(String desc) 
  {
    String arg = (desc == null) ? "" : desc;
    if (!arg.equals(m_description)) {
      m_description = arg;
      return true;
    }
    return false;
  }
  public boolean setUnits(String u) {
    String arg = (u == null) ? "" : u;
    if (!arg.equals(m_units)) {
      m_units = arg;
      return true;
    }
    return false;
  }
  public boolean setMinValue(String minV) {
    String arg = (minV == null) ? "" : minV;
    if (!arg.equals(m_minValue)) {
      if (numericEquals(m_minValue, arg)) return false;
      m_minValue = arg;
      return true;
    }
    return false;
  }
  public boolean setMaxValue(String maxV) {
    String arg = (maxV == null) ? "" : maxV;
    if (!arg.equals(m_maxValue)) {
      if (numericEquals(m_maxValue, arg)) return false;
      m_maxValue = arg;
      return true;
    }
    return false;
  }
  public boolean numberSemantics() {
    return (m_semantics.equals("int") || m_semantics.equals("float") );
  }
  private boolean numericEquals(String s1, String s2) {
    if ((s1.isEmpty()) || (s2.isEmpty())) {
      if ((s1.isEmpty()) && (s2.isEmpty())) return true;
      return false;
    }
    if (m_semantics.equals("int")) {
      return (Integer.parseInt(s1) == Integer.parseInt(s2));
    } else if (m_semantics.equals("float")) {
      return (Float.parseFloat(s1) == Float.parseFloat(s2));
    } else return s1.equals(s2);
  }
  private String m_label;
  private String m_semantics;
  private String m_units="";
  private String m_minValue="";
  private String m_maxValue="";
  private String m_description="";
  private String m_choiceField="";
  private String m_isOptional="0";
  private ProcessNode m_parent;
  
}
