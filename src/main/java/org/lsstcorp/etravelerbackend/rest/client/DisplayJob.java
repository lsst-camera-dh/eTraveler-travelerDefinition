/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter;
import org.lsst.portalDataModel.rest.model.TableData;
import org.lsst.portalDataModel.rest.model.ColumnData;



/**
 *  Given activity id, fetch harnessed job data and display
 * @author jrb
 */
public class DisplayJob extends SimpleTagSupport {
  public void doTag() {
    PageContext page= (PageContext) getJspContext();
    String dbType = ModeSwitcherFilter.getVariable(page.getSession(),
        "dataSourceMode");
    JspWriter wrt = page.getOut();
    String jobId = (String) page.getAttribute("jobId", PageContext.SESSION_SCOPE);
    String targetPrefix = "http://" + page.getRequest().getServerName() + ":" 
        + page.getRequest().getServerPort();
    // String targetUrl = "http://localhost:8080/eTravelerRestful/rest/harnessOutput/" + jobId
    String targetUrl = targetPrefix + "/eTravelerRestful/rest/harnessOutput/" + jobId
        + "?db=" + dbType;
    try {
      Client client = ClientBuilder.newBuilder()
          .register(MultiPartFeature.class)
          .register(new JacksonJsonProvider(new ObjectMapper() ))
          .build();
      WebTarget target = client.target(targetUrl);
      
      List<TableData> tables =
          target.request()
          .header("accept", "application/json")
          .get( new GenericType<List<TableData>>() {} );
      
      wrt.println("<h1>Data for Activity " + jobId + "</h1>");
      HashMap<String, ArrayList<Map<String, String> > >  forDisplay  =
          new HashMap<String, ArrayList<Map<String,String>>>();  
     
      for (TableData table : tables) {
        ArrayList<Map<String, String>> ourRows = table.getRows();
        forDisplay.put(table.getTitle(), ourRows);
        // outputTable(table, wrt);  
      }   
      
      // Set session variable so jsp can pull this back out 
      getJspContext().setAttribute("forDisplay", forDisplay, PageContext.SESSION_SCOPE);
     
    } catch (Exception ex)  {
      System.out.println(ex.getMessage());
    }
  }
  private void outputTable(TableData table, JspWriter wrt) throws Exception {
    
     wrt.println("<p>Found table named " + table.getTitle() + "</p>");
  }
 
}
  
