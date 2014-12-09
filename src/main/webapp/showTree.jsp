<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
   uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>

<!DOCTYPE html>


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Traveler Tree</title>
  </head>
  <body>
 
  <%-- ${import:makeTree(pageContext, "edit")}  --%>   
  ${import:makeTree(pageContext, param.reason)}    
</body>
</html>
