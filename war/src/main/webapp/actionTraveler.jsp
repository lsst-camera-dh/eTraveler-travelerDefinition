<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>
<%@taglib prefix="myTree"  uri="WEB-INF/BackendWebTags.tld" %>
<%@taglib prefix="local" tagdir="/WEB-INF/tags/" %>
<!DOCTYPE html>


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Traveler Actions</title>
    <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimentName=LSST-CAMERA" rel="stylesheet" type="text/css">
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
      <style type="text/css">
     
      form.wide {width: 600px; background-color: lightgray;}
      form {background-color: lightgray;}
    
    </style>

   <script type="text/javascript">
  <!--    
      function  clearDoAction() { 
        var frm  = window.parent.document.getElementById('doAction');
  
        if (frm != null)  frm.src = "";
      }
 // -->    
    </script>
    

  </head>
  <body onload="clearDoAction()">

  <myTree:TreeVariables />
  
  <c:choose>





    <c:when test="${param.action == 'view' }">
       <local:displayProcessStep />
    </c:when>
    <c:when test="${param.action == 'NCR' }" >
      <c:if test="${! empty param.leafSelectedPath}" >
        <c:redirect url="NCRForm.jsp?action=NCR&leafSelectedPath=${param.leafSelectedPath}" />
      </c:if>
      <c:if test="${! empty param.folderSelectedPath}" >
        <c:redirect url="NCRForm.jsp?action=NCR&folderSelectedPath=${param.folderSelectedPath}" />
      </c:if>
    </c:when>
    <c:otherwise>
      
    </c:otherwise>
  </c:choose>
  </body>
</html>
