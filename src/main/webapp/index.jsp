<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="backweb" uri="WEB-INF/BackendWebTags.tld" %>

<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="Window-Target" content="_top" >
        <title>JSP Page</title>
         <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimantName=LSST-CAMERA"
          rel="stylesheet" type="text/css" />
        <link href="css/backendStyle.css" type="text/css" rel="stylesheet" />
    </head>
    <body>
      
        <h1>View and Edit Traveler Definitions</h1>
        <c:set var="ttype_action" value="editTraveler.jsp" />
        <backweb:ListTravelerTypes />
        <%--
        <p> <a href="displayTraveler.jsp">Display a Traveler</a></p>
  
  
    <p>  <a href="editTraveler.jsp">Edit a Traveler</a></p>
    <p>  <a href="addNCR.jsp">Add NCR to a Traveler</a></p>

<p><a href="uploadTraveler.jsp">Ingest a File</p>
--%>

        
</body>
</html>
