<%@tag description="header decorator" pageEncoding="UTF-8"%>
<%@taglib prefix="srs_utils" uri="http://srs.slac.stanford.edu/utils" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table>
  <tr valign="bottom" align="right">
    <td valign="bottom" align="right">           
       <srs_utils:conditionalLink name="Welcome" url="index.jsp" 
                                 iswelcome="true"/> | 

       <srs_utils:conditionalLink name="Display" 
                                 url="displayTraveler.jsp" /> |
       <srs_utils:conditionalLink name="Edit" 
                                 url="editTraveler.jsp" /> |
       <srs_utils:conditionalLink name="Add NCR"
                                  url="addNCR.jsp" /> |
       <srs_utils:conditionalLink name="Upload" 
                                 url="uploadTraveler.jsp" />
    </td>
</tr>
</table>
