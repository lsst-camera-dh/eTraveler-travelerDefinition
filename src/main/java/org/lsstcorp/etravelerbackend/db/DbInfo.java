/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.db;

/**
 *
 * @author jrb
 */
public class DbInfo {
    public DbInfo() { 
     host ="mysql-dev01:3307";
     user = "";
     pwd = "";
     dbname = "";
    }
    public String host;
    public String user;
    public String pwd;
    public String dbname;
    public void establish() {
      // Prompt for properties file path (use default if none given?)
      // ..or if that fails use embedded defaults?
      if (dbname.equals("")) {dbname = "rd_lsst_camt";}
      if (user.equals("")) {user = dbname + "_ro";}
      if (pwd.equals(""))  {pwd = "2chmu#2do";}
    }
   
  
}
