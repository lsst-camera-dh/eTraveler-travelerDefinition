/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import javax.management.Attribute;
import javax.management.AttributeList;

/**
 * Internal representation of prerequisite attached to a particular ProcessNode
 * @author jrb
 */
public class Prerequisite implements TravelerElement {
  /**
   * Interface for importing Prerequisites from another representation
   */
  public interface Importer {
    String provideName();
    String provideType();
    String provideDescription();
    int provideQuantity();
    String provideVersion();
    String provideUserVersionString();
  }
  public interface ExportTarget extends TravelerElement.ExportTarget {
    void acceptPrerequisiteType(String prerequisiteType);
    void acceptPrereqProcessVersion(String version);
    void acceptPrereqProcessUserVersionString(String userVersionString);
    void acceptPrereqName(String name);
    void acceptPrereqId(String prereqId);
    void acceptPrereqParent(ProcessNode process);
    void acceptPrereqQuantity(int quantity);
    void acceptPrereqDescription(String description);
  }
  public Prerequisite(ProcessNode parent, Prerequisite.Importer imp) {
    m_parent = parent;
    m_prerequisiteType = imp.provideType();
    m_name = imp.provideName();
    m_description = imp.provideDescription();
    m_quantity = imp.provideQuantity();
    m_version = imp.provideVersion();
    m_userVersionString = imp.provideUserVersionString();
  }
  // Copy constructor
  public Prerequisite(ProcessNode parent, Prerequisite orig) {
    m_parent = parent;
    m_prerequisiteType = new String(orig.m_prerequisiteType);
    m_name = new String(orig.m_name);
    m_description = new String(orig.m_description);
    m_quantity = orig.m_quantity;
    if (orig.m_version != null) m_version = new String(orig.m_version);
    if (orig.m_userVersionString != null) 
      m_userVersionString = new String(orig.m_userVersionString);
  }
  public void accept(TravelerVisitor visitor, String activity) throws EtravelerException {
    visitor.visit(this, activity);
  }
  public void exportTo(TravelerElement.ExportTarget target) {
    if (target instanceof Prerequisite.ExportTarget) {
      Prerequisite.ExportTarget ptarget = (Prerequisite.ExportTarget) target;
      ptarget.acceptPrereqParent(m_parent);
      ptarget.acceptPrerequisiteType(m_prerequisiteType);
      ptarget.acceptPrereqName(m_name);
      // ptarget.acceptPrereqId(m_prereqId);
      if (m_prerequisiteType.equals("PROCESS_STEP")) {
        ptarget.acceptPrereqProcessVersion(m_version);
        ptarget.acceptPrereqProcessUserVersionString(m_userVersionString);
      }
    }
  }
  
  public AttributeList getAttributes() {
    AttributeList atts = new AttributeList(6);
    atts.add(new Attribute("name", m_name));
    atts.add(new Attribute("Type", m_prerequisiteType));
    atts.add(new Attribute("description", m_description));
    atts.add(new Attribute("quantity", m_quantity));
    if (!m_version.equals("")) {
      atts.add(new Attribute("version", m_version));
    }
    if (!m_userVersionString.equals("")) {
      atts.add(new Attribute("User version", m_userVersionString));
    }
    return atts;
  }
  public String getName() {return m_name;}
  public String getType() {return m_prerequisiteType;}
  public String getDescription() {return m_description;}
  public String getQuantity() {return Integer.toString(m_quantity);}
  public String getVersion() {
    if (m_version == null) return "";
    return m_version;
  }
  public String getUserVersionString() {
    if (m_userVersionString == null) return "";
    return m_userVersionString;
  }
  private String m_prerequisiteType = null;
  // All prereqs have a name. For component prereqs it's the
  // HardwareType.name.  PROCESS_STEP it's Process.name
  private String m_name = null;
  private ProcessNode m_parent = null;
  private String m_description = "";
  private int    m_quantity=1;
  // Following is for PROCESS_STEP type only
  private String m_version = "";
  private String m_userVersionString = "";
  
  // Used for both component and process step types
  // private String m_prereqId;      
}
