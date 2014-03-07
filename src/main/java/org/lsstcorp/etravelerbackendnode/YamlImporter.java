/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import javax.servlet.jsp.PageContext;
/**
 *
 * @author jrb
 */
public class YamlImporter {
  /**
   * Upload a local file
   * @param fileContents  String to be interpreted as Yaml
   * @return 
   */
 public static String parse(PageContext context)  { 
 // public static String parse(String fileContents)  {  
    String fileContents = context.getRequest().getParameter("importYamlFile");
    
    Yaml yaml = new Yaml();
    Map yamlMap = null;
    try {
      yamlMap = (Map<String, Object>) yaml.load(fileContents);
    } catch (Exception ex) {
      System.out.println("failed to load yaml with exception " + ex.getMessage());
      return ex.getMessage();
    }
    ProcessNodeYaml topYaml = new ProcessNodeYaml();
    try {
      topYaml.readYaml(yamlMap, null, false, 0, null);
    } catch (Exception ex) {
      System.out.println("failed to process yaml with exception " + ex.getMessage());
      return ex.getMessage();
    }
    System.out.println("Loaded file into Map of size  " + yamlMap.size());
    ProcessNode traveler;
    try {
      traveler = new ProcessNode(null, topYaml);
    } catch (Exception ex) {
      System.out.println("failed to import from yaml with exception " + ex.getMessage());
      return ex.getMessage();
    }
    return "Successfully imported from Yaml";
  }
  
}
