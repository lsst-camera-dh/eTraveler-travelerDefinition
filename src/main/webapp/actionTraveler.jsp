<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Traveler Actions</title>
  </head>
  <body>
  <style type="text/css">
    <!--
      th { font-size: 10pt; font-weight: bold;}
      td { font-size: 10pt;}
      p  { font-size: 10pt;}
      h4 {font-size: 10pt; font-weight: bold;}
   -->
  </style>
    <c:choose> 
    <c:when test="${! empty param.leafSelectedPath}">
      <h4>Selected step:  ${param.leafSelectedPath}</h4>
      <c:set var="leafPath" value="${param.leafSelectedPath}" scope="session" />
       <br /><br />
     <form action="processAction.jsp" id="actionForm"  name="actionForm"
           title="Edit Actions" >
       <table>
         <tr><th>
       <label for="actions">Select Action</label> </th></tr>
         <tr><td>Display step details<input type="radio" id="action" name="action "value="Display"></td></tr>  
         <tr><td>Edit step<input type="radio" id="action" name="action"value="Edit"></td></tr>
         <tr><td>Add leaf child<input type="radio" id="action" name="action" value="LeafChild"></td></tr>
         <tr><td>Add subfolder child<input type="radio" id="action" name="action" value="Subfolder child"></td></tr>
         <tr><td>Add leaf sibling<input type="radio" id="action" name="action" value="LeafSibling"></td></tr>
         <tr><td>Add subfolder sibling<input type="radio" id="action" name="action" value="SubfolderSiblin"></td></tr>
         <tr><td>Remove step<input type="radio" id="action" name="action "value="Remove"></td></tr>
       </table>
       <input type="submit" value="Do it" />
     </form>
    </c:when>
      <c:otherwise> <h4> NO process step selected</h4>
        <p>Select step in navigation pane to left to see edit operations</p></c:otherwise>
    </c:choose>
    
  </body>
</html>
