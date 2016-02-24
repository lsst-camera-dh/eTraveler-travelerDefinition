package org.lsst.camera.etraveler.backend.ingest;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.lsst.camera.etraveler.backend.node.Traveler;
import org.lsst.camera.etraveler.backend.util.SessionData;
import java.util.Map;
import java.util.HashMap;

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
      m_parms.put("validateOnly", "1");
      m_parms.put("contents", m_contentsFailure);
      Map<String, String> results =
      Traveler.ingest(m_sessionData, m_parms);
    
      System.out.println("Summary: " + results.get("summary"));
      if (results.containsKey("acknowledge") ) {
        System.out.println("Messages: " + results.get("acknowledge"));
      }
  }

  @Test
  public void validateSimple() {
    //this.setName("ingestValidateOnlyTest");
    m_parms.put("validateOnly", "1");
    m_parms.put("contents", m_contentsSimple);
    Map<String, String> results =
      Traveler.ingest(m_sessionData, m_parms);
    
    System.out.println("Summary: " + results.get("summary"));
    if (results.containsKey("acknowledge") ) {
      System.out.println("Messages: " + results.get("acknowledge"));
    }
  }
  
  @Test
  public void ingestSimple() {
    //this.setName("ingestValidateOnlyTest");
    m_parms.put("validateOnly", "0");
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
  
}
