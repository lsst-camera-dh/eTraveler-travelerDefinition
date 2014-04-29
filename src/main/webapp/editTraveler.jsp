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
    <title>Traveler Tree</title>
  </head>
  <body>
   
    
   
 <form method="get" action="editTraveler.jsp">
<table>
<tr>   <td><b>Traveler name:</b></td>
 <c:if test="${empty param.traveler_name}" >
<td> <input type="text" name="traveler_name" value="" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="1" /> </td></tr>
</c:if>
 <c:if test="${! empty param.traveler_name}" >
<td> <input type="text" name="traveler_name" value="${param.traveler_name}" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="${param.traveler_version}" /> </td></tr>
</c:if>
</table>

 <table><tr>     
     <td><b>Db:</b> </td>
 
     <c:if test="${! empty param.db}">
     <p> param.db is ${param.db}</p>
  
     
       <c:set var="dbSeen" value="${param.db}" />
     </c:if> 
   
     <c:if test="${ empty dbSeen}">
       <c:set var="dbSeen" value="dev" />
     </c:if>
     <p> dbSeen is ${dbSeen}</p>
     <c:if test="${dbSeen == 'dev'}" >
      <td>Test  <input type="radio" name="db" value="test" /></td>
      <td>Dev <input type="radio" name="db" value="dev" checked /></td>
     </c:if>
      <c:if test="${dbSeen == \"test\"}" >
      <td>Test  <input type="radio" name="db" value="test" checked/></td>
      <td>Dev <input type="radio" name="db" value="dev"  /></td>
     </c:if>
 </tr>
 </table>
 
<br />
  <input type="submit" value="Display" />
  
  <br /> <br />
  
  <c:if test="${! empty param.traveler_name}" >
    
    <p>Traveler name:  ${param.traveler_name}  <br />
    Version:   ${param.traveler_version}  <br />
    Db:  ${param.db} </p> 

    ${import:retrieveProcess(pageContext)} 

    <c:set var="traveler_name" value="${param.traveler_name}" scope="session" />
    <c:set var="traveler_version" value="${param.traveler_version}" scope="session"/>
    <c:set var="db" value="${param.db}" scope="session" />

    <table width="100%" height="100%" border="0" style="border-top: 1px solid black;">
      <tr width="100%" height=""100%" >
        <td valign="top" width="50%" style="border-right: 1px solid black;">
          <iframe  name="tree" id="tree" src="showTree.jsp" scrolling="auto" marginwidth="0" marginheight="0" frameborder="0" vspace="0" hspace="0" style="width:100%; height:100%;"></iframe>
        </td>
        <td valign="top" width="50%">
          <iframe  name="action" id="action" src="actionTraveler.jsp" scrolling="auto" marginwidth="0" marginheight="0" frameborder="0" vspace="0" hspace="0" style="width:100%; height:100%;"></iframe> 
        </td>
      </tr>
    </table>    
  </c:if >
  </body>
</html>
