
package org.lsst.camera.etraveler.backend.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
//import java.io.Writer;
import java.util.ArrayList;

import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;

/**
  General purpose static routines concerning generic labels
*/
public class LabelUtil {

  /**
     Find any labels associated with object with id=id, labelable type=labelable
     Return array list of strings of form labelGroup:labelName
  */
  public static ArrayList<String>
    getLabels(Connection connect, String id, String labelable)
  throws SQLException {
    String sql=
      "select L.id as labelId,concat(LG.name,':',L.name) as fullname from Label L join LabelHistory LH on L.id=LH.labelId join Labelable on Labelable.id=LH.labelableId join LabelGroup LG on LG.id=L.labelGroupId where LH.objectId='" + id + "' and LH.adding=1 and Labelable.name='" + labelable + "' and LH.id in (select max(id) from LabelHistory group by objectId,labelId)";
    PreparedStatement stmt =
      connect.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
    ResultSet rs = stmt.executeQuery();
    boolean gotRow = rs.first();
    if (!gotRow) return null;
    ArrayList<String> fullnames = new ArrayList<String>();
    while (gotRow) {
      fullnames.add(rs.getString("fullname"));
      gotRow = rs.relative(1);
    }
    stmt.close();
    return fullnames;
  }
  /** 
      Check that labels already exist in the db
  */
  public static ArrayList<String> verifyLabels(Connection connect,
                                   ArrayList<String> labels, String ltype)
    throws EtravelerException, SQLException {
    String sql="select L.id from Label L join LabelGroup LG on L.labelGroupId=LG.id join Labelable on LG.labelableId=Labelable.id where Labelable.name='"
      + ltype + "' and ? = concat(LG.name,':',L.name)";
    PreparedStatement stmt=
      connect.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
    ResultSet rs;
    ArrayList<String> returnIds = new ArrayList<>();
    for (String fullname: labels) {
      stmt.setString(1, fullname);
      rs = stmt.executeQuery();
      boolean gotOne = rs.first();
      if (!gotOne ) {
        stmt.close();
        throw new EtravelerException("Unknown label " + fullname);
      }
      returnIds.add(rs.getString("id"));
    }
    stmt.close();
    return returnIds;
  }
  /**
     Add collection of labels to the object.   Assume it's a new
     object so no need to check if it already has any of them
   */
  public static void addLabels(Connection connect, ArrayList<String> ids,
                                String objectType, String objectId,
                                String user, String reason)
    throws SQLException, EtravelerException {
    if (reason == null) reason="";
    String sql="insert into LabelHistory set objectId='" + objectId +
      "',labelableId=(select id from Labelable where name='" + objectType +
      "'),labelId=?,reason='" + reason + "',adding=1,createdBy='" +
      user + "',creationTS=UTC_TIMESTAMP()";
    PreparedStatement stmt=
      connect.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
    try {
      for (String id: ids) {
        stmt.setString(1,id);
        stmt.executeUpdate();
      }
    } catch (Exception ex) {
      stmt.close();
      //rethrow??
      throw ex;
    }
    stmt.close();
  }
  
}
