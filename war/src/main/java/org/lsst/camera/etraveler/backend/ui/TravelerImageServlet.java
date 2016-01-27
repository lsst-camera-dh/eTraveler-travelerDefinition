/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.ui;
import org.lsst.camera.etraveler.backend.ui.DbImporter;
import java.io.*;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;

import javax.servlet.*;
import javax.servlet.http.*;
import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.node.ProcessNode;
import org.lsst.camera.etraveler.backend.node.Traveler;

import org.lsst.camera.etraveler.backend.util.GraphViz;
/**
 * Create circles-and-arrows picture of traveler. 
 * Adapted from org.srs.pipeline.web.servlet.TaskImageServlet
 * @author jrb
 */
public class TravelerImageServlet extends HttpServlet {
  private String dotCommand;
  
  /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException
  {
    if (request.getParameter("db").equals("NONE"))  {
      throw new ServletException("Images not implemented for db=NONE");
    }
    String decodedName = URLDecoder.decode(request.getParameter("name"), "UTF-8");
    Traveler trav;
    try {
      trav = DbImporter.getCachedTraveler(decodedName,
          request.getParameter("version"), request.getParameter("hgroup"),
          request.getParameter("db"));
    } catch (EtravelerException ex) {
      throw new ServletException(ex.getMessage());
    }
    ByteArrayOutputStream bytes = createTravelerImage(trav.getRoot());
    response.setContentType("image/png");
    bytes.writeTo(response.getOutputStream());
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
        return "Creates circles and arrows image of traveler description";
    }
    
    private ByteArrayOutputStream createTravelerImage(ProcessNode traveler) 
        throws ServletException {
      StringWriter writer = new StringWriter();
      GraphViz gv = new GraphViz("dot");
      try {
        traveler.makeDot(writer);
        return gv.getGraph(writer.toString(), GraphViz.Format.PNG);
      }  catch (Exception ex) {
        throw new ServletException("Error creating process image: " + ex.getMessage());
      }      
    }  
}