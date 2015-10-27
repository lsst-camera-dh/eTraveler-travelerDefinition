
package org.lsstcorp.etravelerbackend.transport;
import org.lsstcorp.etravelerbackend.db.DbConnection;
import org.lsstcorp.etravelerbackend.exceptions.UnknownDbId;
import org.lsstcorp.etravelerbackend.exceptions.EtravelerException;
import java.sql.SQLException;

/**
 * Interface to be satisfied by any objects which are to be copied from
 * one eTraveler db to another (e.g. from Dev to Prod)
 * Might also add methods to import by name, rather than id,
 * and getName() and getId() methods 
 * @author jrb
 */
public interface Transportable {
  /**
   * Initialize an 'empty' Transportable object from db specified by conn
   * id identifies which object. Typically it's an auto-increment field
   * in the table associated with the object type
   * Should id be int or String?
   * @param conn  Database connection
   * @param id    Identifies object to be imported
   */
  void importFrom(DbConnection conn, int id) throws UnknownDbId, SQLException;
  /**
   *  Export an object to db specified by conn
   * @param conn   Database connection
   * @return       id of newly-created object in table assoc. with object type
   */
  int exportTo(DbConnection conn, String user) throws SQLException, EtravelerException;
  
  String getName();

  /*
  int transport(DbConnection readConn, DbConnection writeConn, int id,
                String user) throws UnknownDbId, SQLException, EtravelerException;
                */
}
