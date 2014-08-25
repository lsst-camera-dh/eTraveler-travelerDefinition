<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.srs.web.base.filters.modeswitcher.ModeSwitcherFilter" %> 
<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>

<%@taglib prefix="frames" uri="http://srs.slac.stanford.edu/frames" %>
<%@taglib prefix="ncr" uri="WEB-INF/NCRTags.tld" %>
<%@taglib prefix="local" tagdir="/WEB-INF/tags/" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
  
    <title>Add NCR</title>
    
  <frames:fitToScreenInternalFrame ids="tree,NCR" />
    
    

  <link href="css/backendStyle.css" type="text/css" rel="stylesheet" />    
  </head>

  <%--
  <c:if test="${! empty sessionScope.nodePath }" >
    <p>session variable nodePath is ${sessionScope.nodePath}</p>
  </c:if>
  --%>
  <%-- Clear session variables --%>
  <ncr:ClearNCRVariables />

  
  <table width="100%" height="100%" border="0" style="border-top: 1px solid black;">
    <tr width="100%">
      <td colspan="2">
        <local:selectTravelerForm formAction="addNCR.jsp" /> 
 
        
    <%-- Following modified from editTraveler; probably needs further revision --%>
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
        <td valign="top" width="300" style="border-right: 1px solid black;">
       <%-- netbeans wants width: rather than width=  etc. but then resizing 
            doesn't work!     so leave as is --%>

  <c:if test="${! empty param.traveler_name}" >
    <c:choose>
      <c:when test="${empty retrieveReturn}" >
        <iframe  name="tree" id="tree" src="showTree.jsp?reason=NCR" 
	         scrolling="auto" 
                 marginwidth="0" marginheight="0" 
                 frameborder="0" vspace="0" hspace="0" 
                 style="width:100%; height=100%;"></iframe>
      </td>
      <td valign="top" >
        <iframe  name="NCR" id="NCR" src="NCRForm.jsp" 
                 scrolling="auto" marginwidth="0" marginheight="0" 
                 frameborder="0" vspace="0"    hspace="0" 
                 style="width:100%; height=100%;" width="100%"></iframe> 
      </c:when>
      <c:otherwise>
        <p> ${retrieveReturn} </p>      
      </c:otherwise>
      </c:choose>
  </c:if >
 
  </td>

   </tr>
 </table>    
  
</html>
