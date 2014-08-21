<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="backweb" uri="WEB-INF/BackendWebTags.tld" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>

<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="Window-Target" content="_top" >
        <title>JSP Page</title>
         <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimantName=LSST-CAMERA"
          rel="stylesheet" type="text/css" />
        <link href="css/backendStyle.css" type="text/css" rel="stylesheet" />
    </head>
    <body>
      
        <h1>View and Edit Traveler Definitions</h1>
        
          
        <c:set var="ttype_action" value="editTraveler.jsp" />
        <c:set var="result" value="${backweb:getTravelerTypeInfo(pageContext)}"/>

        <display:table   name="${result.rows}"   uid="ttypes" class="datatable"
                         decorator="org.lsstcorp.etravelerbackendnode.web.TtypeDecorator">
           <display:column property="name" 
                          sortable="true" style="text-align:left" />
           <display:column property="version" sortable="true" style="text-align:right" />
           <display:column property="hname" sortable="true" style="text-align:left" />
           <display:column property="description"  style="text-align:left" />
           <display:column property="createdBy" title="Creator"  style="text-align:left" />
           <display:column title="Creation TS" property="creationTS" sortable="true" 
                           style="text-align:left" />
           <display:column property = "viewEdit" title="View/edit" style="text-align:left"  />                     
           <display:column property = "addNCR" title="Add NCR" style="text-align:left"  />                        
         </display:table>
        
</body>
</html>
