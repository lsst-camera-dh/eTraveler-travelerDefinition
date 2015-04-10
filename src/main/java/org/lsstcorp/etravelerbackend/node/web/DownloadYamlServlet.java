package org.lsstcorp.etravelerbackend.node.web;
import java.io.*;
import java.net.URLDecoder;
// import javax.naming.InitialContext;

import javax.servlet.*;
import javax.servlet.http.*;

import org.lsstcorp.etravelerbackend.node.DbImporter;
import org.lsstcorp.etravelerbackend.node.Traveler;
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
  
    Traveler trav = DbImporter.getTravelerFromKey(travelerKey);
    String fname = travelerKey.replace('@', '_') + ".yaml";
   
    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    response.setHeader("Content-Disposition", 
                       "attachment; filename=\"" + fname + "\"");
    DbImporter.outputYaml(out, trav, true);
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