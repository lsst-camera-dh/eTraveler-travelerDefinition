<%-- 
    Document   : middlelinks
    Created on : May 13, 2014, 2:24:45 PM
    Author     : jrb
--%>

<%@tag description="header decorator" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="srs_utils" uri="http://srs.slac.stanford.edu/utils" %>
<%@taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql"%>

<%-- Allow user to choose default db --%>
 <sql:query var="dbReleaseQ">
       select major, minor, patch, id from DbRelease where status='CURRENT' order by id desc limit 1;
 </sql:query>

<span align="right" >
            Database: [ <srs_utils:modeChooser mode="dataSourceMode"
                                   href="index.jsp" />],
            Schema version <c:forEach var="row" items="${dbReleaseQ.rows}" >
          <c:out value="${row.major}"/>.<c:out value="${row.minor}"/>.<c:out value="${row.patch}" />
        </c:forEach>
</span>

 