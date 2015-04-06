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
<tr>   <td><b>Traveler name:</b></td>
<td> <input type="text" name="traveler_name" value="" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="1" /> </td></tr>
<tr><td> <b>Hardware group:</b></td>
<td> <input type="text" name="traveler_hgroup" value="" /> </td></tr>
</table>

<table><tr><td>
<fieldset >
  <legend>Output style</legend>
   <input type="submit" name="ostyle" value="Tree" />
   <input type="submit" name="ostyle" value="Yaml" />  
</fieldset> </td>
<td>&nbsp;</td>
<td>
  <fieldset>
    <legend> additional styles</legend>
   <input type="submit" name="ostyle" value="Pretty print" />
  <input type="submit" name="ostyle" value="Dot file" />
  <input type="submit" name="ostyle" value="Image" />
  <input type="submit" name="ostyle" value="Image map" />
</fieldset>
</td></tr></table>
  </form>

</c:if>

<c:if test="${! empty traveler }">
 <%-- <c:set var="ostyle" value="${oformat}" />  --%>
<form method="get" action="displayTraveler.jsp" >
<table>
<tr>   <td><b>Traveler name:</b></td>
<td> <input type="text" name="traveler_name" value="${traveler}" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="${version}" /> </td></tr>
<tr><td> <b>Hardware group:</b></td>
<td> <input type="text" name="traveler_hgroup" value="${hgroup}" /> </td></tr>
</table>
<table><tr><td valign="top">
<fieldset >
  <legend>Output&nbsp;style</legend>

   <input type="submit" name="ostyle" value="Tree" />
   <input type="submit" name="ostyle" value="Yaml" />  
</fieldset>
</td><td>&nbsp;</td>
<td>
  <fieldset>
    <legend> more&nbsp;styles</legend>
   <input type="submit" name="ostyle" value="Pretty print" />
   <input type="submit" name="ostyle" value="Yaml-debug" />
  <input type="submit" name="ostyle" value="Dot file" />
  <input type="submit" name="ostyle" value="Image" />
  <%--  Doesn't work so comment out for now
  <input type="submit" name="ostyle" value="Image map" />
  --%>
</fieldset>
</td></tr></table>
     </form>
</c:if>

