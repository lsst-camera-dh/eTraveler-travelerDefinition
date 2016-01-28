<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="backweb" uri="WEB-INF/BackendWebTags.tld" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql"%>
<%@taglib prefix="filter" uri="http://srs.slac.stanford.edu/filter"%>

<sql:query var="statesQ">
   select name from TravelerTypeState order by name;
</sql:query>
   <sql:query var="groupsQ">
     select name from HardwareGroup order by name;
   </sql:query>

<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="Window-Target" content="_top" >
        <title>eTraveler Backend Welcome</title>
         <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimantName=LSST-CAMERA"
          rel="stylesheet" type="text/css" />
        <link href="css/backendStyle.css" type="text/css" rel="stylesheet" />
    </head>
    <body>
      
    <h2>View Process Traveler Definitions</h2>
    <table><tr><td class="vcenter">  <span class="vcenter"> <b>Filtering:</b> </span></td>
        <td>
     <filter:filterTable>
     
        <filter:filterInput var="name" title="Name (substring search)"/>
        <filter:filterSelection title="State" var="state" defaultValue='any'>
            <filter:filterOption value="any">Any</filter:filterOption>
            <c:forEach var="stateName" items="${statesQ.rows}">
                <filter:filterOption value="${stateName.name}"><c:out value="${stateName.name}"/></filter:filterOption>
            </c:forEach>
        </filter:filterSelection>
        <filter:filterSelection title="Version" var="version" defaultValue='all'>
            <filter:filterOption value="latest">Latest</filter:filterOption>
            <filter:filterOption value="all">All</filter:filterOption>
        </filter:filterSelection>
    <filter:filterSelection title="Hardware group" var="group" defaultValue='all'>
      <filter:filterOption value="all" >All</filter:filterOption>
      <c:forEach var="groupName" items="${groupsQ.rows}" >
        <filter:filterOption value="${groupName.name}"><c:out value="${groupName.name}" /></filter:filterOption>
      </c:forEach>
    </filter:filterSelection>
      </filter:filterTable>
        </td></tr></table>  
        <c:set var="ttype_action" value="displayTraveler.jsp" />
        <c:set var="result" value="${backweb:getTravelerTypeInfo(pageContext)}"/>

        <display:table   name="${result.rows}"   uid="ttypes" class="datatable"
                         decorator="org.lsst.camera.etraveler.backend.ui.TtypeDecorator">
           <display:column property="name" 
                          sortable="true" style="text-align:left" />
           <display:column property="version" sortable="true" style="text-align:right" />
           <display:column property="hname" sortable="true" style="text-align:left" />
           <display:column property="state" sortable="true" style="text-align:left" />
           <display:column property="description"  style="text-align:left" />
           <display:column property="createdBy" title="Creator"  style="text-align:left" />
           <display:column title="Creation TS" property="creationTS" sortable="true" 
                           style="text-align:left" />
           <display:column property = "addNCR" title="Add NCR" style="text-align:left"  />                        
         </display:table>
        
</body>
</html>
