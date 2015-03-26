/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node.web;
import org.displaytag.decorator.TableDecorator;
import org.apache.commons.beanutils.DynaBean;
import javax.servlet.jsp.PageContext;
/**
 *
 * @author jrb
 */
public class TtypeDecorator extends org.displaytag.decorator.TableDecorator {
  private String hrefArgs = "";
  String ttype_action = null;
  public TtypeDecorator() {
    super();
    PageContext cxt = super.getPageContext();
    if (cxt != null) {
      if (cxt.getAttribute("ttype_action") != null) {
        ttype_action = (getPageContext().getAttribute("ttype_action")).toString();
      }
    }
  }
 
  private String setHrefArgs()  {
    // compute hrefargs
   
    DynaBean row = (DynaBean) (super.getCurrentRowObject());
    hrefArgs = "?traveler_name=" +
        (row.get("name")).toString()
        + "&traveler_version=" + 
        (row.get("version")).toString()
           + "&traveler_hgroup=" + 
        (row.get("hname")).toString();
    
    return null;  
  }

  public String getViewEdit() {
    setHrefArgs();
    return "<a href='editTraveler.jsp" + hrefArgs + "'>View/Edit</a>";
  }
  public String getAddNCR() {
    setHrefArgs();
    return "<a href='addNCR.jsp" + hrefArgs + "'>Add NCR</a>";
  }
  public String getName() {
    Object curr = super.getCurrentRowObject();
    DynaBean row = (DynaBean) (super.getCurrentRowObject());
    String nameValue = (row.get("name")).toString();
    PageContext cxt = super.getPageContext();
    if ((cxt != null) && (ttype_action == null) ) {
      if (cxt.getAttribute("ttype_action") != null) {
        ttype_action = (getPageContext().getAttribute("ttype_action")).toString();
      }
    }
    if (ttype_action != null) {
      setHrefArgs();
      return "<a href='" + ttype_action + hrefArgs + "'>" + nameValue + "</a>";
    }
    else {
      return nameValue;
    }
  }
}
