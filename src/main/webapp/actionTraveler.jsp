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
    <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimentName=LSST-CAMERA" rel="stylesheet" type="text/css">
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
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


  <c:set var="nodePath" scope="session" />
  <c:set var="isLeaf" scope="session" />
 
    <c:choose> 
    <c:when test="${! empty param.leafSelectedPath}">   
      <h4>Selected step:  ${param.leafSelectedPath}</h4>
      <c:set var="leafPath" value="${param.leafSelectedPath}" scope="session" />
      <c:set var="folderPath" scope="session" />
      <c:set var="nodePath" value="${param.leafSelectedPath}" scope="session" />
      <c:set var="isLeaf" value="1" scope="session" />
  
       <p>To unselect all steps use the browser refresh button</p>
      
     <form action="processAction.jsp" id="actionForm"  name="actionForm" 
           target="doAction"  title="Edit Actions" onreset="clearDoAction()">
       <fieldset><legend>Select action</legend>
       <table>
         <tr><td>Display step details<input type="radio" id="action" 
                                            name="action" value="Display" /></td></tr>  
         <tr><td>Edit step<input type="radio" id="action" name="action"
                                 value="Edit" /></td></tr>
         <tr><td>Add leaf sibling<input type="radio" id="action" name="action" 
                                        value=" leafSibling"/></td></tr>
         <tr><td>Add subfolder sibling<input type="radio" id="action" name="action" 
                                             value="subfolderSibling" /></td></tr>
         <tr><td class="warn" ><b>Remove step</b><input type="radio" id="action" name="action"
                                   value="remove" /></td></tr>
     
       </table>
       </fieldset>
       <p>
       <input type="submit" value="Do it" /> <input type="reset" value="Reset" />
       </p>
     </form>
    </c:when>
     <c:when test="${! empty param.folderSelectedPath}">
         <h4>Selected step:  ${param.folderSelectedPath}</h4>
      <c:set var="folderPath" value="${param.folderSelectedPath}" scope="session"/>
      <c:set var="leafPath" scope="session" />
      <c:set var="nodePath"  value="${param.folderSelectedPath}" scope="session"/>
      <c:set var="isLeaf" scope="session" />
        <p>To unselect all steps use the browser refresh button</p>
      
     <form action="processAction.jsp" id="actionForm"  name="actionForm" 
           target="doAction"  title="Edit Actions" onreset="clearDoAction()" >
       <fieldset><legend>Select action</legend>
       <table>
     <%--    <tr><th>
       <label for="actions">Select Action</label> </th></tr> --%>
         <tr><td>Display step details<input type="radio" id="action" 
                                            name="action"value="Display" /></td></tr>  
         <tr><td>Edit step<input type="radio" id="action" name="action"
                                 value="Edit" /></td></tr>
   
         <tr><td>Add leaf child<input type="radio" id="action" name="action" 
                                      value="leafChild"/></td></tr>
         <tr><td>Add subfolder child<input type="radio" id="action" name="action" 
                                           value="subfolderChild"/></td></tr>
         <tr><td>Add leaf sibling<input type="radio" id="action" name="action" 
                                        value="leafSibling" /></td></tr>
         <tr><td>Add subfolder sibling<input type="radio" id="action" name="action" 
                                             value="subfolderSibling" /></td></tr>
         <tr><td class="seriousWarn"><b>Remove step</b> (and all substeps)<input type="radio" id="action" 
                                        name="action "value="remove" /></td></tr>
       
       </table>
       </fieldset>
         <p>
       <input type="submit" value="Do it" /> <input type="reset" value="Reset" />
         </p>
     </form>
      
     </c:when>
      <c:otherwise> <h4> NO process step selected</h4>
        <p>Select step in navigation pane to left to see edit operations</p>
      </c:otherwise>
    </c:choose>

  </body>
</html>
