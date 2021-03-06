package org.lsst.camera.etraveler.backend.transport;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import org.lsst.camera.etraveler.backend.db.DbConnection;
import org.lsst.camera.etraveler.backend.db.MysqlDbConnection;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.exceptions.UnknownDbId;
import org.lsst.camera.etraveler.backend.transport.HardwareType;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;
import org.lsst.camera.etraveler.backend.ui.DbWebUtil;
    
/**
 *  Accept form input specifying hardware type and db to transport
 *  to, then invoke services from transport.HardwareType to do it
 * 
 * @author jrb
 */
public class TransportHardwareTypeTag extends SimpleTagSupport {
  private String selected;
  private String destDb;
  public void setSelected(String sel) {
    this.selected=sel;
  }
  public void setDestDb(String dest) {
    this.destDb=dest;
  }
  public void doTag() throws JspException, IOException {
    PageContext cxt= (PageContext) getJspContext();
    JspWriter  wrt = cxt.getOut();

    /* From context get
         "ambient" connection parameters
         name of Hardware Type to be transported
         destination dataSourceMode
     */
    String dbType = ModeSwitcherFilter.getVariable(cxt.getSession(),
                                                   "dataSourceMode");
    //String datasource = ModeSwitcherFilter.getVariable(pageContext.getSession(),
    //                                                   "etravelerDb");
    DbConnection readConn=null;
    DbConnection writeConn=null;
    try {
      readConn = DbWebUtil.makeConnection(cxt, true);
    } catch (EtravelerException ex) {
      return;
    }
        //new MysqlDbConnection();
    // readConn.setSourceDb(dbType);
    //readConn.openTomcat(datasource);
    HardwareType h = new HardwareType();
    try {
      h.importFrom(readConn, Integer.parseInt(selected));
    } catch (UnknownDbId ex) {
      wrt.println("<p><b>Hardware type db read failed with exception '"
          + ex.getMessage()+"'</b></p>");
      readConn.close();
      return;
    }  catch (SQLException ex)  {
      wrt.println("<p><b>Hardware type db read failed with exception '"
          + ex.getMessage()+"'</b></p>");
      readConn.close();
      return;
    }
    try {
      writeConn = DbWebUtil.makeConnection(cxt, destDb, false);
    } catch (EtravelerException ex) {
      return;
    }
    
    int newId=0;
    try {
      newId=h.exportTo(writeConn, cxt.getSession().getAttribute("userName").toString());
    } catch (SQLException ex) {
            wrt.println("<p><b>Hardware type db write failed with SQL exception '"
          + ex.getMessage()+"'</b></p>");
      writeConn.close();
    } catch (EtravelerException ex) {
      wrt.println("<p><b>Hardware type db write failed with Etraveler exception '"
          + ex.getMessage()+"'</b></p>");
      writeConn.close();
    }
    writeConn.close();
    
    wrt.println("<p>Wrote new hardware type " + h.getName() +
        " with id="+Integer.toString(newId)
        + " to database " + destDb + "</p>");   
  }                   
}
