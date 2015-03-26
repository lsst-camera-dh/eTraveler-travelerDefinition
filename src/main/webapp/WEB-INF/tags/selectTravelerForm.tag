<%-- 
    Document   : selectTravelerForm
    Created on : Jun 20, 2014, 1:21:42 PM
    Author     : jrb
--%>

<%@tag description="puts up traveler select form" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="import" uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="formAction"%>

<%-- Clear any old tree selections --%>
<c:set var="nodePath" scope="session" />
<c:set var="leafSelectedPath" scope="session" />
<c:set var="folderSelectedPath" scope="session" />
<c:set var="isLeaf" scope="session" />
 <form method="get" action=${formAction}>
   <table>
     <tr>   
       <td><b>Traveler name:</b></td>
       <c:if test="${empty param.traveler_name}" >
       <td> <input type="text" name="traveler_name" value="" /> </td>
     </tr>
     <tr><td> <b>Version:</b></td>
         <td> <input type="text" name="traveler_version" value="1" /> </td>
     </tr>
     <tr><td> <b>Hardware group:</b></td>
         <td> <input type="text" name="traveler_hgroup" value="" /> </td>
     </tr>
       </c:if>
      <c:if test="${! empty param.traveler_name}" >
       <td> <input type="text" name="traveler_name" 
                   value="${param.traveler_name}" /> </td>
      </tr>
      <tr><td> <b>Version:</b></td>
         <td> <input type="text" name="traveler_version" 
                     value="${param.traveler_version}" /> </td>
      </tr>
      <tr><td> <b>Hardware group:</b></td>
        <td><input type="text" name="traveler_hgroup" 
                      value="${param.traveler_hgroup}" /> </td>
      </tr>
      </c:if>
   </table>

 
<br />
  <input type="submit" value="Display"  />
 </form>
  
 
<%--
  <input type="submit" name="outputType" id="outputType" value="Display" />
  
  <input type="submit" name="outputType" id="outputType" formaction="outputYaml.jsp" value="Yaml" />
  --%>
  


