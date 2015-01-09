/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node;

import java.io.Writer;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import  org.yaml.snakeyaml.Yaml;
/**
 *
 * @author jrb
 */
public class TravelerToYamlVisitor implements TravelerVisitor {
  
  private Yaml m_yaml;
  private String m_dbSource;
  private Map<String, Object> data;
  
  public TravelerToYamlVisitor(String dbSource)  {
    m_dbSource = dbSource;
    m_yaml = new Yaml();
  }
  public void visit(ProcessNode process, String activity, Object cxt) throws EtravelerException {
    Object cxtToPass = cxt;
    if (cxtToPass == null) {
      data = new HashMap<String, Object>();
      cxtToPass = data;
    }
    ProcessNodeToYaml yamlChild = new ProcessNodeToYaml(this, (HashMap<String, Object>) cxtToPass);
    process.exportTo(yamlChild);
  }
  public void addChild(ArrayList< HashMap<String, Object> > children, ProcessNode process,
      String activity) throws EtravelerException {
    HashMap<String, Object> childMap = new HashMap<String, Object>();
    visit(process, "", childMap);
    children.add(childMap);
  }
  public void visit(PrescribedResult result, String activity, Object cxt) 
      throws EtravelerException {
    
  }
  public void visit(Prerequisite prerequisite, String activity, Object cxt) 
      throws EtravelerException {
    
  }
  public String dump(Writer wrt)  {
    if (data != null) {
      m_yaml.dump(data, wrt);
    }
    // return wrt.toString();
    return "";
  }
  
}
