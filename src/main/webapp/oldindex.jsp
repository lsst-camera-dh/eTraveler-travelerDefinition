<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="db" uri="http://lsstcorp.org/etravelerbackenddbwar/DbTest" %>
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
        <h1>Hello from DbTest!</h1>
        <p>It's
 <jsp:useBean id="now" class="java.util.Date"/>    
<fmt:formatDate value="${now}" dateStyle="long"/> </p>
        <%--
<form method="get" action="dbtest.jsp">
  

<p>Calling DbTest main..  </p>

<p>
          ${db:mainFromJsp()}
</p>
  
  <input type="submit" value="DbTest" />
</form>
          --%>
   
 <h3> Display a Traveler </h3>
 <form method="get" action="displayTraveler.jsp">
<p>
   Traveler name: <input type="text" name="traveler_name" value="" /> <br />
   Version: <input type="text" name="traveler_version" value="1" /> <br />
   Database:  <select name="db"> 
     <option>Test</option>
     <option selected>Dev</option>
   </select>
     <%--
   Db: Test <input type="radio" name="db" value="test" />
   Dev <input type="radio" name="db" value="dev" checked />--%>
    <br />
   <input type="submit" value="Display" />
</p>
</form>
<h3> Upload a File</h3>
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
</form>

<h3> Ingest a File</h3>
<form name="YamlToDbForm" enctype="multipart/form-data" method="post"
      action="uploadYamlToDb.jsp">
     
  <table>
    <tr> <th><b> Yaml file: </b> </th> 
      <td><input type="file" name="importYamlFile" 
value="" width="60" /></td></tr>
    <tr><td>
        <b>Db:</b> </td><td>
        <table><tr>
            <td>Test  <input type="radio" name="db" value="test" /></td>
      <td>Dev <input type="radio" name="db" value="dev" checked /></td>
      
          </tr></table></td>
    </tr>
    <tr>
      <td><input type="submit" value="Import" name="importFromFileAction" />  
      </td>
      <td>&nbsp;
      </td></tr></table>
</form>
        
</body>
</html>
