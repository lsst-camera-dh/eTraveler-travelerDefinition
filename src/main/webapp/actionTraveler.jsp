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
  <p>Selected node path using session.getAttribute:  <%= session.getAttribute("nodePath") %></p>
  <p>or try using sessionScope:  ${sessionScope.nodePath} </p>
    <c:choose> 
      <c:when test="${! empty sessionScope.leafPath }">   
      <h4>Selected step:  ${param.leafSelectedPath}</h4>
  
       <p>To unselect all steps use the browser refresh button</p>
      
     <form action="processAction.jsp" id="actionForm"  name="actionForm" 
           target="doAction"  title="Edit Actions" onreset="clearDoAction()">
       <fieldset>
         <legend>Select per-step action</legend>
       <table>
         <tr><td>Display step details<input type="radio" id="action" 
                                            name="action" value="Display" /></td></tr>  
         <tr><td>Edit step<input type="radio" id="action" name="action"
                                 value="Edit" /></td></tr>
         <tr><td>Add leaf sibling (NYI)<input type="radio" id="action" name="action" 
                                        value=" leafSibling" disabled /></td></tr>
         <tr><td>Add subfolder sibling (NYI)<input type="radio" id="action" name="action" 
                                             value="subfolderSibling" disabled /></td></tr>
         <tr><td class="warn" ><b>Remove step</b> (NYI)<input type="radio" id="action" name="action"
                                   value="remove" disabled /></td></tr>
     
       </table>
       </fieldset>
       <p>
       <input type="submit" value="Do it" /> <input type="reset" value="Reset" />
       </p>
     </form>
    </c:when>
     <c:when test="${! empty param.folderSelectedPath}" >
         <h4>Selected step:  ${param.folderSelectedPath}</h4>
        <p>To unselect all steps use the browser refresh button</p>
      
     <form action="processAction.jsp" id="actionForm"  name="actionForm" 
           target="doAction"  title="Edit Actions" onreset="clearDoAction()" >
    
       <fieldset>
         <legend>Select per-step action</legend>
       <table>
     <%--    <tr><th>
       <label for="actions">Select Action</label> </th></tr> --%>
         <tr><td>Display step details<input type="radio" id="action" 
                                            name="action"value="Display" /></td></tr>  
         <tr><td>Edit step<input type="radio" id="action" name="action"
                                 value="Edit" /></td></tr>
   
         <tr><td>Add leaf child (NYI)<input type="radio" id="action" name="action" 
                                      value="leafChild" disabled /></td></tr>
         <tr><td>Add subfolder child (NYI)<input type="radio" id="action" name="action" 
                                           value="subfolderChild" disabled /></td></tr>
         <tr><td>Add leaf sibling (NYI)<input type="radio" id="action" name="action" 
                                        value="leafSibling" disabled  /></td></tr>
         <tr><td>Add subfolder sibling (NYI)<input type="radio" id="action" name="action" 
                                             value="subfolderSibling" disabled /></td></tr>
         <tr><td class="seriousWarn"><b>Remove step</b> (and all substeps) (NYI)<input type="radio" id="action" 
                                        name="action "value="remove" disabled /></td></tr>
       
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
