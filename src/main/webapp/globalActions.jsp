<%-- 
    Document   : globalActions.  Show results of traveler-global actions
    Created on : Jun 11, 2014, 5:53:27 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="org.lsstcorp.etravelerbackendnode.DbImporter" %>
<%@page import="javax.management.Attribute" %>
<%@page import="javax.management.AttributeList" %>

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
        AttributeList lst;   
        String oe="even";        
    %>
    <p>Called with action parameter equal to 
      <%= request.getParameter("action")  %> </p>
    <c:choose>
      <c:when test="${param.action == 'list'}">
      <% lst = imp.listEdited(pageContext);
      if (lst.size() == 0) { %>
    <p> No edited steps found </p>
    <% } else { %>
    <table class="datatable">
      <thead><tr><th>Path</th><th>Edit type</th></tr>
        <% for (int i = 0; i < lst.size(); i++) {
        Attribute a = (Attribute) lst.get(i);
        if (i%2 ==0) oe = "odd";
       else oe = "even";               
       %>
        <tr class="<%= oe %>"><td class="leftAligned"><%= a.getName() %></td>
          <td class="leftAligned"><%= a.getValue() %></td>
         
        </tr>
        <% } %>
    </table>
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
