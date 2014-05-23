<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
   uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Display Traveler</title>
  </head>
  <body>
    <style type="text/css">
    <!--
      th { font-size: 10pt; font-weight: bold;}
      td { font-size: 10pt;}
      p  { font-size: 10pt;}
      h4 {font-size: 10pt; font-weight: bold;}
   -->
  </style>
    <h1>Display Traveler</h1>
    <jsp:useBean id="now" class="java.util.Date"/> 
    
    <p>It's now 
    <fmt:formatDate value="${now}" pattern="dd-MM-yyyy HH:mm:ss a z" /></p>
    <p>
    Displaying traveler <b>${param.traveler_name}</b>, version <b>${param.traveler_version}</b>,
    from db <b>${session.getAttribute("dataSourceMode")}</b></p>

    <p>
    ${import:retrieveProcess(pageContext)} 
    </p>
    <c:set var="nLines" value="${import:nLinesUsed(pageContext)}" /> 
    
    <%--  Branch on value of ostyle --%>
    <c:choose>
      <c:when test="${param.ostyle == 'pprint' }" >
         <c:if test="${nLines > 0}">
    <pre>
      
      <c:forEach var="i" begin="0" end="${nLines - 1}" >
        ${import:fetchLine(pageContext, i)}
      </c:forEach>
      
    </pre>
          </c:if>
      </c:when>
      <c:when test="${param.ostyle == 'dot' }" >
      <pre>
    ${import:dotSource(pageContext)}
      </pre>
      </c:when>
      <c:when test="${param.ostyle == 'img' }" >
    ${import:dotImg(pageContext)}
       </c:when>
      <c:when test="${param.ostyle == 'imgMap' }" >
    ${import:dotImgMap(pageContext)}
       </c:when>
    <c:when test="${param.ostyle == 'tree' }" >
   <%-- 
       <c:set var="treeRoot" value="${import:buildTree(pageContext)}" />
  <tree:tree model="${treeRoot}" folderHref="plotMain.jsp?folderSelectedPath=%p"
             leafHref="plotMain.jsp?leafSelectedPath=%p" 
             rootVisible="false" target="plotMain" showEmptyFolders="false" />
      --%>
     ${import:makeTree(pageContext, "view")}   
       </c:when>
   
    </c:choose>
</body>
</html>
