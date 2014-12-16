/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.db;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author jrb
 */

public interface DbConnection {

  int DBOPTION_READDEFAULTFILE = 1;
  int DBOPTION_READDEFAULTGROUP = 2;
  
  static public int ADD_CREATION_TIMESTAMP=1;

  boolean setOption(int option, String value);

  boolean open(String host, String userid, String pwd, String dbname);
  
  /**
   * Ask Tomcat for a connection in its pool
   * @param name
   * @return  True if successful
   */
  boolean openTomcat(String name);

  boolean close();

  boolean isConnected();

  java.sql.Connection getConnection();

  /**
   *
   * @param table table in which row is to be inserted
   * @param cols column names to be set
   * @param vals values for columns.  Use null to set to default
   * @param where either "" or string starting with " where ". 
   * @param extras bit mask to indicate system-generated columns
   * @return   auto-generated key
   */
  String doInsert(String table, String[] cols,
      String[] vals, String where, int extras) throws SQLException;
  /**
   * Generic update routine
   * @param table      Table to be updated
   * @param cols       Columns to be updated
   * @param vals       Values for new columns
   * @param where      Condition to select rows to be updated
   * @param extras      Extra options, e.g. for locking
   * @throws SQLException 
   */
  void doUpdate(String table, String[] cols, String[] vals,
      String where, int extras) throws SQLException;
  
  /**
   * Create a PreparedStatement which does a query (SELECT). Typically it will
   * be modified by one or more PreparedStatement.set... calls, affecting
   * information in the WHERE clause.
   *
   * @param table Table being queried (maybe need ArrayList here?)
   * @param cols Fields to be retrieved
   * @param where WHERE clause (may be "")
   * @return a PreparedStatement
   */
  java.sql.PreparedStatement prepareQuery(String table, String[] cols,
          String where);
  /**
   * Do a SELECT which should return a single field from one row.
   * If result set is not consistent with this, complain
   * @param table   Table to be queried
   * @param col     Field whose value is to be returned
   * @param where   WHERE clause
   * @return        Field value if all goes well; else null
   */
  String fetchColumn(String table, String col, String where);
  
  /**
   *  Return array of values for specified column, satisfying "where" condition 
   * @param tableSpec   Table to be queried
   * @param col         Column to be fetched
   * @param where       WHERE clause
   * @return            ArrayList<String> of values or null
   */
  ArrayList<String> fetchColumnMulti(String tableSpec, String col, String where);
  /**
   * Update a single column to new value according to condition 'where'
   * @param table  Table to be updated
   * @param col    Column to be updated
   * @param val    New value
   * @param where   Condition
   * @return 
   */
  void updateColumn(String table, String col, String val, String where, 
      int extras) throws SQLException;
  boolean getAutoCommit() throws SQLException;
  void setAutoCommit(boolean autoCommit) throws SQLException;
  int  getTransactionIsolation() throws SQLException;
  void setTransactionIsolation(int level) throws SQLException;
  void commit() throws  SQLException;
  void rollback() throws SQLException;
  void setReadOnly(boolean readOnly) throws SQLException;
  boolean isReadOnly() throws SQLException;
  void setSourceDb(String dbSource);
  String getSourceDb();
  
}
