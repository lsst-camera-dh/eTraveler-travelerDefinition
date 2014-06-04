<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@page import="java.util.ArrayList" %>
<%@page import="javax.management.AttributeList" %>
<%@page import="javax.management.Attribute" %>
<%@page import="org.lsstcorp.etravelerbackendnode.DbImporter" %>
<%@page import="org.lsstcorp.etravelerbackendnode.Prerequisite" %>
<%@page import="org.lsstcorp.etravelerbackendnode.PrescribedResult" %>

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
      .left {align: left; text-align: left;}
   -->
    </style>
    <% if (request.getParameter("nodePath") != null )     { %>
      <c:set var="nodePath" value="${param.nodePath}" scope="session" />
      <c:set var="leafPath" value="" scope="session" />
      <c:set var="folderPath" value="" scope="session" />
    <% } %>
    <% if ((session.getAttribute("nodePath") != null ) &&
           (request.getParameter("action") != null) )         { %>
    <%-- <h3> <%= session.getAttribute("nodePath") %> </h3>    --%>
    <% DbImporter imp = new DbImporter(); %>
  
    <c:choose>
    <c:when test="${param.action == 'Display' || param.action == 'DisplayOrig'}">
      <%@include file="displayProcessFragment.jsp" %>
    </c:when>
  
    <c:when test="${param.action == 'Edit' }">
      <%@include file="editProcessFragment.jsp" %>    
    </c:when>
   
    <c:otherwise>
      ${import:doAction(pageContext, param.action)}
    </c:otherwise>
    </c:choose>
  
    <% } %>
  </body>
</html>
