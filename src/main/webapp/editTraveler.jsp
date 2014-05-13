<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>

<%@taglib prefix="frames" uri="http://srs.slac.stanford.edu/frames" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
  
    <title>Traveler Tree</title>
    
  <frames:fitToScreenInternalFrame ids="tree,action,doAction" />
    
  </head>
  <style type="text/css">
    <!--
      th { font-size: 10pt; font-weight: bold;}
      td { font-size: 10pt;}
      p  { font-size: 10pt;}
      h4 {font-size: 10pt; font-weight: bold;}
   -->
  </style>
   
  <c:if test="${! empty session.getAttribute('nodePath') }" >
    <p>session variable nodePath is session.getAttribute('nodePath')</p>
  </c:if>
  <table width="100%" height="100%" border="0" style="border-top: 1px solid black;">
    <tr width="100%">
      <td colspan="2">
 <form method="get" action="editTraveler.jsp">
   <table>
     <tr>   
       <td><b>Traveler name:</b></td>
       <c:if test="${empty param.traveler_name}" >
       <td> <input type="text" name="traveler_name" value="" /> </td>
     </tr>
     <tr><td> <b>Version:</b></td>
         <td> <input type="text" name="traveler_version" value="1" /> </td>
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
      </c:if>
   </table>

   
 <table><tr>     
     <td valign="bottom"><b>Db:</b> 
       <c:choose>
         <c:when test="${! empty param.db}">
       
     <%-- <p> param.db is ${param.db}</p>   --%>
  
      
     <c:set var="dbSeen" value="${param.db}" />
         </c:when> 
   
         <c:otherwise>
       <c:set var="dbSeen" value="dev" />
         </c:otherwise>
       </c:choose>
       </td>
     <%-- <p> dbSeen is ${dbSeen}</p>  --%>
     <td valign="bottom">
       <c:choose>
     <c:when test="${dbSeen == 'dev'}" >
      Test  <input type="radio" name="db" value="test" /></td>
      <td>Dev <input type="radio" name="db" value="dev" checked />
     </c:when>
     <c:otherwise>
      Test  <input type="radio" name="db" value="test" checked/></td>
      <td>Dev <input type="radio" name="db" value="dev"  />
     </c:otherwise>
      </c:choose>
     </td>
 </tr>
 </table>
 
<br />
  <input type="submit" value="Display" />
  </form>
     <c:if test="${! empty param.traveler_name}" >
       <p>Traveler name:  ${param.traveler_name}  <br />
    Version:   ${param.traveler_version}  <br />
    Db:  ${param.db} </p> 

    ${import:retrieveProcess(pageContext)} 

    <c:set var="traveler_name" value="${param.traveler_name}" scope="session" />
    <c:set var="traveler_version" value="${param.traveler_version}" scope="session"/>
    <c:set var="db" value="${param.db}" scope="session" />
     </c:if>
</td>
</tr>
 <%--
  <table width="100%" height="100%" border="0" style="border-top: 1px solid black;">
 --%>
      <tr width="100%" >
        <td valign="top" width="300" style="border-right: 1px solid black;">
       <%-- netbeans wants width: rather than width=  etc. but then resizing 
            doesn't work!     so leave as is --%>

 
  <c:if test="${! empty param.traveler_name}" >
    
    


          <iframe  name="tree" id="tree" src="showTree.jsp" scrolling="auto" 
                   marginwidth="0" marginheight="0" 
                   frameborder="0" vspace="0" hspace="0" 
                   style="width=100%; height=100%;"></iframe>
        </td>
        <td valign="top" style="border-right: 1px solid black;">
          <iframe  name="action" id="action" src="actionTraveler.jsp" 
                   scrolling="auto" marginwidth="0" marginheight="0" 
                   frameborder="0" vspace="0"    hspace="0" 
                   style="width=100%; height=100%;" width="100%"></iframe> 
     
      
  </c:if >
  <c:if test="${ empty param.db}">

          <iframe  name="tree" id="tree"  scrolling="auto" marginwidth="0" 
                   marginheight="0" frameborder="0" vspace="0" hspace="0" 
                   style="width=100%; height=100%;"></iframe>
        </td>
        <td valign="top" >
          <iframe  name="action" id="action"  scrolling="auto" marginwidth="0" 
                   marginheight="0" frameborder="0" vspace="0" hspace="0" 
                   width="100%" style="width=100%; height=100%;"></iframe> 
         
  </c:if>
 
  </td>

  <td valign="top">
       <% if (session.getAttribute("nodePath") != null) { %>
    <iframe name="doAction"  id="doAction" scrolling="auto" marginwidth="0" 
            marginheight="0" frameborder="0" vspace="0" hspace="0" 
            style="width=100%; height=100%" src="processAction.jsp"
            width="100%" height="100%"> </iframe>
  <% } else { %>
    <iframe name="doAction"  id="doAction" scrolling="auto" marginwidth="0" 
            marginheight="0" frameborder="0" vspace="0" hspace="0" 
            style="width=100%; height=100%" > </iframe>
  <% } %>
   </td>
      </tr>
    </table>    
  
</html>
