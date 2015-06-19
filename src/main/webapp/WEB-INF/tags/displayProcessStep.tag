<%-- 
    Document   : displayProcessStep
    Created on : July 2, 2014, 5:17:07 PM
    Author     : jrb
    Purpose    : To be used in processAction.jsp and anywhere else that
                 process step needs to be displayed
--%>

<link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimentName=${appVariables.experiment}" rel="stylesheet" type="text/css">



<%@tag description="display attributes of process step, assoc. prereq, results"
       pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@taglib prefix="import" 
          uri="http://lsstcorp.org/etravelerbackend/DbImporter" %>


      <h3>Process Attributes</h3>
         <display:table name="${import:selectedNodeAttributes(pageContext)}" 
                                class="datatable" uid="process">
           <display:column property="name" title="Attribute" 
                          sortable="true" style="text-align:left" />
           <display:column property="value" style="text-align:left" />
         </display:table>
      <c:if test="${import:getPrerequisiteCount(pageContext) > 0}">
        <h3>Prerequisites</h3>
   
        <display:table name="${import:getPrerequisites(pageContext)}"
                       class="datatable" uid="prereq" >
          <display:column property="name" title="Name" 
                          sortable="true" style="text-align:left"/>
          <display:column property="type" title="Type" style="text-align:left"/>
          <display:column property="quantity" title="Quant" style="text-align:right" />
          <display:column property="description" title="Descrip" 
                          style="text-align:left" />

          <display:column property="version" style="text-align:right" />
          <display:column property="userVersionString" title="User version"
                          style="text-align:left" />
        </display:table>
      </c:if>
      <c:if test="${import:getResultCount(pageContext) > 0}">
        <h3>Prescribed Operator Inputs</h3>
        <display:table name="${import:getResults(pageContext)}"
                       class="datatable" uid="result" >
          <display:column property="label" title="Label" 
                          sortable="true" style="text-align:left"/>
          <display:column property="semantics" title="Type" style="text-align:left"/>
          <display:column property="description" title="Descrip" style="text-align:left" />
          <display:column property="units" style="text-align:left" />
          <display:column property="minValue" title="Min" style="text-align:right" />
          <display:column property="maxValue" title="Max" style="text-align:right" />
          <display:column property="isOptional" title="Optional" style="text-align:left" />
        </display:table>
      </c:if>


