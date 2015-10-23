/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.transport;
import org.lsstcorp.etravelerbackend.db.DbConnection;
import org.lsstcorp.etravelerbackend.exceptions.UnknownDbId;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author jrb
 */
public class HardwareType implements Transportable {
  private String m_oldId=null;
  private String m_newId=null;
  private String m_name=null;
  private String m_isBatched=null;
  private String m_autoSequenceWidth=null;
  private String m_autoSequence=null;
  private String m_description=null;
  private String m_trackingType=null;
  private static String [ ] s_getCols=
  {"id", "name","isBatched","description",
   "autoSequenceWidth", "autoSequence", "trackingType"};
  private static String [ ] s_putCols=
  {"name", "isbatched", "description", "autoSequenceWidth", "autoSequence", 
   "trackingType", };
                                       
  
  
  public HardwareType() {}
  public void importFrom(DbConnection conn, int id) throws UnknownDbId {
    m_oldId = Integer.toString(id);
    String where = " where id=" + m_oldId;
    PreparedStatement q = conn.prepareQuery("HardwareType", s_getCols, where);
    ResultSet rs;

    try {
      q.executeQuery();
    }
  }
  public int exportTo(DbConnection conn, String user) throws SQLException {
    return 0;
  }

 public int transport(DbConnection readConn, DbConnection writeConn, int id,
               String user) throws UnknownDbId, SQLException {
   importFrom(readConn, id);
   return exportTo(writConn, user);
}
