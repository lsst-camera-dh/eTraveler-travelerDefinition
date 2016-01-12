<%-- 
    Document   : displayTravelerForm
    Created on : Jun 20, 2014, 1:21:42 PM
    Author     : jrb
--%>

<%@tag description="puts up traveler display form" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="traveler"%>
<%@attribute name="version" %>
<%@attribute name="hgroup" %>
<%@attribute name="oformat" %>

<%-- any content can be specified here e.g.: --%>
<style type="text/css">
  fieldset { border-color: black; width: 30%}
</style>

<%-- Leave-it-alone form --%>
<%-- Initial state form --%>

<c:if test="${empty traveler}" >
  
<form method="get" action="displayTraveler.jsp" >
<table>
<tr>   <td>
<fieldset >
<legend>Chosen&nbsp;traveler&nbsp;type</legend>
<table>
<tr><td><b>Traveler&nbsp;name:</b></td>
<td> <input type="text" name="traveler_name" value="" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="1" /> </td></tr>
<tr><td> <b>Hardware&nbsp;group:</b></td>
<td> <input type="text" name="traveler_hgroup" value="" /> </td></tr>
</table>
</fieldset>
</td>
<td>&nbsp;</td>
<td>

 <table><tr><td>
<fieldset >
  <legend>Output style</legend>
   <input type="submit" name="ostyle" value="Tree" 
          title="Collapsible/expandable tree. Click node for details "/>
   <input type="submit" name="ostyle" value="Pretty print"
          title="reasonably complete simple text version of the process traveler"/>
  <input type="submit" name="ostyle" value="Yaml-canonical"
         title="export canonical YAML to local file"/>
  <input type="submit" name="ostyle" value="Yaml-verbose"
         title="export verbose YAML to local file"/>
</fieldset>
</td></tr></table>

</td></tr></table>
  </form>

</c:if>

<c:if test="${! empty traveler }">
 <%-- <c:set var="ostyle" value="${oformat}" />  --%>
<form method="get" action="displayTraveler.jsp" >
<table>
<tr><td>
<fieldset >
<legend>Chosen&nbsp;traveler&nbsp;type</legend>
<table>
<tr><td><b>Traveler&nbsp;name:</b></td>
<td> <input type="text" name="traveler_name" value="${traveler}" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="${version}" /> </td></tr>
<tr><td> <b>Hardware&nbsp;group:</b></td>
<td> <input type="text" name="traveler_hgroup" value="${hgroup}" /> </td></tr>
</table>
</fieldset>
</td>
<td>&nbsp;</td>
<td>
<table><tr><td valign="top">
<fieldset >
  <legend>Output&nbsp;style</legend>

   <input type="submit" name="ostyle" value="Tree" />
  <input type="submit" name="ostyle" value="Pretty print" />
  <input type="submit" name="ostyle" value="Yaml-canonical"
         title="export canonical YAML to local file"/>
  <input type="submit" name="ostyle" value="Yaml-verbose"
         title="export verbose YAML to local file"/>
</fieldset>
</td></tr></table>
</td></tr></table>
     </form>
</c:if>

