<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@page import="javax.management.AttributeList" %>
<%@page import="javax.management.Attribute" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Traveler Action in Progress</title>
  </head>
  <body>
   <style type="text/css">
    <!--
      th { font-size: 10pt; font-weight: bold;}
      td { font-size: 10pt;}
      p  { font-size: 10pt;}
      h4 {font-size: 10pt; font-weight: bold;}
      .bold {font-weight: bold}
   -->
    </style>
    
    <% if ((session.getAttribute("nodePath") != null ) &&
           (request.getParameter("action") != null) )         { %>
    <%-- <h3> <%= session.getAttribute("nodePath") %> </h3>    --%>
  
    <c:choose>
    <c:when test="${param.action == 'Display' }">
         <display:table name="${import:selectedNodeAttributes(pageContext)}" 
                                class="datatable" >
           <display:column property="name" title="Attribute" 
                           headerClass="sortable" style="text-align:left" />
           <display:column property="value" />
         </display:table>
    </c:when>
    <c:when test="${param.action == 'Edit' }">
    
      <c:set var="atts" value="${import:selectedNodeAttributes(pageContext)}" />
      <% 
      AttributeList attsj = (AttributeList) pageContext.getAttribute("atts");
      String name="", oldVersion="", htype="", childType="", maxIt="", nSub="";
      String nPrereq="", nResult="";
      String description="", instructionsURL="", actionMask="";
      String userVersionString=null;
      String hrtype=null;
      
      for (int i = 0; i < attsj.size(); i++) {
        Attribute att = (Attribute) attsj.get(i);
        String val = att.getValue().toString();
        switch (att.getName()) {
          case "name": 
            name = val; 
            break;
          case "version" :
            oldVersion = val; break;
          case "hardware type":
            htype = val; break;
          case "child type":
            childType = val; break;
          case "description":
            description = val; break;
          case "instructions URL":
            instructionsURL=val; break;
          case "max iterations":
            maxIt = val; break;
          case "traveler action mask":
            actionMask = val; break;
          case "# substeps":
            nSub = val; break;
          case "# prerequisites":
            nPrereq = val; break;
         case "# solicited results":
            nResult = val; break;
         case "user version string":
            userVersionString = val; break;
         case "hardware relationship type":
            hrtype = val; break;                                                  
        }
      }    %>
      
   
      <form action="updateStep.jsp" id="updateForm" name="updateForm" 
            target="doAction" >
        <table cellpadding="2">
          <tbody>
          <tr><td class="bold"">Name</td><td><%= name %></td></tr>
          <tr><td class="bold">Original version</td><td><%= oldVersion %></td></tr>
          <tr><td class="bold">Hardware type</td><td><%= htype %></td></tr>
          <tr><td class="bold">Child type</td><td><%= childType %></td></tr>
          <tr><td class="bold"><label for="description">Description</label></td>
            <td><input name="description" id="description" 
                       value="<%= description %>" /></td></tr>
           <tr><td class="bold"><label for="instructionsURL">Instructions URL</label></td>
            <td><input name="instructionsURL" id="instructionsURL" 
                       value="<%= instructionsURL %>" /></td></tr>       
           <tr><td class="bold"><label for="maxIt">Max iteration</label></td>
            <td><input name="maxIt" id="maxIt" 
                       value="<%= maxIt %>" /></td></tr>
          </tbody>
        </table>
        <input type="hidden" name="name" id="name" value="<%= name %>" />
        <input type="hidden" name="oldVersion" id="oldVersion" 
               value="<%= oldVersion %>" />
        <input type="hidden" name="htype" id="htype" value="<%= htype %>" />
        <input type="hidden" name="childType" id="childType" 
               value="<%= childType %>" />
        <p>
        <input type="submit" value="Save edit" />
        <input type="reset"  value="Reset form" />
        </p>
      </form>
    </c:when>
   
    <c:otherwise>
      ${import:doAction(pageContext, param.action)}
    </c:otherwise>
    </c:choose>
  
    <% } %>
  </body>
</html>
