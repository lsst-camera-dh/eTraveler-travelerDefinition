<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>
<%@taglib prefix="backweb"  uri="WEB-INF/BackendWebTags.tld" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@taglib prefix="local" tagdir="/WEB-INF/tags/" %>
<%@page import="java.util.ArrayList" %>
<%@page import="javax.management.AttributeList" %>
<%@page import="javax.management.Attribute" %>
<%@page import="org.lsstcorp.etravelerbackendnode.DbImporter" %>
<%@page import="org.lsstcorp.etravelerbackendnode.Prerequisite" %>
<%@page import="org.lsstcorp.etravelerbackendnode.PrescribedResult" %>

<!DOCTYPE html>


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Traveler Action in Progress</title>
    <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimantName=${appVariables.experiment}"
          rel="stylesheet" type="text/css" />
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
  </head>
  <body>
  
    <% if (request.getParameter("nodePath") != null )     { %>
      <c:set var="nodePath" value="${param.nodePath}" scope="session" />
      <c:set var="leafPath" value="" scope="session" />
      <c:set var="folderPath" value="" scope="session" />
    <% } %>
    <% if ((session.getAttribute("nodePath") != null ) &&
           (request.getParameter("action") != null) )         { %>
   
  
    <c:choose>
    <c:when test="${param.action=='Display' || param.action=='DisplayOrig' || param.action=='view'}">
      <local:displayProcessStep />
    </c:when>
  
    <c:when test="${param.action == 'Edit' }">
      <script type="text/javascript">
  <!--
      function isInt(id, resetValue) {
        node = document.getElementById(id);
        if ( (node.value == null) ||
          (Math.floor(node.value) != node.value) ) {
          alert("Not an integer!");
          node.value = resetValue;
          window.setTimeout("node.focus()", 100);
         } else return true;}
      function isPosInt(id, resetValue) {
        node = document.getElementById(id);
        if  (!(Math.floor(node.value) == node.value) ) {
          node.value = resetValue;
          alert("Value must be an integer!");
          window.setTimeout("node.focus()", 100);
        } else if (node.value <= 0) {
          node.value = resetValue;
          alert("Value must be a positive integer!");
          window.setTimeout("node.focus()", 100);
        } else return true;
      }
      function isNumber(id, type, resetValue) {
        if (type == "int") {
         return isInt(id, resetValue);
        }
        node = document.getElementById(id);
        if ( (node.value == null) ||           
          (Math.abs(node.value) != Math.abs(node.value) ) ) {
          alert("Supplied value is not a number!");
          node.value = resetValue;
          window.setTimeout("node.focus(), 100");
        } 
        else {
          return true;
        }
      }    
  // -->    
</script>
     <%--  <%@include file="editProcessFragment.jspf" %> --%>
     <backweb:EditProcessStep />
    </c:when>
   
    <c:otherwise>
      ${import:doAction(pageContext, param.action)}
    </c:otherwise>
    </c:choose>
  
    <% } %>
  </body>
</html>
