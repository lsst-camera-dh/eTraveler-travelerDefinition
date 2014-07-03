<%-- 
    Document   : uploadTraveler
    Created on : Jun 19, 2014, 4:46:53 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>  
<%@taglib prefix="yamltodb" 
   uri="http://lsstcorp.org/etravelerbackend/YamlToDb" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Upload traveler</title>
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
    <style type="text/css">
      form    {width: 320px}
    </style>
  </head>
  <body>
    <h3 > Ingest a File</h3>
     
<form  name="YamlToDbForm" enctype="multipart/form-data" method="post"
      action="ingestOutput.jsp" >
     
 
    <p> <b> Yaml file: </b>
      <input type="file" name="importYamlFile" 
      value=""  /></p>
    <fieldset >
      <legend>Actions</legend>
   
      <input type="submit" value="Check" name="fileAction" />
     
      <input type="submit" value="Import" name="fileAction" />  
      
     </fieldset>
  
</form>
     
  </body>
</html>
