<%-- 
    Document   : updateEditedList
    Created on : Jun 17, 2014, 2:00:02 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="import" 
   uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Update Edited ProcessTreeNode List</title>
    <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimantName=LSST-CAMERA"
          rel="stylesheet" type="text/css" />
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
  </head>
  <body>
      ${import:adjustList(pageContext, param.undo)}
      <c:redirect url="globalActions.jsp?action=list" />
  </body>
</html>
