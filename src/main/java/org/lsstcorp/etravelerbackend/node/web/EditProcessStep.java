/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.node.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import org.lsstcorp.etravelerbackend.node.DbImporter;
import org.lsstcorp.etravelerbackend.node.Prerequisite;
import org.lsstcorp.etravelerbackend.node.PrescribedResult;
    
/**
 *
 * @author jrb
 */
public class EditProcessStep extends SimpleTagSupport {
  
  private String genId(String nm, int ix) {
    return  nm+ "_" + Integer.toString(ix);
  }
  private String genNode(String nm, int ix) {
    return "document.updateForm." + genId(nm, ix);                                  
  }
  private boolean isNumberSemantics(String sem) {
    return (sem.equals("int") || sem.equals("float"));
  }
         
  public void doTag() throws JspException, IOException {
    PageContext pageContext= (PageContext) getJspContext();
    DbImporter imp = new DbImporter();
    JspWriter  wrt = pageContext.getOut();
    int ix = 0;  
    AttributeList attsj = imp.selectedNodeAttributes(pageContext);
    HashMap<String, String> attsMap = new HashMap<String, String>();
   
    for (int i=0; i < attsj.size(); i++ ) {
      Attribute att = (Attribute) attsj.get(i);
      attsMap.put((att.getName()).toString(), (att.getValue()).toString());
    }
    
    /* Make form  for modifying process attributes and, if present,
     * prerequisites and prescribed result specs */
    
    wrt.println("<form action='updateStep.jsp' id='updateForm' name='updateForm' >");
    wrt.println("<table cellpadding='2'>");
    wrt.println("<tbody>");
    wrt.println("<tr><td class='bold'>Name</td><td>" + (attsMap.get("name")).toString() + "</td></tr>");
    wrt.println("<tr><td class='bold'>Original version</td><td>" 
        + (attsMap.get("version")).toString() +"</td></tr>");
    wrt.println("<tr><td class='bold'>Hardware group</td><td>"
          + (attsMap.get("hardware group")).toString() + "</td></tr>");
    if (attsMap.containsKey("hardware type")) {
      wrt.println("<tr><td class='bold'>Hardware type</td><td>"
          + (attsMap.get("hardware type")).toString() + "</td></tr>");
    }
    wrt.println("<tr><td class='bold'>Child type</td><td>"
        + (attsMap.get("child type")).toString() + "</td></tr>");
    if (attsMap.containsKey("hardware relationship type")) {
      if (!(attsMap.get("hardware relationship type").toString().isEmpty())) {
        wrt.println("<tr><td class='bold'>Hardware relationship type</td><td>"
            + (attsMap.get("hardware relationship type")).toString() + "</td></tr>");
         wrt.println("<tr><td class='bold'>Hardware relationship slot</td><td>"
            + (attsMap.get("hardware relationship slot")).toString() + "</td></tr>");
      }     
    } 
   
    wrt.println("<tr><td class='bold'><label for='description'>Description</label></td>");
    
    wrt.println("<td><input name='description' id='description' size='50'");
    wrt.println("value='" + (attsMap.get("description")).toString() + "' /></td></tr>");
    wrt.println("<tr><td class='bold'><label for='user version string'>User version string</label></td>");
    wrt.println("<td><input name='userVersionString' id='userVersionString' size='20'");
    wrt.println("value='" + (attsMap.get("user version string")).toString() + "' /></td></tr>");
    wrt.println("<tr><td class='bold'><label for='instructionsURL'>Instructions URL</label></td>");
    wrt.println("<td><input name='instructionsURL' id='instructionsURL' size='50'");
    wrt.println("value='" + (attsMap.get("instructions URL")).toString() + "' /></td></tr>");
    if (!imp.selectedIsRoot(pageContext)) {
      wrt.println("<tr><td class='bold'><label for='maxIt'>Max iteration</label></td>");
      wrt.println("<td><input name='maxIt' id='maxIt' size='3'");
      wrt.println("value='" + (attsMap.get("max iterations")).toString() 
          + "' onblur='isPosInt(\"maxIt\",\"1\")' /></td></tr>");
    }
  
    if ((attsMap.containsKey("condition")) ) {
      wrt.println("<tr><td class='bold'><label for='condition'>Condition</label></td>");
      wrt.println("<td><input name='condition' id='condition' size='50'");
      wrt.println("value='" + (attsMap.get("condition")).toString() + "' /></td></tr>");
    }
    wrt.println("</tbody></table>");
    wrt.println("<input type='hidden' name='name' id='name' value='" 
        + (attsMap.get("name")).toString() + "' />");
    wrt.println("<input type='hidden' name='oldVersion' id='oldVersion'"); 
    wrt.println("value='" + (attsMap.get("version")).toString() + "' />");
    wrt.println("<input type='hidden' name='hgroup' id='hgroup' value='" 
        + (attsMap.get("hardware group")).toString() + "' />");
    wrt.println("<input type='hidden' name='childType' id='childType' value='" 
        + (attsMap.get("child type")).toString() + "' />");  
    if (attsMap.containsKey("hardware relationship type")) {
      wrt.println("<input type='hidden' name='hrtype' id='hrtype' value='"
          + attsMap.get("hardware relationship type").toString() + "' />");
      wrt.println("<input type='hidden' name='hrslot' id='hrslot' value='"
          + attsMap.get("hardware relationship slot").toString() + "' />");
    }
    
    
    
    String nodestring;
    String idstring;
          
    ArrayList<Prerequisite> prereqs =  imp.getPrerequisites(pageContext);
 
    if (prereqs != null) {
      outputPrereqs(prereqs, wrt);
    }
    
    ArrayList<PrescribedResult> results =  imp.getResults(pageContext);
 
    if (results != null) {
      outputResults(results, wrt);
    }
    if  (imp.selectedClone(pageContext)) { 
      wrt.println("<p><b>There are multiple instances of selected step</b></p>");
      wrt.println("<p><input type='submit' name='save' value='Save edit, all instances' />");
      /*  Not supported for the time being
      if (!imp.selectedHasChildren(pageContext)) {  // give option to save all or just this node
        wrt.println("<input type='submit' name='save' value='Save edit, this instance' />");
      }
      */
    } else {
      wrt.println("<p><input type='submit' name='save' value='Save edit' />");
    }

    //wrt.println("<p><input type='submit' value='Save edit' />");
    wrt.println("<input type='reset' value='Reset form' />");
    wrt.println("</p></form>");
  }
    
