<%-- 
    Document   : uploadYaml
    Created on : Nov 27, 2013, 1:32:12 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="yamltodb" 
   uri="http://lsstcorp.org/etravelerbackend/WriteToDb" %>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Ingest results</title>
  </head>
  <body>
    <h2>Results of ${param.fileAction}</h2>
    <%-- Save parameters in session variables --%>
    <c:set var="fileContents" value="${param.importYamlFile}" scope="session" />
    <c:set var="action" value="${param.fileAction}" scope="session" />
    <c:set var="reason" value="" scope="session" />
    <c:set var="owner" value="" scope="session" />
    <c:if test="${param.fileAction == 'Import' }">
      <c:set var="reason" value="${param.reason}" scope="session" />
      <c:set var="owner" value="${param.owner}" scope="session" />
    </c:if>
     ${yamltodb:ingest(pageContext)}
  </body>
</html>
