<%-- 
    Document   : uploadTraveler
    Created on : Jun 19, 2014, 4:46:53 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>  
 <%@taglib prefix="yamltodb" 
   uri="http://etraveler.camera.lsst.org/backend/WriteToDb" %> 
 <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Upload traveler</title>
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
    <style type="text/css">
      form.medium    {width: 320px; background-color:  gainsboro }
      form.wide {width: 600px; background-color: gainsboro}
      h3 {color: forestgreen}
    </style>
  </head>
  <body>
  
   
    <h2 > Ingest a File</h2>
    <table width="100%"><tr><td valign="top">
    <h3> Check/validate only</h3>
     
    <form class="medium" name="YamlToDbTestForm" enctype="multipart/form-data" method="post" >
 
    <p> <b> Yaml file: </b>
      <input type="file" name="importYamlFile" value="" /></p>
     <!-- <p title="If checked issues a warning for each deprecated step name"><b>Enable step name vetting: </b>-->
    <input name="strictNameChecking" type="hidden" value="on"/>
    </p>
    <fieldset >
      <legend>Actions</legend>
   
      <input type="submit" value="Check YAML" name="fileAction" 
             title="See if YAML file conforms to eTraveler requirements.  Does not ingest into database"/>
      <input type="submit" value="Db validate" name="fileAction"
             title="See if YAML file conforms to eTraveler requirements and is compatible in some respects to selected databse. Does not ingest."/>
      
     </fieldset>
  
  
    </form>
    
        </td><td>&nbsp;</td><td valign="top">
    
    <h3>Full ingest</h3>
    
     <form class="wide" name="YamlToDbForm" enctype="multipart/form-data" 
           method="post">   
 
    <p> <b> Yaml file: </b>
      <input type="file" name="importYamlFile" value="" /></p>
      <input name="strictNameChecking" type="hidden" value="on"/>
    <table ><tr>
        <td><label for="reason"><b> Description of new process traveler: </b>
          </label></td>
          <td><textarea  rows="2" cols="60" id="reason"  name="reason"
                         value="">
            </textarea> 
          </td></tr>
      <tr><td>
          <label for="owner">
    <b>Responsible person:</b> </label></td>
    <td><input type="text" name="owner" cols="50" id="owner" 
               value=""/>
    </td></tr></table>
      <fieldset>
      <legend>Actions</legend>
   
      <input type="submit" value="Import" name="fileAction" 
             title="Create new traveler definition in the database"/>  
      </fieldset>
</form>

   </td></tr></table>
    <c:if test ="${! empty param.fileAction}" >
      <hr />
      <h2>Results of ${param.fileAction}</h2>
      
      ${yamltodb:ingest(pageContext)}
               
               
     </c:if> 
  </body>
</html>