  private void outputPrereqs(ArrayList<Prerequisite> prereqs, JspWriter wrt)
  throws IOException {
    boolean haveProcessStep = false;
    for (Prerequisite p: prereqs) {
      if (p.getType().equals("PROCESS_STEP")) { haveProcessStep = true;}
    } 
    int ix=0;
    wrt.println("<fieldset>");
    wrt.println(" <legend>Prerequisites</legend>");
    wrt.println(" <table class='datatable' cellpadding='2' border='0'>");
    wrt.println("<thead>");
    wrt.println("<tr><th bgcolor='red'>DELETE</th><th align='left'>Name</th>");
    wrt.println("<th align='left'>Type</th><th align='left'>Count</th>");
    wrt.println("<th align='left'>Descrip</th>");
    if (haveProcessStep) {
      wrt.println(" <th align='left'>Version</th><th align='left'>User_version</th></tr>");
    }
    wrt.println("</thead><tbody>");
    String rowclass="odd";
    String warning="grey";
    for (Prerequisite prereq: prereqs) {
       String idString = genId("count", ix);
       wrt.println("<tr class='" + rowclass + "'>");
       wrt.println("<td bgcolor='" + warning + "'>");
       wrt.println("<input type='checkbox' name='" + genId("removePrereq", ix) 
           + "' id='" + genId("remove", ix) + "' /></td>"); 
       wrt.println("<td align='left'>" + prereq.getName() + "</td>");
       wrt.println("<td align='left'>" + prereq.getType() + "</td>");
       wrt.println("<td align='right'><input type='number' min='1' step='1'");          
       wrt.println("max='999' name='" +  genId("count", ix) +"' id='" 
           + genId("count",ix) + "' value='"
           + prereq.getQuantity() + "' onblur='isPosInt(\"" + idString 
           + "\",\"1\")' /></td>  <td align='left'>");
                          
       wrt.println("<input type='text'  name='" + genId("prereqDescrip",ix)  
           + "'  id='" + genId("prereqDescrip", ix)
           + "' value='" + prereq.getDescription() + "' /></td>");
       if (haveProcessStep) { 
         wrt.println("<td>" + prereq.getVersion() + "</td><td>");
         String uv = prereq.getUserVersionString();
         if (uv == null) { uv = "";}
         wrt.println("<input type='text' name='" + genId("userVersion", ix)
             + "' id='" +  genId("userVersion", ix)
             + "'  size='5' value='" + uv + "'/></td></tr>");
       }
       ix++;  
       if (rowclass.equals("even")) {
         rowclass="odd";  warning="grey"; }
       else { rowclass="even"; warning="black"; }
    }
    wrt.println("<tbody></table></fieldset>");
  }
      
