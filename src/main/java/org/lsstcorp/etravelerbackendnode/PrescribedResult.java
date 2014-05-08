/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

/**
 *  Internal representation of result template for a particular Process;
 * e.g., description of measured quantity operator must enter
 * @author jrb
 */
public class PrescribedResult  implements TravelerElement {
  public void accept(TravelerVisitor visitor, String activity) 
      throws EtravelerException {
    visitor.visit(this, activity);
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
  }
  public interface ExportTarget extends TravelerElement.ExportTarget {
    void acceptLabel(String label);
    void acceptSemantics(String semantics);
    void acceptUnits(String units);
    void acceptMinValue(String minValue);
    void acceptMaxValue(String maxValue);
    void acceptResultDescription(String description);
    void acceptChoiceField(String choiceField);
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
  }
  // Copy constructor
  public PrescribedResult(ProcessNode parent, PrescribedResult orig) {
    m_parent = parent;
    m_label = new String(orig.m_label);
    m_semantics = new String(orig.m_semantics);
    m_description = new String(orig.m_description);
    m_units = new String(orig.m_units);
    if (orig.m_minValue != null) m_minValue = new String(orig.m_minValue);
    if (orig.m_maxValue != null) m_maxValue = new String(orig.m_maxValue);
    if (orig.m_choiceField != null) m_choiceField = new String(orig.m_choiceField);
  }

  private String m_label;
  private String m_semantics;
  private String m_units="";
  private String m_minValue="";
  private String m_maxValue="";
  private String m_description="";
  private String m_choiceField="";
  private ProcessNode m_parent;
  
}
