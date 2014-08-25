<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter" %> 
<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>

<%@taglib prefix="frames" uri="http://srs.slac.stanford.edu/frames" %>
<%@taglib prefix="local" tagdir="/WEB-INF/tags/" %>

<!DOCTYPE html>


<html>
  <head>  
    <title>Traveler Tree</title>
    <frames:fitToScreenInternalFrame ids="tree,action,doAction" />

    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" />    
  </head>
  <body>
  <c:if test="${ ! empty session.getAttribute('nodePath') }" >
    <p>session variable nodePath is session.getAttribute('nodePath')</p>
  </c:if>
  <table width="100%" height="100%" border="0" style="border-top: 1px solid black;">
    <tr width="100%">
      <td colspan="2">
    <local:selectTravelerForm formAction="editTraveler.jsp" />  

    <c:if test="${! empty param.traveler_name}" >
    
      <c:set var="traveler_name" value="${param.traveler_name}" scope="session" />
      <c:set var="traveler_version" value="${param.traveler_version}" scope="session"/>
      <c:set var="traveler_htype" value="${param.traveler_htype}" scope="session" />
      <c:set var="retrieveReturn" value="${import:retrieveProcess(pageContext, false)}" /> 
      <%! ModeSwitcherFilter msf;
              String dbtype; %>
   
              <% dbtype = msf.getVariable(session, "dataSourceMode"); %>
    </c:if>
    </td>
    </tr>

    <tr width="100%" >


    <c:if test="${ ! empty param.traveler_name}" >
      <c:choose>
        <c:when test="${empty retrieveReturn}" >
          <td valign="top" width="300" style="border-right: 1px solid black;">
       <%-- netbeans wants height: rather than height=  etc. but then resizing 
            doesn't work!     so leave as is --%>
          <iframe  name="tree" id="tree" src="showTree.jsp?reason=edit" 
                 scrolling="auto" 
                 marginwidth="0" marginheight="0" 
                 frameborder="0" vspace="0" hspace="0" 
                 style="width:100%; height=100%;"></iframe>
        </td>
        <td valign="top" width="300" style="border-right: 1px solid black;">
          <iframe  name="action" id="action" src="actionTraveler.jsp" 
                   scrolling="auto" marginwidth="0" marginheight="0" 
                   frameborder="0" vspace="0"    hspace="0" 
                   style="width:100%; height=100%;" width="100%"></iframe> 
        </td>
        </c:when>
        <c:otherwise>
          <td valign="top" width="300" style="border-right: 1px solid black;">
       <%-- netbeans wants height: rather than height=  etc. but then resizing 
            doesn't work!     so leave as is --%>
       <p> ${retrieveReturn} </p>   
          </td>
        </c:otherwise>
      </c:choose>
    </c:if >
 
  

    <td valign="top">
      <% if (session.getAttribute("nodePath") != null) { %>
      <iframe name="doAction"  id="doAction" scrolling="auto" marginwidth="0" 
            marginheight="0" frameborder="0" vspace="0" hspace="0" 
            style="width:100%; height=100%" src="processAction.jsp"
           > </iframe>
      <% } else { %>
      <iframe name="doAction"  id="doAction" scrolling="auto" marginwidth="0" 
              marginheight="0" frameborder="0" vspace="0" hspace="0" 
              style="width:100%; height=100%"  > </iframe>
  <% } %>
    </td>
    </tr>
    </table>    
    </body>
</html>
