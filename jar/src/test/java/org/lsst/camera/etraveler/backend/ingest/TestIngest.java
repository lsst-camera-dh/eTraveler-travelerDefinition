package org.lsst.camera.etraveler.backend.ingest;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.lsst.camera.etraveler.backend.node.Traveler;
import org.lsst.camera.etraveler.backend.util.SessionData;
import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestIngest {

  // Create and initialize FilterConfig instance for ModeSwitcherFilter
  // Create and initialize ModeSwitcherFilter instance?
  private String m_contentsSimple;
  private String m_contentsFailure;
  private HashMap<String, String> m_parms;
  private SessionData m_sessionData;

  /*
  public TestIngest(String testName) {
    super(testName);
  }
*/
  @Before
  public void setup() {
    StringBuilder bld = new StringBuilder(1000);
    bld = appendLine(bld, "Name: TestIngest_Fail1");
    bld = appendLine(bld, "Subsystem: CRFT");
    bld = appendLine(bld, "ShortDescription: Unknown subsystem; should fail"); 
    bld = appendLine(bld, "Version: next");
    bld = appendLine(bld, "HardwareGroup: borogove");
    m_contentsFailure = bld.toString();
    
    bld.delete(0, bld.length() -1);     
    bld = appendLine(bld, "Name: TestIngest_ok1");
    bld = appendLine(bld, "Subsystem: CR");
    bld = appendLine(bld, "ShortDescription: For use in simple unit test");
    bld = appendLine(bld, "HardwareGroup: borogove");
    bld = appendLine(bld, "Version: next");
    bld = appendLine(bld, "Sequence:");
    bld = appendLine(bld, "  - Name: TestIngest1_permissions");
    bld = appendLine(bld, "    ShortDescription: must be operator or QA");
    bld = appendLine(bld, "    PermissionGroups:");
    bld = appendLine(bld, "      - operator");
    bld = appendLine(bld, "      - qualityAssurance");
    bld = appendLine(bld, "    Version: next");
    bld = appendLine(bld, "  - Name: TestIngest1_OptionalInputs");
    bld = appendLine(bld, "    ShortDescription: define optional input");
    bld = appendLine(bld, "    Version: next");
    bld = appendLine(bld, "    OptionalInputs:");
    bld = appendLine(bld, "      - Label: Enter an integer ");
    bld = appendLine(bld, "        InputSemantics: int");
    bld = appendLine(bld, "        Description: test optional input");
    bld = appendLine(bld, "      - Label: Enter a string ");
    bld = appendLine(bld, "        InputSemantics: string");
    bld = appendLine(bld, "        Description: 2nd optional input");
    m_contentsSimple = bld.toString();
    
  

    m_parms = new HashMap<>();
    
    m_parms.put("operator", "jrb");
   
    // For now set localhost field to true.  That will change
    m_sessionData = new SessionData("Raw", "jdbc/rd-lsst-camt",
                                    "/nfs/farm/g/lsst/u3/ET",
                                    true, true);
    
  }

  /**
     For now caller must keep track of indent
   */
  private StringBuilder appendLine(StringBuilder bld, String line) {
    bld.append(line + "\n");
    return bld;
  }
  
  @Test
  public void validateFailure() {
      System.out.println("Running validateFailure test");
      m_parms.put("validateOnly", "true");
      m_parms.put("contents", m_contentsFailure);
      Map<String, String> results =
      Traveler.ingest(m_sessionData, m_parms);
    
      System.out.println("Summary: " + results.get("summary"));
      if (results.containsKey("acknowledge") ) {
        System.out.println("Messages: " + results.get("acknowledge"));
        assertNotNull("validation should have failed", 
          results.get("acknowledge"));
      }
  }

  @Test
  public void validateSimple() {
    //this.setName("ingestValidateOnlyTest");
    System.out.println("Running validateSimple test");
    m_parms.put("validateOnly", "true");
    m_parms.put("contents", m_contentsSimple);
    Map<String, String> results =
      Traveler.ingest(m_sessionData, m_parms);
    
    System.out.println("Summary: " + results.get("summary"));
    if (results.containsKey("acknowledge") ) {
      System.out.println("Messages: " + results.get("acknowledge"));
      assertNull("Validation should have succeeded",
        results.get("acknowledge"));
    }
  }
  
  @Test
  public void validateClone() {
    System.out.println("running validateClone test");
    //System.out.println("Working Directory = " + System.getProperty("user.dir"));
    // Working directing is eTraveler-travelerDefinition/jar
    String fp = "src/test/yaml/Clone_autotest.yaml";
   
    checkFile(fp, true);
  }
  
  @Test
  public void validateNextLast() {
    System.out.println("running validateNextLast test");
    String fp = "src/test/yaml/NextLast_autotest.yaml";
    
    checkFile(fp, true);
  }
  @Test
  public void validateOperatorInput() {
    System.out.println("running validateOperatorInput test");
    String fp = "src/test/yaml/OperatorInput_autotest.yaml";
    // Should validate and produce warning that signature field is irrelevant
    // for float input and will be ignored
    checkFile(fp, true);
  }
  /* Don't routinely run ingest test to avoid cluttering up db */
  @Ignore @Test
  public void ingestSimple() {
    System.out.println("Running ingestSimple test");
    m_parms.put("validateOnly", "false");
    m_parms.put("contents", m_contentsSimple);
    m_parms.put("responsible", "Joanne Bogart");
    m_parms.put("reason", "Simple ingest test");
    Map<String, String> results =
      Traveler.ingest(m_sessionData, m_parms);
    
    System.out.println("Summary: " + results.get("summary"));
    if (results.containsKey("acknowledge") ) {
      System.out.println("Messages: " + results.get("acknowledge"));
    }
  }
  
  private void checkFile(String fp, boolean validateOnly) 
  {
    if (validateOnly) {
      m_parms.put("validateOnly", "true");
    } else {
      m_parms.put("validateOnly", "false");
    }   
    String contents = "";
    try {
      FileReader f = new FileReader(fp);
      BufferedReader br = new BufferedReader(f);
      String line = br.readLine();
      while (line != null) {
        contents += line + "\n";
        line = br.readLine();
      }
    } catch  (FileNotFoundException ex)   {
      fail("Unable to open file " + fp);
      return;
    } catch (IOException ex) {
      fail("Exception while reading " + fp);
      return;
    }
    m_parms.put("contents", contents);
      Map<String, String> results =
      Traveler.ingest(m_sessionData, m_parms);
    
    System.out.println("Summary: " + results.get("summary"));
    if (results.containsKey("acknowledge") ) {
      System.out.println("Messages: " + results.get("acknowledge"));
      assertNull("Validation should have succeeded",
        results.get("acknowledge"));
    }
  }
}
