<%-- 
    Document   : getSummary
    Created on : Dec 12, 2014, 1:09:06 PM
    Author     : jrb
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="client" uri="WEB-INF/RestfulClientTags.tld" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
     <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
    <title>Result Summary Data</title>
  </head>
  <body>
    <h1>Result Summary Data</h1>

    <p>Select job to view by clicking on its id</p>

    <c:set var="result" 
           value="${client:getHarnessed(pageContext)}" />
    
     <display:table   name="${result.rows}"   uid="ttypes" class="datatable"
                         decorator="org.lsstcorp.etravelerbackend.rest.client.ActivityDecorator">
           <display:column property="id" 
                          sortable="true" style="text-align:left" />
           <display:column property="jobname" sortable="true" style="text-align:right" />
           <display:column property="userVersion" sortable="true" style="text-align:left" />
           <display:column property="hardwareType" sortable="true" style="text-align:left" />
      
           <display:column property="closedBy" sortable="true" title="Closer"  style="text-align:left" />
           <display:column title="ending TS" property="endTS" sortable="true" 
                           style="text-align:left" />
                         
         </display:table>
    
    
  </body>
</html>
