<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Traveler Action in Progress</title>
  </head>
  <body>
    <p> Try to write session attribute leafPath:   [ 
      <%=  session.getAttribute("leafPath")    %>
      ] </p>
    <p> Try to write action: [
      <%=   request.getParameter("action")        %>
     ] </p>
    <c:if test="{! empty session.getAttribute(leafPath)}">
      <p> Path for selected leaf is ${session.getAttribute(leafPath)} </p>
      <p> Selected action is ${param.action} </p>
    </c:if>
    
    
    <c:if test="${! empty param.leafSelectedPath}">
      Selected Node: <b>${param.leafSelectedPath}</b><br>
      <c:set var="leafPath" value="${param.leafSelectedPath}" scope="session" />
    </c:if>

    
  </body>
</html>
