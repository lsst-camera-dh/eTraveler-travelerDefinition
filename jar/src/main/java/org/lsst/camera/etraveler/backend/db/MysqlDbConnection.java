/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.db;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.jsp.JspException;
import org.srs.web.base.db.ConnectionManager;
import org.lsst.camera.etraveler.backend.util.SessionData;


/**
 * Handle Mysql db connections. Services include open, close and configure
 * (setOption method)
 *
 * @author jrb
 */
public class MysqlDbConnection implements DbConnection {
  public MysqlDbConnection() {  }
  private Connection m_connect = null;
  /* Give caller a place to store identifying string */
  private String m_sourceDb = null;
  private boolean m_readOnly = false;

  public boolean setOption(int option, String value) {
    return true;
  }
  public boolean openTomcat(String name) {
    try {
      m_connect = ConnectionManager.getConnection(name);
    } catch (JspException ex) {
      System.out.println("Failed to get connection");
      System.out.println(ex.getMessage());
      return false;
    }
    return true;
  }

  /*
  @Override
  public boolean open(String host, String userid, String pwd, String dbname) {
    if (m_connect != null) {
      System.out.println("Close old connection first");
      return false;
    }
    String dbUrl = "jdbc:mysql://mysql-dev01.slac.stanford.edu:3307/";
    try {
      Class.forName("com.mysql.jdbc.Driver");
      dbUrl += dbname;
      m_connect = DriverManager.getConnection(dbUrl, userid, pwd);
    } catch (Exception ex) {
      System.out.println("Failed to connect to " + dbUrl);
      System.out.println(ex.getMessage());
      return false;
    }
    return true;
  }
  */

  public boolean open(SessionData sd) {
    if (!sd.getStandalone() ) return openTomcat(sd.getDatasource());

    // Following Max's prescription
    String dbUrl = "jdbc:mysql://mysql-dev01.slac.stanford.edu:3307/";
    if (sd.getDbType().equals("Prod")) {
        dbUrl = "jdbc:mysql://mysql-node03.slac.stanford.edu/";
    }

    String uPropertyName = sd.getDbType() + ".username";
    String pPropertyName = sd.getDbType() + ".pwd";
    String dbPropertyName = sd.getDbType() + ".dbname";
    String username = System.getProperty(uPropertyName);
    String pwd = System.getProperty(pPropertyName);
    String dbname = System.getProperty(dbPropertyName);
    dbUrl += dbname;
    if (username == null) {
        System.out.println("Null value for property " + uPropertyName);
        return false;
    } 
    if (pwd == null) {
        System.out.println("Null value for property " + pPropertyName);
        return false;
    }

    try {
      m_connect = DriverManager.getConnection(dbUrl, username, pwd);
    } catch (Exception ex) {
      System.out.println("Failed to get connection to db " + sd.getDbType());
      return false;
    }
    return true;
  }
  /**
   * Build and execute an insert statement; return auto-generated key
   *
   * @param table Table in which to insert
   * @param cols columns to be set
   * @param vals values. May be "?" if intended to be reset
   * @param where WHERE clause (may be "")
   * @return Statement
   */
  @Override
  public String doInsert(String table, String[] cols,
                         String[] vals, String where,
                         int extras) throws SQLException {
    String theSql = assembleInsert(table, cols, vals, where, extras);

    Statement stmt;
    ResultSet genKeys;
    try {
      stmt = m_connect.createStatement();
      stmt.execute(theSql, Statement.RETURN_GENERATED_KEYS);
      genKeys = stmt.getGeneratedKeys();
      boolean more = genKeys.next();
      if (!more) {return null;}
      return genKeys.getString(1);
    } catch (SQLException ex) {
      System.out.println("Insert failed with exception ");
      System.out.println(ex.getMessage());
      throw ex;
    }
  }

  public void doUpdate(String table, String[] cols, String[] vals,
                       String where, int extras) throws SQLException {
    String theSql = assembleUpdate(table, cols, vals, where, extras);
    Statement stmt;
    try {
      stmt = m_connect.createStatement();
      stmt.executeUpdate(theSql);
    } catch (SQLException ex) {
      System.out.println("Update failed with exception ");
      System.out.println(ex.getMessage());
      throw ex;
    } 
    return;
  }
    
  /**
   * Implementation of ConnectionDb.prepareQuery
   *
   * @param tableSpec Table(s) to be queried
   * @param cols Fields to be fetched
   * @param where WHERE clause (may be "")
   * @return a PreparedStatement. May be modified before execution and reused.
   */
  @Override
  public PreparedStatement prepareQuery(String tableSpec, String[] cols,
          String where) {
    int len = Array.getLength(cols);
    if (len == 0) {
      throw new IllegalArgumentException("No items to be selected!");
    }
    String theSql = "SELECT " + cols[0];
    for (int i = 1; i < len; i++) {
      theSql += "," + cols[i];
    }
    theSql += " from " + tableSpec;  // single table or join
    theSql += " " + where;
    PreparedStatement stmt;
    try {
      stmt = m_connect.prepareStatement(theSql, ResultSet.TYPE_SCROLL_INSENSITIVE);
    } catch (SQLException ex) {
      System.out.println("Couldn't prepare statement");
      return null;
    }
    return stmt;
  }
  
