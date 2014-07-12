<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tree" uri="http://java.freehep.org/tree-taglib" %>
<%@taglib prefix="ncr" uri="WEB-INF/NCRTags.tld" %>
<%@taglib prefix="local" tagdir="/WEB-INF/tags/" %>
<!DOCTYPE html>


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>NCR Form</title>
    <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimentName=LSST-CAMERA" rel="stylesheet" type="text/css">
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
  </head>
  <body>
    
  


  <c:choose>
    <c:when test="${! empty param.reset}" >
      <ncr:ClearNCRVariables />  
    </c:when>
    <c:otherwise>
      <c:if test="${! empty sessionScope.nodePath}" >

        <c:if test="${param.exitOrReturn == 'exit to NCR'}" >
          <c:set var="exitStep" value="${sessionScope.nodePath}" scope="session" />
        </c:if>
        <c:if test="${param.exitOrReturn == 'return from NCR'}" >
          <c:set var="returnStep" value="${sessionScope.nodePath}" scope="session" />
        </c:if>
      </c:if>
      <c:if test="${! empty param.NCRCondition}" >
        <c:set var="NCRCondition" value="${param.NCRCondition}" scope="session" />
      </c:if>
    </c:otherwise>
  </c:choose>
  
   <c:if test="${ ! empty param.create}" >
     <c:choose>
       <c:when test="${ empty param.ncrTraveler }" >
         <p>Missing required field <b>NCR traveler</b>. No travelers of
           appropriate hardware type found.</p>
       </c:when>
       <c:when test="${ empty sessionScope.NCRCondition || empty sessionScope.exitStep || empty sessionScope.returnStep}" >
         <c:if test="${ empty sessionScope.NCRCondition}">
           <p>Missing required field <b>Condition text</b></p>
         </c:if>
         <c:if test="${ empty sessionScope.exitStep}">
           <p>Missing required field <b>Exit step</b>. 
             Use tree to choose step; then exit/return selection</p>
         </c:if>
         <c:if test="${ empty sessionScope.returnStep}">
         <p>Missing required field <b>Return step</b>. 
           Use tree to choose step; then exit/return selection.</p>
         </c:if>
       </c:when>
       
         
       <c:otherwise>
         ${import:makeNCR(pageContext, param.ncrTraveler)}
          <p>Making the NCR!! </p>
       </c:otherwise>
     </c:choose>
   </c:if>
 
  <form action="NCRForm.jsp" target="NCR" >
   
    <c:choose>
      <c:when test="${! empty sessionScope.nodePath}" >
        <p>Selected step:<b> ${sessionScope.nodePath}</p>
        <table>
          <tr>
            <td>Use this step for:</td>
            <td><input type="submit" name="exitOrReturn"  
                       value="exit to NCR" />  
              <input type="submit" name="exitOrReturn"  
                     value="return from NCR" />  
            </td>
          </tr>
        </table>
      </c:when>
        <c:otherwise>
          <p>Use process tree to left to select steps for exit from traveler to NCR, 
            return from NCR to traveler</p>
       
        </c:otherwise>
    </c:choose>
  
    <table>
      <tr><td> <b>Exit step:</b> </td>
        <td>${sessionScope.exitStep}</td>
      </tr>
      <tr><td><b>Return step:</b></td>
        <td>${sessionScope.returnStep} </td>
      </tr>
      <tr><td>
      <label for="NCRCondition" > <b>Condition text:</b></label></td>
        <td><input type="text" name="NCRCondition" id="NCRCondition" 
                   value="${sessionScope.NCRCondition}"  /></td>
      </tr>
  
    
    
        <ncr:ShowNCRCandidates />
        </table>
    <p>
      <input type="submit" value="Create NCR" name="create" id="create" /> 
      <input type="submit" value="Reset" name="reset" />
    </p>
   
  </form>
    
 