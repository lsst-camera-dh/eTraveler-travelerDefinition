/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.rest.client;
import org.displaytag.decorator.TableDecorator;
import org.apache.commons.beanutils.DynaBean;
import javax.servlet.jsp.PageContext;

/**
 *
 * @author jrb
 */
public class ActivityDecorator extends org.displaytag.decorator.TableDecorator {
  public ActivityDecorator() {
    super();
  }
  
  public String getId() {
    //String href = "http://localhost:8080/eTravelerRestful/rest/harnessOutput/";
    Object curr = super.getCurrentRowObject();
    DynaBean row = (DynaBean) (super.getCurrentRowObject());
    String idValue = (row.get("id")).toString();
    
   // return "<client:DisplayJob jobId='idValue' />";
    return "<a href='jobData.jsp?jobId=" + idValue + "'> " + idValue + "</a>";    
        //"<a href='" + href + idValue + "'?db='Raw'>" + idValue + "</a>";
  }
  
}