  public String fetchColumn(String tableSpec, String col, String where) {
     String[] cols = new String[1];
     cols[0] = col;
     PreparedStatement stmt = prepareQuery(tableSpec, cols, where);
     ResultSet rs;
     String val = null;
     try {
       rs = stmt.executeQuery();
       boolean ok = rs.first();
       if (ok) {
         val = rs.getString(col);
         if (rs.relative(1))  {
           // too many rows returned
           val = null;
         }
       }
     } catch (SQLException ex) {
       System.out.println("Failed to fetch column'" + col + "'with tableSpec=" + tableSpec +
           " and WHERE clause='" + where + " with error " + ex.getMessage() );
       val = null;
     }  
     try {
       stmt.close();
     } catch (SQLException ex) { }
     
     return val;
   }
  /**
   *   Same as fetchColumn but doesn't expect only single row to satisfy query
   * @param tableSpec
   * @param col
   * @param where
   * @return Array of string values or null
   */
  public ArrayList<String> fetchColumnMulti(String tableSpec, String col, String where) {
     String[] cols = new String[1];
     cols[0] = col;
     PreparedStatement stmt = prepareQuery(tableSpec, cols, where);
     ResultSet rs;
     ArrayList<String> vals = null;
     try {
       rs = stmt.executeQuery();
       boolean ok = rs.first();
       if (ok) {
         vals = new ArrayList<String>();
         vals.add(rs.getString(col));
         while (rs.relative(1))  {
           vals.add(rs.getString(col));
         }
       }
     } catch (SQLException ex) {
       vals = null;
     }  
     try {
       stmt.close();
     } catch (SQLException ex) { }
     
     return vals;
   }
  public void updateColumn(String table, String col, String val, String where,
      int extras) throws SQLException {
    String[] cols = new String[1];
    String[] vals = new String[1];
    cols[0] = col; vals[0] = val;
    doUpdate(table, cols, vals, where, extras);
  } 

  @Override
  public boolean close() {
    if (m_connect == null) {
      return true;
    }
    try {
      if (m_readOnly) setReadOnly(false);
      setAutoCommit(true);
      m_connect.close();
      m_connect = null;
    } catch (SQLException ex) {
      System.out.println("Failed to close db connection");
      System.out.println(ex.getMessage());
      m_connect = null;
      return false;
    }
    return true;
  }
 
  /**
   *
   * getConnection returns the underlying java.sql.Connection object, needed in
   * order to create a Statement or PreparedStatement for use on the connection.
   */
  @Override
  public Connection getConnection() {
    return m_connect;
  }

  @Override
  public boolean isConnected() {
    return (m_connect != null);
  }
  public void setSourceDb(String src) {
    m_sourceDb = src;
  }
  public String getSourceDb() {return m_sourceDb; }
  public boolean getAutoCommit() throws SQLException {
    return m_connect.getAutoCommit();
  }
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    m_connect.setAutoCommit(autoCommit);
  }
  public int  getTransactionIsolation() throws SQLException {
    return m_connect.getTransactionIsolation();
  }
  public void setTransactionIsolation(int level) throws SQLException {
    m_connect.setTransactionIsolation(level);
  }
  public void commit() throws SQLException {
   m_connect.commit(); 
  }
  public void rollback() throws SQLException {
    m_connect.rollback();
  }
  public boolean isReadOnly() throws SQLException {
    m_readOnly = m_connect.isReadOnly();
    return m_readOnly;
  }
  public void setReadOnly(boolean readOnly) throws SQLException {
    m_connect.setReadOnly(readOnly);
    m_readOnly = readOnly;
  }
   /**
   * Utility to do most of the work of buildInsert. Throw
   * exception if lengths of cols and vals don't match.
   * Current implementation does not support ? replacement
   *
   * @param table table in which to insert
   * @param cols field names to be set
   * @param vals values for above fields
   * @param where WHERE clause
   * @return String containing "INSERT into .. "
   */
  private String assembleInsert(String table, String[] cols, String[] vals,
                                String where, int extras) {
    String theSql = "INSERT into " + 
      assembleValues(table, cols, vals, where, extras);
    return theSql;
  }
  private String assembleUpdate(String table, String[] cols, String[] vals,
                                String where, int extras) {
    String theSql = "UPDATE " +
      assembleValues(table, cols, vals, where, extras);
    return theSql;
  }

  private String assembleValues(String table, String[] cols, String[] vals,
                                String where, int extras) {
    if (Array.getLength(cols) != Array.getLength(vals)) {
      throw new IllegalArgumentException("cols and vals must be same size");
    }

    if (Array.getLength(cols) == 0) {
      throw new IllegalArgumentException("Must specify at least 1 column");
    }

    String sql = table + " set " + cols[0];
    if (vals[0] == null) {
      sql += "=DEFAULT ";
    }  else {
      vals[0] = escapeSingle(vals[0]);
      sql += "='" + vals[0] + "' ";
    }
    for (int i = 1; i < Array.getLength(cols); i++) {
      sql += ", " + cols[i];
      if (vals[i] == null) {
        sql += "=DEFAULT ";
      }  else {
        sql += "='" + escapeSingle(vals[i]) + "' ";
      }
    }
    if ((extras & DbConnection.ADD_CREATION_TIMESTAMP) != 0)  {
      sql += ", creationTS=UTC_TIMESTAMP() ";
    }
    sql += " " + where;
    return sql;
  }
  /**
   * Replace all occurrences of singe quote (') with escaped version (\')
   * @param str
   * @return 
   */
  private static String escapeSingle(String str)  {
    String ret="";
    int startIx = 0;
    int ix = str.indexOf("'", startIx);
    while (ix != -1) {
      ret = ret + str.substring(startIx, ix) + "\\" +"'";
      startIx = ix+1;
      ix = str.indexOf("'", startIx);
    }
    ret = ret + str.substring(startIx);
    
    return ret;
  }

}
