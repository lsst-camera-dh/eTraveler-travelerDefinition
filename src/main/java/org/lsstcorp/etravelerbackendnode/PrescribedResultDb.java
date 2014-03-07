/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import org.lsstcorp.etravelerbackenddb.DbConnection;
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
  private static String[] s_patternCols = {"inputSemanticsId", "label",
    "units", "description", "minV", "maxV", "choiceField"};
  
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
  public PrescribedResultDb(DbConnection connect, String id) 
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
        System.out.println("query of prerequisite types failed with exception ");
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
    m_minV =  rs.getString(++ix);
    if (m_minV != null) {
      if (m_minV.isEmpty()) m_minV = null;
    }
    m_maxV =  rs.getString(++ix);
    if (m_maxV != null) {
      if (m_maxV.isEmpty()) m_maxV = null;
    }
    m_choiceField =  rs.getString(++ix);
    rs.close();
    if (!s_semanticsIdMap.containsKey(m_semanticsId))  {
        throw new UnknownDbId(m_semanticsId, "InputSemantics");
    }
    m_semantics = s_semanticsIdMap.get(m_semanticsId);
  }   // end constructor

  void verify(ConcurrentHashMap<String, String> smap)  throws EtravelerException {
    if (!smap.containsKey(m_semantics))  {
        throw new 
          EtravelerException("No such semantics type " + m_semanticsId);
    }
    m_semanticsId = smap.get(m_semantics);
    m_verified = true;
  }

  private static String[] s_insertResultCols=
  {"label", "inputSemanticsId", "processId", "description", "units", "createdBy",
   "minV", "maxV"};

  void writeToDb(DbConnection connect, ProcessNodeDb parent, String user) 
    throws    SQLException {
    String[] vals = new String[s_insertResultCols.length];
    vals[0] = m_label;
    vals[1] = m_semanticsId;
    vals[2] = parent.provideId();
    vals[3] = m_description;
    vals[4] = m_units;
    vals[5] = user;
    if (m_minV != null) {
      if (m_minV.isEmpty()) m_minV = null;
    }
    vals[6] = m_minV;
    if (m_maxV != null) {
      if (m_maxV.isEmpty()) m_maxV = null;
    }
    vals[7] = m_maxV;
 
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
  
  // ExportTarget interface
  public void acceptLabel(String label) { m_label = label;}
  public void acceptSemantics(String semantics) { m_semantics=semantics;}
  public void acceptUnits(String units) {m_units = units;}
  public void acceptMinValue(String minValue) {m_minV = minValue;}
  public void acceptMaxValue(String maxValue) {m_maxV = maxValue;}
  public void acceptResultDescription(String description) {
    m_description = description;
  }
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
  private DbConnection m_connect=null;
  private boolean m_verified=false;
 }
