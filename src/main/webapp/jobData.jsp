<%-- 
    Document   : jobData
    Created on : Jan 5, 2015, 1:53:13 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="client" 
   uri="WEB-INF/RestfulClientTags.tld" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
     <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
    <title>Job Data</title>
  </head>
  <body>
   
 
    <c:set var="jobId" value="${param.jobId}" scope="session" />
    <client:DisplayJob />
    
    <c:forEach var="tableName" items="${forDisplay.keySet()}" >
      <br /><br />
      <display:table class="datatable" 
                     uid="${tableName}" name="${forDisplay.get(tableName)}">
        <display:caption><b>${tableName}</b></display:caption>
      </display:table> 
    </c:forEach>
    
  </body>
</html>
