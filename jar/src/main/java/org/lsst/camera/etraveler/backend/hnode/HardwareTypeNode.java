package org.lsst.camera.etraveler.backend.hnode;

import java.util.ArrayList;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

/**
 * Represent one node in a diagram of hardware types which collectively
 * form an assembly description
 * @author jrb
 */
public class HardwareTypeNode
{
  public HardwareTypeNode(HardwareTypeNode parent,
                          HardwareTypeNode.Importer imp) throws Exception {
    m_parent = parent;
    m_name = imp.provideName();
    m_slotname = imp.provideSlotname();
    m_isBatched = imp.provideIsBatched();
    m_quantity = imp.provideQuantity();
    m_comments = imp.provideComments();
    int nChildren = imp.provideNChildren();
    imp.finishFetchAttributes();
    if (nChildren > 0) {
      m_children = new ArrayList<HardwareTypeNode>(nChildren);
      for (int ic = 0; ic < nChildren; ic++) {
        m_children.add(imp.provideNextChild(this));
      }
    }
  }

  public String getName() {return m_name;}
  /**
   * Interface for importing hardware nodes from another representation,
   * such as spreadsheet
   */
  public interface Importer {
    String provideName();
    String provideSlotname();
    boolean provideIsBatched() throws EtravelerException;
    int provideQuantity() throws EtravelerException;
    String provideComments();
    // int  provideLevel();

    int provideNChildren() throws EtravelerException;
    // Should be called before provideChild, after everything else
    void finishFetchAttributes();
    HardwareTypeNode provideNextChild(HardwareTypeNode parent)
      throws Exception;

    /* 
       chance for source to do anything else it needs to do.
       Return true if this was the last row; false otherwise
     */
    boolean finishImport();
  }
  public void exportToWrapper(HardwareTypeNode.Wrapper target) {
    target.acceptName(m_name);
    target.acceptChildren(m_children);
  }



  // Near-clone from ProcessNode.Wrapper
  public interface Wrapper {
    void acceptName(String name);
    void acceptChildren(ArrayList<HardwareTypeNode> children);
    void exportDone();
  }

  private HardwareTypeNode m_parent=null;
  private String       m_name=null;    /* component type name */
  private String       m_slotname=null; /* if child in assembly */
  // Maybe add field for lsst id of actual component if assigned or installed
  // private String       m_componentId=null;
  
  private boolean      m_isBatched=false;
  private int          m_quantity=1;   /* may be more if batched */
  private String       m_comments=null;
  private ArrayList<HardwareTypeNode> m_children=null;
  
  // private int          m_level; /* Might not need this. root cmpt is level 0 */
}
