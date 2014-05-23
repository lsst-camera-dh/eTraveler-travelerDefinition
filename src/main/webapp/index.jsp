<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="db" uri="http://lsstcorp.org/etravelerbackend/DbTest" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
      <%--
        <h1>View and Create Traveler Definitions</h1>
        <p>It's
 <jsp:useBean id="now" class="java.util.Date"/>    
<fmt:formatDate value="${now}" dateStyle="long"/> </p>
   --%>
   <table width="100%" cellpadding="3" border="2">
    <tr><td>
 <h3 align="center"> Display a Traveler </h3>
 <form method="get" action="displayTraveler.jsp" >
<table>
<tr>   <td><b>Traveler name:</b></td>
<td> <input type="text" name="traveler_name" value="" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="1" /> </td></tr>
</table>

 <table cellpadding="2"><tr>
<td valign="bottom"><b>Output style:</b>
 <td>Pretty print <input type="radio" name="ostyle" value="pprint" checked /></td>
 <td>Dot source <input type="radio" name="ostyle" value="dot" /></td>
   <td>Image <input type="radio" name="ostyle" value="img" /> </td>
   <td>Mapped image <input type="radio" name="ostyle" value="imgMap" /> </td>
   <td>Tree <input type="radio" name="ostyle" value="tree" /> </td>
 </tr>
 </table>
<br />
  <input type="submit" value="Display" />
  </form>
 </td>
 <td valign="top">
  <h3 align="center"> Edit a Traveler </h3>
  
  <table><tr><td>
        <a href="editTraveler.jsp">Select traveler to edit</a>
      </td></tr></table>     

 </td></tr>
    <tr><td valign="top">
<h3 align="center"> Upload a File (check syntax without ingesting)</h3>
<form name="YamlImportForm" enctype="multipart/form-data" method="post"
      action="uploadYaml.jsp">
     
  <table >
    <tr> <th> <b> Yaml file: </b> </th>
      <td><input type="file" name="importYamlFile" 
value="" width="60" /></td></tr>
    <tr>
      <td><input type="submit" value="Import" name="importFromFileAction" />  
      </td>
      <td>&nbsp;
      </td></tr></table>
</form> </td>
<td>
<h3 align="center"> Ingest a File</h3>
<form name="YamlToDbForm" enctype="multipart/form-data" method="post"
      action="uploadYamlToDb.jsp">
     
  <table>
    <tr> <th><b> Yaml file: </b> </th> 
      <td><input type="file" name="importYamlFile" 
value="" width="60" /></td></tr>
    
    <tr>
      <td><input type="submit" value="Import" name="importFromFileAction" />  
      </td>
      <td>&nbsp;
      </td></tr></table>
</form>
</td></tr></table>
        
</body>
</html>
