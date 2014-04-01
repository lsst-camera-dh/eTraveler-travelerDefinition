<%-- 
    Document   : DisplayProcess
    Created on : Mar 28, 2014, 2:56:46 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="import" 
   uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Display Process</title>
  </head>
  <body>
    <h2>Process ${param.process}, version ${param.version} </h2>
    <p>If all is well, Traveler name and version should appear above</p>
  </body>
</html>
