<%-- 
    Document   : updateStep
    Created on : May 9, 2014, 11:17:33 AM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://etraveler.camera.lsst.org/backend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="local" tagdir="/WEB-INF/tags/" %>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
     <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
    <title>Updated Process Step</title>
  </head>
 
  <body>
    <c:set var="saveReturn" value="${import:saveStep(pageContext)}" />
    <c:choose>
      <c:when test="${! empty saveReturn}">
        <p>${saveReturn}</p>
      </c:when>
      <c:otherwise>
        <h3>Updated Process Step</h3>
        
        <local:displayProcessStep />
   
      </c:otherwise>
    </c:choose>
  </body>
</html>
