package org.lsst.camera.etraveler.backend.ui;
import java.io.*;
import java.net.URLDecoder;
// import javax.naming.InitialContext;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import org.lsst.camera.etraveler.backend.ui.DbImporter;
import org.lsst.camera.etraveler.backend.node.Traveler;
/**
 *  Allow user to download generated yaml
 * @author jrb
 */
public class DownloadYamlServlet extends HttpServlet {
  
  /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   */

  protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException
  {
    // String db = request.getParameter("tdb");
    String travelerKey = request.getParameter("key");
    String ostyle = request.getParameter("ostyle");
    String subtype="";
    if (ostyle.contains("canonical")) {subtype="_canonical";}
    else if (ostyle.contains("verbose")) subtype="_verbose";
    ConcurrentHashMap<String, Traveler> tmap;
    String attributeName = DbImporter.getTravelerMapAttribute();
    tmap = (ConcurrentHashMap<String, Traveler>) 
      request.getSession().getAttribute(attributeName);
    if (tmap == null)  {
      tmap = new ConcurrentHashMap<String, Traveler> ();
      request.getSession().setAttribute(attributeName, tmap);
    }
    Traveler trav = DbImporter.getTravelerFromKey(travelerKey, tmap);
    String fname = travelerKey.replace('@', '_') + subtype + ".yaml";
   
    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    response.setHeader("Content-Disposition", 
                       "attachment; filename=\"" + fname + "\"");
    trav.outputYaml(out, ostyle.equals("Yaml-verbose"));
    out.close();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo()
    {
        return "Creates yaml, prompts user for download";
    }
    
}
