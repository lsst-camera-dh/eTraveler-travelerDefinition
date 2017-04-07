/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;

import java.io.Writer;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
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
  private boolean m_includeDbInternal = false;
  private String m_subsystem=null;
  private String m_standaloneNCR=null;
  
  public TravelerToYamlVisitor(String dbSource)  {
    m_dbSource = dbSource;
    m_yaml = new Yaml();
  }
  public void setIncludeDbInternal(boolean val)  {m_includeDbInternal = val;}
  public boolean getIncludeDbInternal() {return m_includeDbInternal;}
  public void setSubsystem(String sub) {m_subsystem = sub;}
  public String getSubsystem() {return m_subsystem;}
  public void setStandaloneNCR(String NCR) {m_standaloneNCR = NCR;}
  public String getStandaloneNCR() {return m_standaloneNCR;}
  public void visit(ProcessNode process, String activity, Object cxt) throws EtravelerException {
    Object cxtToPass = cxt;
    boolean topNode = false;
    if (cxtToPass == null) {
      data = new HashMap<String, Object>();
      cxtToPass = data;
      topNode = true;
    }
    ProcessNodeToYaml yamlChild = new ProcessNodeToYaml(this, (HashMap<String, Object>) cxtToPass);
    yamlChild.setIsRoot(topNode);
    process.exportTo(yamlChild);
    if (topNode)  {
      yamlChild.acceptSourceDb(m_dbSource);
      yamlChild.acceptSubsystem(m_subsystem);
      yamlChild.acceptStandaloneNCR(m_standaloneNCR);
    }
  }
  public void addChild(ArrayList< HashMap<String, Object> > children, ProcessNode process,
      String activity) throws EtravelerException {
    HashMap<String, Object> childMap = new HashMap<String, Object>();
    visit(process, "", childMap);
    children.add(childMap);
  }
  public void visit(PrescribedResult result, String activity, Object cxt) 
      throws EtravelerException {
    ResultToYaml yamlResult = new ResultToYaml(this, (Map<String, Object> )cxt);
    result.exportTo(yamlResult); 
  }
  
   public void addPrescribedResult(ArrayList< HashMap<String, Object> > prescribedList, 
       PrescribedResult pre,  String activity) throws EtravelerException {
    HashMap<String, Object> prescribedMap = new HashMap<String, Object>();
    visit(pre, "", prescribedMap);
    prescribedList.add(prescribedMap);
  }
  public void visit(Prerequisite prerequisite, String activity, Object cxt) 
      throws EtravelerException {
    PrerequisiteToYaml yamlPrereq = new PrerequisiteToYaml(this, (Map<String, Object> )cxt);
    prerequisite.exportTo(yamlPrereq);
  }
  
  public void addPrerequisite(ArrayList< HashMap<String, Object> > prereqList, Prerequisite pre,
      String activity) throws EtravelerException {
    HashMap<String, Object> prereqMap = new HashMap<String, Object>();
    visit(pre, "", prereqMap);
    prereqList.add(prereqMap);
  }
  
  public void visit(RelationshipTask rel, String activity, Object cxt)
      throws EtravelerException {
    RelationshipTaskToYaml yamlRela = new RelationshipTaskToYaml(this, (Map<String, Object>) cxt);
    rel.exportTo(yamlRela);
  }
  
  public void addRelationshipTask(ArrayList< HashMap<String, Object> > relList, 
      RelationshipTask rel, String activity) throws EtravelerException {
    HashMap<String, Object> relMap = new HashMap<String, Object>();
    visit(rel, "", relMap);
    relList.add(relMap);
  }
  public String dump(Writer wrt)  {
    if (data != null) {
      m_yaml.dump(data, wrt);
    }
    return "";
  }
  
}
