<%-- 
    Document   : uploadYamlToDb
    Created on : Jan 17, 2014, 3:30:12 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="yamltodb" 
   uri="http://lsstcorp.org/etravelerbackend/YamlToDb" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Yaml to Db</title>
  </head>
  <body>
    <h2>Welcome to Yaml to Db</h2>
    <c:if test="${ ! empty param.importFromFileAction }" >
      <p>Got submit request from form on index.jsp</p>
      <c:choose>
        <c:when test="${ ! empty param.importYamlFile }" >
          <p>Got non-empty input </p>
        <preformat>
          ${param.importYamlFile}
        </preformat>
          <c:catch var="error">
            <p>About to ingest..</p>
            <c:set var="useTransactions" value="true" />
            ${yamltodb:ingest(pageContext)}
            <%--${yamltodb:ingest(param.importYamlFile, useTransactions)} --%>
            <p>did parse!</p>
          </c:catch>
          <c:choose>
            <c:when test="${empty error}">
              <c:set var="successMessage" value="Ingest successful." />
            </c:when>
            <c:otherwise>
              <c:set var="errorMessage" value="Ingest failed." />
            </c:otherwise>
          </c:choose>
        </c:when>
        <c:otherwise>
          <c:set var="errorMessage" 
                 value="Ingest failed: file name cannot be empty" />
        </c:otherwise>
      </c:choose>
    </c:if>
  </body>
</html>
