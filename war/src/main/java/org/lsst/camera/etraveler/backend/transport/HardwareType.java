/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.transport;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import org.lsst.camera.etraveler.backend.exceptions.UnknownDbId;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 *
 * @author jrb
 */
public class HardwareType implements Transportable {
  private String m_oldId=null;
  private String m_newId=null;
  private String m_name=null;
  private String m_isBatched=null;
  private String m_description=null;
  private String m_autoSequenceWidth=null;
  private String m_autoSequence=null;
  private String m_trackingType=null;
  private boolean m_importDone=false;
  private static String [ ] s_getCols=
  {"name","isBatched","description", "autoSequenceWidth", "autoSequence", 
   "trackingType"};
  private static String [ ] s_putCols=
  {"name", "isBatched", "description", "autoSequenceWidth", "autoSequence", 
   "trackingType", "createdBy"};
                                       
  
  
  public HardwareType() {}
  public void importFrom(DbConnection conn, int id) throws UnknownDbId, 
      SQLException {
    m_oldId = Integer.toString(id);
    String where = " where id=" + m_oldId;
    PreparedStatement q = conn.prepareQuery("HardwareType", s_getCols, where);
    ResultSet rs=null;

    try {
      rs = q.executeQuery();
      rs.next();
      int ix = 0;
      m_name = rs.getString(++ix);
      m_isBatched = rs.getString(++ix);
      m_description = rs.getString(++ix);
      m_autoSequenceWidth = rs.getString(++ix);
      m_autoSequence = rs.getString(++ix);
      m_trackingType = rs.getString(++ix);
      m_importDone=true;
    } catch (SQLException ex) {
      // maybe print a message and rethrow?
      System.out.println("Query for HardwareType id=" + id + 
                         " failed with error ");
      System.out.println(ex.getMessage());
      throw ex;
    } finally {
      rs.close();
    }
    
  }
  public int exportTo(DbConnection conn, String user) 
    throws SQLException, EtravelerException  {
    String [] vals= {m_name, m_isBatched, m_description, m_autoSequenceWidth,
                     m_autoSequence, m_trackingType, user};
    if (!m_importDone) {
      throw new 
        EtravelerException("HardwareType object is not ready for export");
    }
    try {
      m_newId=conn.doInsert("HardwareType", s_putCols, vals, " ", 
                            DbConnection.ADD_CREATION_TIMESTAMP);
      
    } catch (SQLException ex) {
      System.out.println("Failed to export Hardware type'"+m_name+"'");
      throw ex;
    }
    return Integer.parseInt(m_newId);
  }
  
  public String getName() {return m_name;}

  /*
  public int transport(DbConnection readConn, DbConnection writeConn, int id,
               String user) throws UnknownDbId, SQLException, EtravelerException {
   importFrom(readConn, id);
   return exportTo(writeConn, user);
 }
 */ 
}
