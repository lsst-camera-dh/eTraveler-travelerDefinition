<%-- 
    Document   : uploadAssemblySpreadsheet
    Created on : Jun 19, 2014, 4:46:53 PM
    Author     : jrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>  
 <%@taglib prefix="spreadsheet"  uri="WEB-INF/AssemblySpreadsheet.tld" %>
 <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Upload Assembly Spreadsheet</title>
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
    <style type="text/css">
      form.medium    {width: 320px; background-color:  gainsboro }
      form.wide {width: 600px; background-color: gainsboro}
      h3 {color: forestgreen}
    </style>
  </head>
  <body>
  
   
    <h2 > Ingest a Spreadsheet</h2>
    <table width="100%"><tr><td valign="top">

    <h3> Check/validate only</h3>
     
    <form class="medium" name="UploadSpreadsheetForm"
    	  enctype="multipart/form-data" method="post" >
 
    <p> <b> Spreadsheet file: </b>
      <input type="file" name="importSpreadsheet" value="" /></p>
    </p>
    <fieldset >
      <legend>Actions</legend>
   
      <input type="submit" value="Check spreadsheet" name="fileAction" 
             title="See if spreadsheet meets syntax requirements.  Does not ingest into database"/>
      
      <input type="submit" value="Db validate" name="fileAction"
             title="See if spreadsheet conforms to assembly requirements and is compatible in some respects to selected databse. Does not ingest."/>
      
     </fieldset>
  
  
    </form>
  </td>
  <td>&nbsp;</td>
  
<%--
  <td valign="top">    
    <h3>Full ingest</h3>
    Needs more work...not yet clear where this ends up in db
    <form class="wide" name="SpreadsheetToDbForm"
                 enctype="multipart/form-data" method="post">   
 
      <p> <b> Spreadsheet: </b>
          <input type="file" name="importSpreadsheetFile" value="" /></p>
      <input name="strictNameChecking" type="hidden" value="on"/>
      <table ><tr>
        <td><label for="reason"><b> Description of new assembly template: </b>
        </label></td>
        <td><textarea  rows="2" cols="60" id="reason"  name="reason"
                       value="">
            </textarea> 
        </td></tr>
        <tr><td>
          <label for="owner">
          <b>Responsible person:</b> </label>
        </td>
        <td><input type="text" name="owner" cols="50" id="owner" value=""/>
        </td>
        </tr>
       </table>
       <fieldset>
          <legend>Actions</legend>
          <input type="submit" value="Import" name="fileAction" 
                 title="Create new assembly template in the database"/>  
       </fieldset>
     </form>

   </td>
--%>


    </tr></table>
    <c:if test ="${! empty param.fileAction}" >
      <hr />
      <h2>Results of ${param.fileAction}</h2>
      
      <spreadsheet:IngestSpreadsheet />
     </c:if> 

  </body>
</html>
