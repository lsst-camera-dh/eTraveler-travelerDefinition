<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Traveler Action &quot;${param.action}&quot; in Progress</title>
  </head>
  <body>
    <h3> 
      <% if(session.getAttribute("leafPath") != null ) { %>
          <%= session.getAttribute("leafPath")   %>
      <% }  %>
      <% if(session.getAttribute("folderPath") != null ) { %>  
          <%= session.getAttribute("folderPath")  %>
      <% } %>
    </h3>
    <%--
    <p> Try to write session attribute leafPath:   [ 
      <%=  session.getAttribute("leafPath")    %>
      ] </p>
    <p> Try to write action: [
      <%=   request.getParameter("action")        %>  <br />
      As param.action:  ${param.action}
     ] </p>  --%>
    <c:choose>
    <c:when test="${param.action == 'Display' }">
         <display:table name="${import:selectedNodeAttributes(pageContext)}" 
                                class="datatable" >
           <display:column property="name" title="Attributes" 
                           headerClass="sortable" style="text-align:left" />
           <display:column property="value" />
         </display:table>
    </c:when>
    <c:otherwise>
      ${import:doAction(pageContext, param.action)}
    </c:otherwise>
    </c:choose>
  
    
  </body>
</html>
