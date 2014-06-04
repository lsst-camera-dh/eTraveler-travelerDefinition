<%-- 
    Document   : displayProcessFragment
    Created on : Jun 4, 2014, 11:26:07 AM
    Author     : jrb
    Purpose    : To be include in processAction.jsp
--%>

      <h3>Process Attributes</h3>
         <display:table name="${import:selectedNodeAttributes(pageContext)}" 
                                class="datatable" >
           <display:column property="name" title="Attribute" 
                           headerClass="sortable" style="text-align:left" />
           <display:column property="value" />
         </display:table>
      <c:if test="${import:getPrerequisiteCount(pageContext) > 0}">
        <h3>Prerequisites</h3>
   
        <display:table name="${import:getPrerequisites(pageContext)}"
                       class="datatable" >
          <display:column property="name" title="Name" headerClass="sortable"
                          style="text-align:left"/>
          <display:column property="type" title="Type" style="text-align:left"/>
          <display:column property="quantity" title="Quant" style="text-align:right" />
          <display:column property="description" title="Descrip" style="text-align:left" />

          <display:column property="version" style="text-align:right" />
          <display:column property="userVersionString" title="User version"
                          style="text-align:left" />
        </display:table>
      </c:if>
      <c:if test="${import:getResultCount(pageContext) > 0}">
        <h3>Prescribed Operator Inputs</h3>
        <display:table name="${import:getResults(pageContext)}"
                       class="datatable" >
          <display:column property="label" title="Label" headerClass="sortable"
                          style="text-align:left"/>
          <display:column property="semantics" title="Type" style="text-align:left"/>
          <display:column property="description" title="Descrip" style="text-align:left" />
          <display:column property="units" style="text-align:left" />
          <display:column property="minValue" title="Min" style="text-align:right" />
          <display:column property="maxValue" title="Max" style="text-align:right" />
        </display:table>
      </c:if>


