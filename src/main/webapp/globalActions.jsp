<%-- 
    Document   : globalActions.  Show results of traveler-global actions
    Created on : Jun 11, 2014, 5:53:27 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="import" 
          uri="http://etraveler.camera.lsst.org/backend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@page import="org.lsst.camera.etraveler.backend.node.DbImporter" %>
<%@page import="org.lsst.camera.etraveler.backend.node.EditedTreeNode" %>
<%@page import="javax.management.Attribute" %>
<%@page import="javax.management.AttributeList" %>
<%@page import="java.util.ArrayList"%>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Global traveler actions</title>
    <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimantName=LSST-CAMERA"
          rel="stylesheet" type="text/css" />
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
  </head>
  <body>
    <%! DbImporter imp; 
        ArrayList<EditedTreeNode> lst;   
        String oe="even";        
    %>
    
    <%--
    <p>Called with action parameter equal to 
      <%= request.getParameter("action")  %> </p>
    --%>
    <c:choose>
      <c:when test="${param.action == 'list'}">
      <% lst = imp.listEdited(pageContext);
      if (lst.size() == 0) { %>
    <p> No edited steps found </p>
    <% } else { %>
    <form action="updateEditedList.jsp" >
    <display:table class="datatable" name="${import:listEdited(pageContext)}"
                   id="row">
      <display:column property="path" title="Path" style="text-align:left" />
      <display:column property="editType" title="Edit type" 
                      style="text-align:left" />
      <display:column  title="Undo" style="text-align:left">
        <button type="submit" title="${row.undo}" name="undo" value="${row.path}" >
        <c:out value="${row.undo}" />
        </button>
      </display:column> />
    </display:table>
    </form>
     
        <% } %>
     
      </c:when>
      <c:when test="${param.action == 'ingest'}">
        <% imp.ingestEdited(pageContext); %>
      </c:when>
      <c:when test="${param.action == 'revert'}">
        <% imp.revertEdited(pageContext); %>
      </c:when>
    <c:otherwise>
      <p>Unrecognized traveler action ${param.action} </p>
    </c:otherwise>
    </c:choose>
  </body>
</html>
