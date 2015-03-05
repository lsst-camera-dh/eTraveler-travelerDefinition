<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
   uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>
<%@taglib prefix="local" tagdir="/WEB-INF/tags/" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Display Traveler</title>
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" />
  </head>
  <body>
   
    <h1>Display Traveler</h1>
  
    <c:choose>
    <c:when test="${ (empty param.traveler_name) || (empty param.ostyle)}">
        <local:displayTravelerForm />
    </c:when>
    <c:otherwise>
        <local:displayTravelerForm traveler="${param.traveler_name}"
                                 version="${param.traveler_version}"
                                 hgroup="${param.traveler_hgroup}"
                                 oformat="${param.ostyle}" />
    </c:otherwise>
      
    </c:choose>
        <c:if test="${! empty param.traveler_name}" >
    <p>
    Displaying traveler <b>${param.traveler_name}</b>, version <b>${param.traveler_version}</b>,
    from db <b>${appVariables.dataSourceMode}</b></p>

    <p>
    ${import:retrieveProcessPrint(pageContext)} 
    </p>
    ${import:displayTraveler(pageContext)}
   
        </c:if>
</body>
</html>
