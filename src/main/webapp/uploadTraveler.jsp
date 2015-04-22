<%-- 
    Document   : uploadTraveler
    Created on : Jun 19, 2014, 4:46:53 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>  
<%-- <%@taglib prefix="yamltodb" 
   uri="http://lsstcorp.org/etravelerbackend/WriteToDb" %>  --%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Upload traveler</title>
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
    <style type="text/css">
      form    {width: 320px; background-color: lightgray; 
                border-color: black; border-width: 1}
      form.wide {width: 600px; background-color: lightgray; border-width: 1}
      h3 {color: forestgreen}
    </style>
  </head>
  <body>
    <h2 > Ingest a File</h2>
    <table width="100%"><tr><td valign="top">
    <h3> Check/validate only</h3>
     
<form  name="YamlToDbTestForm" enctype="multipart/form-data" method="post"
      action="ingestOutput.jsp" >
     
 
    <p> <b> Yaml file: </b>
      <input type="file" name="importYamlFile" 
      value=""  /></p>
    <fieldset >
      <legend>Actions</legend>
   
      <input type="submit" value="Check YAML" name="fileAction" />
      <input type="submit" value="Db validate" name="fileAction" />
      
     </fieldset>
  
</form>
    
        </td><td>&nbsp;</td><td valign="top">
    
    <h3>Full ingest</h3>
    
     <form class="wide" name="YamlToDbForm" enctype="multipart/form-data" method="post"
      action="ingestOutput.jsp" >
     
 
    <p> <b> Yaml file: </b>
      <input type="file" name="importYamlFile" 
      value=""  /></p>
   
    <table ><tr>
        <td><label for="reason"><b> Description of new process traveler: </b>
          </label></td>
          <td><textarea  rows="2" cols="60" id="reason"  name="reason" >
 
            </textarea> 
          </td></tr>
      <tr><td>
          <label for="owner">
    <b>Responsible person:</b> </label></td>
    <td><input type="text" name="owner" cols="50" id="owner" value=""/>
    </td></tr></table>
      <fieldset>
      <legend>Actions</legend>
   
      <input type="submit" value="Import" name="fileAction" />  
      </fieldset>
    
  
</form>
        </td></tr></table>
  </body>
</html>
