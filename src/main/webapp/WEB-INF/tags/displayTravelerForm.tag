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
<%@attribute name="oformat" %>

<%-- any content can be specified here e.g.: --%>

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
</table>

 <table cellpadding="2"><tr>
<td valign="bottom"><b>Output style:</b>
 <td>Pretty print <input type="radio" name="ostyle" value="pprint" /></td>
 <td>Dot source <input type="radio" name="ostyle" value="dot" /></td>
   <td>Image <input type="radio" name="ostyle" value="img" /> </td>
   <td>Mapped image <input type="radio" name="ostyle" value="imgMap" /> </td>
   <td>Tree <input type="radio" name="ostyle" value="tree" /> </td>
 </tr>
 </table>
<br />
  <input type="submit" value="Display" />
  </form>

</c:if>

<c:if test="${! empty traveler }">
  <c:set var="ostyle" value="${oformat}" />
<form method="get" action="displayTraveler.jsp" >
<table>
<tr>   <td><b>Traveler name:</b></td>
<td> <input type="text" name="traveler_name" value="${traveler}" /> </td></tr>
<tr><td> <b>Version:</b></td>
<td> <input type="text" name="traveler_version" value="${version}" /> </td></tr>
</table>

 <table cellpadding="2"><tr>
<td valign="bottom"><b>Output style:</b>
 <td>Pretty print <input type="radio" name="ostyle" value="pprint" /></td>
 <td>Dot source <input type="radio" name="ostyle" value="dot" /></td>
   <td>Image <input type="radio" name="ostyle" value="img" /> </td>
   <td>Mapped image <input type="radio" name="ostyle" value="imgMap" /> </td>
   <td>Tree <input type="radio" name="ostyle" value="tree" /> </td>
 </tr>
 </table>
<br />
  <input type="submit" value="Display" />
  </form>
</c:if>

