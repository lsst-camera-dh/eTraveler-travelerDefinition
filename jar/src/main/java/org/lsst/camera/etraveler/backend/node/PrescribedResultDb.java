/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.node;
import org.lsst.camera.etraveler.backend.exceptions.UnknownDbId;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author jrb
 */
public class PrescribedResultDb implements PrescribedResult.Importer,
    PrescribedResult.ExportTarget {
  private static PreparedStatement s_inputPatternQuery;
  public static void reset() {
    s_inputPatternQuery = null;
  }
  private static String[] s_patternCols = {"inputSemanticsId", "label",
    "units", "description", "isOptional", "minV", "maxV", "choiceField",
    "roleBitmask"};
  
  private static ConcurrentHashMap<String, String> s_semanticsIdMap;
    
  /**
   * use this constructor when building for export from PrescribedResult 
   */
  public PrescribedResultDb(DbConnection connect) {
    m_connect = connect;
  }
  
  /**
   *    Use this constructor when building from database representation
   * @param connect
   * @param id
   * @throws SQLException
   * @throws EtravelerException 
   */
  public PrescribedResultDb(DbConnection connect, String id, 
    ConcurrentHashMap<String, String> pgmap) 
      throws SQLException, EtravelerException {
    m_id = id;
    if (s_inputPatternQuery == null) {
      String where = " WHERE id=?";
      s_inputPatternQuery = connect.prepareQuery("InputPattern", s_patternCols, where);
      // s_processQuery = connect.prepareQuery("Process", s_processCols, where);
 
      if (s_inputPatternQuery == null) {
        throw new SQLException("DbConnection.prepareQuery failure");
      }
      //  Get all input semantics type ids and names; save in map
      s_semanticsIdMap = new ConcurrentHashMap<String, String>();
      PreparedStatement semanticsIdQuery;
      String[] gets = {"id", "name"};
      semanticsIdQuery = connect.prepareQuery("InputSemantics", gets, "");
      ResultSet r;
      try {
        r = semanticsIdQuery.executeQuery();
        r.next();
        while (!r.isAfterLast()) {
          s_semanticsIdMap.put(r.getString("id"), r.getString("name"));
          r.next();
        }
        r.close();
      } catch (SQLException ex) {
        System.out.println("query of result types failed with exception ");
        System.out.println(ex.getMessage());
        throw ex;
      }
    }   // end of init section
    ResultSet rs;
    // String semanticsId;
    try {
      s_inputPatternQuery.setString(1, m_id);
      rs = s_inputPatternQuery.executeQuery();
      rs.next();
    }  catch (SQLException ex)  {
      throw ex;
    }
    int ix = 0;
    m_semanticsId = rs.getString(++ix);
    m_label =  rs.getString(++ix);
    m_units =  rs.getString(++ix);
    m_description =  rs.getString(++ix);
    m_isOptional = rs.getString(++ix);
    m_minV =  rs.getString(++ix);
    if (m_minV != null) {
      if (m_minV.isEmpty()) m_minV = null;
    }
    m_maxV =  rs.getString(++ix);
    if (m_maxV != null) {
      if (m_maxV.isEmpty()) m_maxV = null;
    }
    m_choiceField =  rs.getString(++ix);
    m_roleMask = rs.getString(++ix);
    rs.close();
    if (!s_semanticsIdMap.containsKey(m_semanticsId))  {
        throw new UnknownDbId(m_semanticsId, "InputSemantics");
    }
    m_semantics = s_semanticsIdMap.get(m_semanticsId);  
    if (m_roleMask.equals("0")) m_role="(?)";
    else {
      if (!pgmap.containsKey(m_roleMask))  {
        throw new EtravelerException("Unknown role mask bit " + m_roleMask);
      }
      m_role = pgmap.get(m_roleMask);
    }
  }   // end constructor

  void verify(ConcurrentHashMap<String, String> smap, 
      ConcurrentHashMap<String, String> pgmap)  throws EtravelerException {
    if (!smap.containsKey(m_semantics))  {
        throw new 
          EtravelerException("No such semantics type " + m_semanticsId);
    }
    m_semanticsId = smap.get(m_semantics);
    if (!m_semantics.equals("signature")) {
      m_roleMask = null;
    } else {
      if (m_role.equals("(?)") ) {
        m_roleMask = "0";
      } else {
        if (!pgmap.containsKey(m_role)) {
          throw new EtravelerException("No such role " + m_role);
        }
        m_roleMask = pgmap.get(m_role);
      }
    }
    m_verified = true;
  }

  private static String[] s_insertResultCols=
  {"label", "inputSemanticsId", "processId", "description", "units", "isOptional",
    "createdBy", "minV", "maxV", "roleBitmask"};

  void writeToDb(DbConnection connect, ProcessNodeDb parent, String user) 
    throws    SQLException {
    String[] vals = new String[s_insertResultCols.length];
    vals[0] = m_label;
    vals[1] = m_semanticsId;
    vals[2] = parent.provideId();
    vals[3] = m_description;
    vals[4] = m_units;
    vals[5] = m_isOptional;
    vals[6] = user;
    if (m_minV != null) {
      if (m_minV.isEmpty()) m_minV = null;
    }
    vals[7] = m_minV;
    if (m_maxV != null) {
      if (m_maxV.isEmpty()) m_maxV = null;
    }
    vals[8] = m_maxV;
    if (m_roleMask != null) {
      if (m_roleMask.isEmpty()) m_roleMask = null;
    }
    vals[9] = m_roleMask;
 
    try {
      m_id = m_connect.doInsert("InputPattern", s_insertResultCols, vals, "", 
          DbConnection.ADD_CREATION_TIMESTAMP);
    } catch (SQLException ex)  {
      System.out.println("Failed to create entry for result labeled " 
                         + m_label + "with exception");
      System.out.println(ex.getMessage());
      throw ex;
    }
  }

  // Importer interface
  public String provideLabel() {return m_label;}
  public String provideSemantics() {return m_semantics;} 
  public String provideDescription() {return m_description;}
  public String provideUnits() {return m_units;}
  public String provideMinValue() {return m_minV;}
  public String provideMaxValue() {return m_maxV;}
  public String provideChoiceField() {return m_choiceField;}
  public String provideIsOptional() {return m_isOptional;}
  public String provideRole() {return m_role;}
  
  // ExportTarget interface
  public void acceptLabel(String label) { m_label = label;}
  public void acceptSemantics(String semantics) { m_semantics=semantics;}
  public void acceptUnits(String units) {m_units = units;}
  public void acceptMinValue(String minValue) {m_minV = minValue;}
  public void acceptMaxValue(String maxValue) {m_maxV = maxValue;}
  public void acceptResultDescription(String description) {
    m_description = description;
  }
  public void acceptSignatureRole(String role) {
    m_role = role;
  }
  public void acceptIsOptional(String isOpt) {m_isOptional=isOpt;}
  public void acceptChoiceField(String choiceField) {
    m_choiceField = choiceField;
  }

  private String m_id=null;
  private String m_label="";
  private String m_semantics=null;
  private String m_semanticsId=null;
  private String m_units=null;
  private String m_description="";
  private String m_minV=null;
  private String m_maxV=null;
  private String m_choiceField=null;
  private String m_isOptional="0";
  private String m_role=null;
  private String m_roleMask=null;
  private DbConnection m_connect=null;
  private boolean m_verified=false;
 }
