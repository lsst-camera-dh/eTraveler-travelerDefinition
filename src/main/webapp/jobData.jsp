<%-- 
    Document   : jobData
    Created on : Jan 5, 2015, 1:53:13 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="client" 
   uri="WEB-INF/RestfulClientTags.tld" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Job Data</title>
  </head>
  <body>
   
    <%--  <client:DisplayJob jobId='${jobId}'/>  --%>
    <c:set var="jobId" value="${param.jobId}" scope="session" />
    <client:DisplayJob />
  </body>
</html>
