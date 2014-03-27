<%-- 
    Document   : uploadYaml
    Created on : Nov 27, 2013, 1:32:12 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="impyaml" 
   uri="http://lsstcorp.org/etravelerbackend/YamlImporter" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Upload Yaml</title>
  </head>
  <body>
    <h2>Welcome to Upload Yaml</h2>
    <c:if test="${ ! empty param.importFromFileAction }" >
      <p>Got submit request from form on index.jsp</p>
      <c:choose>
        <c:when test="${ ! empty param.importYamlFile }" >
          <p>Got non-empty input </p>
        <preformat>
          ${param.importYamlFile}
        </preformat>
     
            <p>About to parse..</p>
            ${impyaml:parse(pageContext)}
            <%-- ${impyaml:parse(param.importYamlFile, param2)} --%>
            <p>did parse!</p>
       
          <c:choose>
            <c:when test="${empty error}">
              <c:set var="successMessage" value="Import successful." />
            </c:when>
            <c:otherwise>
              <c:set var="errorMessage" value="Import failed." />
            </c:otherwise>
          </c:choose>
        </c:when>
        <c:otherwise>
          <c:set var="errorMessage" 
                 value="Import failed: file name cannot be empty" />
        </c:otherwise>
      </c:choose>
    </c:if>
  </body>
</html>
