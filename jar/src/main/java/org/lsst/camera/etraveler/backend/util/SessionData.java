/**
 A little class to store information normally obtained from the HttpSession 
 */

package org.lsst.camera.etraveler.backend.util;

import javax.servlet.http.HttpServletRequest;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;



public class SessionData {
  private String m_dbType=null;
  private String m_datasource=null;
  private String m_fileStore=null;
  private boolean m_localhost=false;
  private boolean m_standalone=false;

  public SessionData() {}
  public SessionData(String dbType, String datasource, String fileStore,
                     boolean localhost, boolean standalone) {
    m_dbType = dbType;
    m_datasource = datasource;
    m_fileStore = fileStore;
    m_localhost = localhost;
    m_standalone = standalone;
  }

  public SessionData(HttpServletRequest req) {
    m_dbType = ModeSwitcherFilter.getVariable(req.getSession(), 
                                              "dataSourceMode");
    m_datasource = ModeSwitcherFilter.getVariable(req.getSession(), 
                                              "etravelerDb");

    m_fileStore = ModeSwitcherFilter.getVariable(req.getSession(), 
                                              "etravelerFileStore");

    String url = (req.getRequestURL()).toString();
    m_localhost = (url.contains("localhost"));
    m_standalone = false;
  }

  public String getDbType() {return m_dbType;}
  public String getDatasource() { return m_datasource;}
  public String getFileStore() {return m_fileStore;}
  public boolean getLocalhost() {return m_localhost;}
  public boolean getStandalone() { return m_standalone;}
}
