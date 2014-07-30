<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>
<%@taglib prefix="myTree"  uri="WEB-INF/TreeTags.tld" %>
<%@taglib prefix="local" tagdir="/WEB-INF/tags/" %>
<!DOCTYPE html>


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

  <myTree:TreeVariables />
  
  <c:choose>
    <c:when test="${param.action == 'edit' }">

  <h4>Selected step:  ${sessionScope.nodePath} </h4>
   <p>To unselect all steps use the browser refresh button</p>
    <c:choose> 
      <c:when test="${! empty sessionScope.leafPath }">   
      
     <form action="processAction.jsp" id="actionForm"  name="actionForm" 
           target="doAction"  title="Edit Actions" onreset="clearDoAction()">
       <fieldset>
         <legend>Select per-step action</legend>
       <table>
         <tr><td><input type="submit" id="action" 
                                            name="action" value="Display" /></td></tr>  
         <tr><td><input type="submit" id="action" name="action"
                                 value="Edit" /></td></tr>
         <tr><td><input type="submit" id="action" name="action" 
                                        value=" Add leaf sibling (NYI)" disabled /></td></tr>
         <tr><td><input type="submit" id="action" name="action" 
                                             value="Add folder sibling" disabled /></td></tr>
         <tr><td><b><input class="warn" type="submit" id="action" name="action"
                                   value="Remove (NYI)" disabled /></b></td></tr>
     
       </table>
       </fieldset>
     
     </form>
    </c:when>
     <c:when test="${! empty param.folderSelectedPath}" >
      
     <form action="processAction.jsp" id="actionForm"  name="actionForm" 
           target="doAction"  title="Edit Actions" onreset="clearDoAction()" >
    
       <fieldset>
         <legend>Select per-step action</legend>
       <table>
     
         <tr><td><input type="submit" id="action" 
                                            name="action"value="Display" /></td></tr>  
         <tr><td><input type="submit" id="action" name="action"
                                 value="Edit" /></td></tr>
   
         <tr><td><input type="submit" id="action" name="action" 
                                      value="Add leaf child (NYI)" disabled /></td></tr>
         <tr><td><input type="submit" id="action" name="action" 
                                           value="Add folder child (NYI)" disabled /></td></tr>
         <tr><td><input type="submit" id="action" name="action" 
                                        value="Add leaf sibling (NYI)" disabled  /></td></tr>
         <tr><td><input type="submit" id="action" name="action" 
                                             value="Add folder sibling (NYI)" disabled /></td></tr>
         <tr><td >
             <b> <input class="seriousWarn" type="submit" id="action" 
                name="action "value="Remove step &amp; substeps! (NYI)" disabled /></b></td></tr>
       
       </table>
       </fieldset>
  
     </form>
      
     </c:when>
      <c:otherwise> <h4> NO process step selected</h4>
        <p>Select step in navigation pane to left to see edit operations</p>
      </c:otherwise>
    </c:choose>
        
    <h4>Global Traveler Actions</h4>
    <table><tr><td>
    <a href="globalActions.jsp?action=list" target="doAction">List modified steps</a>
        </td></tr> 
  <%--    <tr><td>
    <a href="globalActions.jsp?action=revert" target="doAction">Undo step modifications</a><br />
      </td></tr>   --%>
      <tr><td>
    <a href="globalActions.jsp?action=ingest" target="doAction">Ingest modified traveler</a></p>
      </td></tr>  </table>
    </c:when>
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
