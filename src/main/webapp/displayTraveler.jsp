<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
   uri="http://lsstcorp.org/etravelerbackenddbwar/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Display Traveler</title>
  </head>
  <body>
    <h1>Display Traveler</h1>
    <jsp:useBean id="now" class="java.util.Date"/> 
    
    It's now 
    <fmt:formatDate value="${now}" pattern="dd-MM-yyyy HH:mm:ss a z" />
    <br /> <br />
    Name is ${param.traveler_name} <br />
 <%--  ${import:retrieveProcess( param.traveler_name, param.traveler_version)} --%>  
    ${import:retrieveProcess(pageContext)} 
    <br />
    <c:set var="nLines" value="${import:nLinesUsed(param.traveler_name, 
                                 param.traveler_version)}" /> 
    <c:if test="${nLines > 0}">
    <preformat>
      
      <c:forEach var="i" begin="0" end="${nLines - 1}" >
        ${import:fetchLine(param.traveler_name, param.traveler_version, i)}
      </c:forEach>
    </preformat>
    </c:if>
</body>
</html>
