<%-- 
    Document   : middlelinks
    Created on : May 13, 2014, 2:24:45 PM
    Author     : jrb
--%>

<%@tag description="header decorator" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="srs_utils" uri="http://srs.slac.stanford.edu/utils" %>



<%-- Allow user to choose default db --%>

<span align="right" >
            Database: [ <srs_utils:modeChooser mode="dataSourceMode"
                                   href="index.jsp" />]
</span>

 