/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.nodes.Node;

/**
 *
 * @author jrb
 */
public class PrerequisiteYaml implements Prerequisite.Importer {
  private static ArrayList<String> s_knownKeys = null;
  static final int NAME=0;
  static final int PREREQUISITETYPE=1;
  static final int USERVERSIONSTRING=2;
  static final int VERSION=3;
  static final int DESCRIPTION=4;
  static final int QUANTITY=5;
    
  public void readYaml(Map<String, Object> yamlMap, ProcessNodeYaml parent, int iPre)
      throws EtravelerException {
    if (s_knownKeys == null)  {
      s_knownKeys = new ArrayList<String>(QUANTITY + 1);
      s_knownKeys.add(NAME, "Name");
      s_knownKeys.add(PREREQUISITETYPE, "PrerequisiteType");
      s_knownKeys.add(USERVERSIONSTRING, "UserVersionString");
      s_knownKeys.add(VERSION, "Version");   
      s_knownKeys.add(DESCRIPTION, "Description");
      s_knownKeys.add(QUANTITY, "Quantity");
    }
    m_parent = parent;
    
    Iterator<String> it = yamlMap.keySet().iterator();
   
    while (it.hasNext()) {
      String foundKey = it.next();
      int keyIx = s_knownKeys.indexOf(foundKey);
      if (keyIx == -1) {
        throw new UnrecognizedYamlKey(foundKey, "Prerequisite");
      }
      
    
      if (yamlMap.get(foundKey) == null)  {
        throw new NullYamlValue(foundKey, "Prerequisite");
      }
      String v = yamlMap.get(foundKey).toString();
      switch (keyIx)  {
      case NAME:
        m_name = v; break;
      case PREREQUISITETYPE:
        m_prerequisiteType = v; break;
      case DESCRIPTION:
        m_description = v; break;
      case QUANTITY:
        try {
          m_quantity = Integer.parseInt(v);
        } catch (NumberFormatException ex)  {
          throw new WrongTypeYamlValue(foundKey, v, "Prerequisite");
        }
        break;
      case VERSION:
        m_version = v; 
        try {
          Integer.parseInt(v);
        } catch (NumberFormatException ex)  {
          throw new WrongTypeYamlValue(foundKey, v, "Prerequisite");
        }
        break;
      case USERVERSIONSTRING:
        m_userVersionString = v; break;
      }
    } 
  }
  public String provideName() {return m_name;}
  public String provideType() {return m_prerequisiteType;}
  public String provideDescription() {
    return m_description;
  }
  public int provideQuantity() {return m_quantity;}
  public String provideUserVersionString() {return m_userVersionString;}
  public String provideVersion() { return m_version;}
  
  private String m_name;
  private String m_prerequisiteType;
  private String m_description="";
  private int m_quantity = 1;
  private String m_userVersionString="";
  private String m_version="";
  private ProcessNodeYaml m_parent=null;
}
