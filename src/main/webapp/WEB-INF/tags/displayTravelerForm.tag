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
<%@attribute name="htype" %>
<%@attribute name="oformat" %>

<%-- any content can be specified here e.g.: --%>
<style type="text/css">
  fieldset { border-color: black; width: 50%}
</style>

<c:set var="boo" value="jum" >
</c:set>

<%-- Leave-it-alone form --%>
<%-- Initial state form --%>

<c:if test="${empty traveler}" >
  
<form method="get" action="displayTraveler.jsp" >
<table>
<tr>   <td><b>Traveler name:</b></td>
<td> <input type="text" name="traveler_name" value="" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="1" /> </td></tr>
<tr><td> <b>Hardware type:</b></td>
<td> <input type="text" name="traveler_htype" value="" /> </td></tr>
</table>

<fieldset >
  <legend>Output style</legend>
   <input type="submit" name="ostyle" value="Pretty print" />
  <input type="submit" name="ostyle" value="Dot file" />
  <input type="submit" name="ostyle" value="Image" />
  <input type="submit" name="ostyle" value="Image map" />
  <input type="submit" name="ostyle" value="Tree" />
</fieldset>
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
<tr><td> <b>Hardware type:</b></td>
<td> <input type="text" name="traveler_htype" value="${htype}" /> </td></tr>
</table>

<fieldset >
  <legend>Output style</legend>
   
  <input type="submit" name="ostyle" value="Pretty print" />
  <input type="submit" name="ostyle" value="Dot file" />
  <input type="submit" name="ostyle" value="Image" />
  <input type="submit" name="ostyle" value="Image map" />
  <input type="submit" name="ostyle" value="Tree" />
 
</fieldset>
  </form>
</c:if>

