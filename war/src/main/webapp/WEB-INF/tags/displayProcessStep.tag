<%-- 
    Document   : displayProcessStep
    Created on : July 2, 2014, 5:17:07 PM
    Author     : jrb
    Purpose    : To be used in processAction.jsp and anywhere else that
                 process step needs to be displayed
--%>

<link href="//srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimentName=${appVariables.experiment}" rel="stylesheet" type="text/css">



<%@tag description="display attributes of process step, assoc. prereq, results"
       pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@taglib prefix="import" 
          uri="http://etraveler.camera.lsst.org/backend/DbImporter" %>

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
          <display:column property="name" title="Name"
                          sortable="true" style="text-align:left"/>
          <display:column property="semantics" title="Type" style="text-align:left"/>
          <display:column property="description" title="Descrip" style="text-align:left" />
          <display:column property="units" style="text-align:left" />
          <display:column property="minValue" title="Min" style="text-align:right" />
          <display:column property="maxValue" title="Max" style="text-align:right" />
          <display:column property="role" title="Role" style="text-align:left" />
        </display:table>
      </c:if>
      <c:if test="${import:getOptionalResultCount(pageContext) > 0}">
        <h3>Optional Operator Inputs</h3>
        <display:table name="${import:getOptionalResults(pageContext)}"
                       class="datatable" uid="result" >
          <display:column property="label" title="Label" 
                          sortable="true" style="text-align:left"/>
          <display:column property="name" title="Name"
                          sortable="true" style="text-align:left"/>
          <display:column property="semantics" title="Type" style="text-align:left"/>
          <display:column property="description" title="Descrip" style="text-align:left" />
          <display:column property="units" style="text-align:left" />
          <display:column property="minValue" title="Min" style="text-align:right" />
          <display:column property="maxValue" title="Max" style="text-align:right" />
        </display:table>
      </c:if>

       <c:if test="${import:getRelationshipTaskCount(pageContext) > 0}">
        <h3>Hardware Relationships</h3>
        <display:table name="${import:getRelationshipTasks(pageContext)}"
                       class="datatable" uid="relationship" >
          <display:column property="name" title="Name" 
                          sortable="true" style="text-align:left"/>
          <display:column property="action" title="Action" style="text-align:left"/>
          <display:column property="slot" title="Slot descriptor" style="text-align:left"/>
        </display:table>
      </c:if>


