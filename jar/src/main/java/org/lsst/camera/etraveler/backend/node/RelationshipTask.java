package org.lsst.camera.etraveler.backend.node;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import javax.management.Attribute;
import javax.management.AttributeList;

/**
 * Internal representation of hardware relationship action attached to a 
 * particular ProcessNode
 * @author jrb
 */
public class RelationshipTask implements TravelerElement {
  /**
   * Interface for importing RelationshipTask from another representation
   */
  public interface Importer {
    String provideRelationshipName();
    String provideRelationshipAction();
  }
  public interface ExportTarget extends TravelerElement.ExportTarget {
    void acceptRelationshipName(String name);
    void acceptRelationshipAction(String action);
    void acceptRelationshipParent(ProcessNode process);
    void acceptRelationshipTaskId(String id);
  }

  public RelationshipTask(ProcessNode parent, 
                            RelationshipTask.Importer imp) {
    m_parent = parent;
    m_action = imp.provideRelationshipAction();
    m_name = imp.provideRelationshipName();
  }
  // Copy constructor
  public RelationshipTask(ProcessNode parent, RelationshipTask orig) {
    m_parent = parent;
    m_action = new String(orig.m_action);
    m_name = new String(orig.m_name);
  }
  public void accept(TravelerVisitor visitor, String activity, Object cxt) 
      throws EtravelerException {
    visitor.visit(this, activity, cxt);
  }
  public void exportTo(TravelerElement.ExportTarget target) {
    if (target instanceof RelationshipTask.ExportTarget) {
      RelationshipTask.ExportTarget rtarget = 
        (RelationshipTask.ExportTarget) target;
      rtarget.acceptRelationshipParent(m_parent);
      rtarget.acceptRelationshipName(m_name);
      rtarget.acceptRelationshipAction(m_action);
    }
  }
  public AttributeList getAttributes() {
    AttributeList atts = new AttributeList(2);
    atts.add(new Attribute("name", m_name));
    atts.add(new Attribute("action", m_action));
    return atts;
  }
  public String getName() {return m_name;}
  public String getAction() {return m_action;}

  private ProcessNode m_parent = null;
  private String m_name = null;
  private String m_action = null;
}