  private void outputResults(ArrayList<PrescribedResult> results, JspWriter wrt)
      throws IOException {
    boolean haveNumeric = false;
    if (results != null) {
      for (PrescribedResult r: results) {
        String sem = r.getSemantics();
        if ((sem.equals("int")) || (sem.equals("float")) ) {haveNumeric = true;}
      }
    }
   
    // wrt.println("<h3> Write prescribed results here</h3>");
    wrt.println("<fieldset><legend>Required operator inputs</legend>");
    wrt.println("<table cellpadding='2'  class='datatable' border='0'>"); 
    wrt.println("<thead>");
    wrt.println("<tr><th bgcolor='red'>DELETE</th><th align='left'>Label</th>");
    wrt.println("<th align='left'>Type</th>  <th align='left'>Descrip</th>");
    if (haveNumeric) { 
      wrt.println("<th align='left'>Units</th>");
      wrt.println("<th align='left'>Min</th><th align='left'>Max</th></tr>");
    } 
    wrt.println("</thead><tbody>");         
   
    String rowclass="odd";
    String warning="grey"; 
    String empty = "";
    int  ix = 0;                                          
    for (PrescribedResult result: results) {               
      wrt.println("<tr class='" + rowclass + "'>");
      wrt.println("<td bgcolor='" + warning + "'><input type='checkbox' name='" 
          + genId("removeResult", ix) + "' id='"
          + genId("removeResult", ix) +"' /></td>"); 
      wrt.println("<td class='left'>" + result.getLabel() + "</td>");
      wrt.println("<td class='left'>" + result.getSemantics() + "</td>");
      wrt.println("<td><input type='text' name='" 
          + genId("resultDescrip", ix) + "' id='" + genId("resultDescrip", ix)
          + "' value='" + result.getDescription() + "' /></td>");
      if (haveNumeric) {
        if (isNumberSemantics(result.getSemantics())) {
          String idstring = genId("min", ix);
          wrt.println("<td class='left'>");
          wrt.println("<input type='text' name='" + genId("units", ix) 
              + "' id='" + genId("units", ix) + "' size='6' value='"
              + result.getUnits() + "' /></td>");
          wrt.println("<td><input type='text' name='" + idstring 
              +  "' id='" + idstring + "' size='4' value='"
              + result.getMinValue() + "' onblur='isNumber(\""
              + idstring + "\",\"" + result.getSemantics() + "\",\""
              + empty + "\")' /></td>");
          idstring = genId("max", ix);
          wrt.println("<td><input type='text' name='" + idstring 
              + "' id='" + idstring +"' size='4' value='"
              + result.getMaxValue() +"'" 
              + "onblur='isNumber(\"" + idstring + "\",\"" 
              + result.getSemantics() + "\",\"" + empty
              + "\")' /></td>");
        } else { 
          wrt.println("<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>");
        }
      } 
      wrt.println("</tr>");
      if (rowclass.equals("even")) {
        rowclass="odd";  warning="grey"; }
      else { rowclass="even"; warning="black"; }            
      ix++;
    }
    wrt.println("<tbody></table></fieldset>");
  } 
                  
}
