package org.lsst.camera.etraveler.backend.node;

import org.lsst.camera.etraveler.backend.exceptions.UnrecognizedYamlKey;
import org.lsst.camera.etraveler.backend.exceptions.WrongTypeYamlValue;
import org.lsst.camera.etraveler.backend.exceptions.NullYamlValue;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.nodes.Node;

/**
 *
 * @author jrb
 */
public class RelationshipTaskYaml implements RelationshipTask.Importer {
  private static ArrayList<String> s_knownKeys = null;
  static final int RELATIONSHIPNAME=0;
  static final int RELATIONSHIPACTION=1;
  static final int RELATIONSHIPSLOT=2;
  static final int FROMSOURCERELATIONSHIPTASKID=3;

  public void readYaml(Map<String, Object> yamlMap, ProcessNodeYaml parent, int iPre)
      throws EtravelerException {
    if (s_knownKeys == null)  {
      s_knownKeys = new ArrayList<String>(FROMSOURCERELATIONSHIPTASKID + 1);
      s_knownKeys.add(RELATIONSHIPNAME, "RelationshipName");
      s_knownKeys.add(RELATIONSHIPACTION, "RelationshipAction");
      s_knownKeys.add(RELATIONSHIPSLOT, "RelationshipSlot");
      s_knownKeys.add(FROMSOURCERELATIONSHIPTASKID, "FromSourceRelationshipTaskId");
    }
    m_parent = parent;
    
    Iterator<String> it = yamlMap.keySet().iterator();
   
    while (it.hasNext()) {
      String foundKey = it.next();
      int keyIx = s_knownKeys.indexOf(foundKey);
      if (keyIx == -1) {
        throw new UnrecognizedYamlKey(foundKey, "RelationshipTask");
      }
      
      if (yamlMap.get(foundKey) == null)  {
        throw new NullYamlValue(foundKey, "RelationshipTask", "");
      }
      String v = yamlMap.get(foundKey).toString();
      switch (keyIx)  {
      case RELATIONSHIPNAME:
        m_name = v; break;
      case RELATIONSHIPACTION:
        m_action = v; break;
      case RELATIONSHIPSLOT:
        m_slot = v;
       /* Ignore informational keys written by export */
      case FROMSOURCERELATIONSHIPTASKID:
        break;
      }
    } 
    if (m_name == null) {
      throw new NullYamlValue("RelationshipName", "RelationshipTask", "");
    }
    if (m_action == null) {
      throw new NullYamlValue("RelationshipAction", "RelationshipTask", "");
    }
  }
  public String provideRelationshipName() {return m_name;}
  public String provideRelationshipAction() {return m_action;}
  public String provideRelationshipSlot() {return m_slot;}
  
  private String m_name="";
  private String m_action="";
  private String m_slot="ALL";
  private ProcessNodeYaml m_parent=null;
}
