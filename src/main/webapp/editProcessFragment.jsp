<%-- 
    Document   : editProcessFragment
    Created on : Jun 4, 2014, 11:31:02 AM
    Author     : jrb
    Purpose    : Handles editing of process step. To be included in 
                 processAction.jsp
--%>

<script type="text/javascript">
  <!--
      function isInt(id) {
        node = document.getElementById(id);
        if (Math.floor(node.value) != node.value) {
          alert("Not an integer!");
          window.setTimeout("node.focus()", 100);
         } else return true;}
      function isPosInt(id) {
        node = document.getElementById(id);
        if  (!(Math.floor(node.value) == node.value) ) {
          alert("Value must be an integer!");
          window.setTimeout("node.focus()", 100);
        } else if (node.value <= 0) {
          alert("Value must be a positive integer!");
          window.setTimeout("node.focus()", 100);
        } else return true;
      }
      function isNumber(id) {
        node = document.getElementById(id);
        if ( (node.value == null) || (Math.abs(node.value) != Math.abs(node.value) ) ) {
          alert("Supplied value is not a number!");
          window.setTimeout("node.focus(), 100");
        } 
        else {
          return true;
        }
      }
     
  // -->    
</script>
     <% 
      AttributeList attsj = imp.selectedNodeAttributes(pageContext);
      String name="", oldVersion="", htype="", childType="", maxIt="", nSub="";
      String nPrereq="", nResult="";
      String description="", instructionsURL="", actionMask="";
      String userVersionString=null;
      String hrtype=null;
      
      for (int i = 0; i < attsj.size(); i++) {
        Attribute att = (Attribute) attsj.get(i);
        String val = att.getValue().toString();
        switch (att.getName()) {
          case "name": 
            name = val; 
            break;
          case "version" :
            oldVersion = val; break;
          case "hardware type":
            htype = val; break;
          case "child type":
            childType = val; break;
          case "description":
            description = val; break;
          case "instructions URL":
            instructionsURL=val; break;
          case "max iterations":
            maxIt = val; break;
          case "traveler action mask":
            actionMask = val; break;
          case "# substeps":
            nSub = val; break;
          case "# prerequisites":
            nPrereq = val; break;
         case "# solicited results":
            nResult = val; break;
         case "user version string":
            userVersionString = val; break;
         case "hardware relationship type":
            hrtype = val; break;                                                  
        }
      } 
      ArrayList<Prerequisite> prereqs =  imp.getPrerequisites(pageContext);
      boolean haveProcessStep = false;
      if (prereqs != null) {
        for (Prerequisite p: prereqs) {
          if (p.getType().equals("PROCESS_STEP")) { haveProcessStep = true;}
        } 
      }                          
      ArrayList<PrescribedResult> results = imp.getResults(pageContext);
      boolean haveNumeric = false;
      if (results != null) {
        for (PrescribedResult r: results) {
          String sem = r.getSemantics();
          if ((sem.equals("int")) || (sem.equals("float")) ) {haveNumeric = true;}
        }
      }
      %>
   
      <form action="updateStep.jsp" id="updateForm" name="updateForm" >
        <table cellpadding="2">
          <tbody>
          <tr><td class="bold">Name</td><td><%= name %></td></tr>
          <tr><td class="bold">Original version</td><td><%= oldVersion %></td></tr>
          <tr><td class="bold">Hardware type</td><td><%= htype %></td></tr>
          <tr><td class="bold">Child type</td><td><%= childType %></td></tr>
          <tr><td class="bold"><label for="description">Description</label></td>
            <td><input name="description" id="description" size="50"
                       value="<%= description %>" /></td></tr>
           <tr><td class="bold"><label for="instructionsURL">Instructions URL</label></td>
            <td><input name="instructionsURL" id="instructionsURL" size="50"
                       value="<%= instructionsURL %>" /></td></tr>       
           <tr><td class="bold"><label for="maxIt">Max iteration</label></td>
            <td><input name="maxIt" id="maxIt" size="3"
                       value="<%= maxIt %>" onblur='isPosInt("maxIt")' /></td></tr>
          </tbody>
        </table>
        <input type="hidden" name="name" id="name" value="<%= name %>" />
        <input type="hidden" name="oldVersion" id="oldVersion" 
               value="<%= oldVersion %>" />
        <input type="hidden" name="htype" id="htype" value="<%= htype %>" />
        <input type="hidden" name="childType" id="childType" 
               value="<%= childType %>" />
        <%!         
          String nodestring;
          String idstring;
          int    ix = 0;
          
          String genId(String nm) {
            return  nm+ "_" + Integer.toString(ix);
          }
          String genNode(String nm) {
            return "document.updateForm." + genId(nm);                                  
          }
         %>
        
        
        <% 
          if (prereqs != null) { 
            ix = 0;
        %>
          <fieldset>
          <legend>Prerequisites</legend>
           <table cellpadding="2"  class="datatable" border="0"> 
             <thead>
               <tr><th bgcolor="red">DELETE</th><th align="left">Name</th>
                 <th align="left">Type</th><th align="left">Count</th>
                 <th align="left">Descrip</th>
                 <% if (haveProcessStep) { %>
                 <th align="left">Version</th><th align="left">User_version</th></tr>
                 <% } %>
             </thead>
           <tbody>
            <%! 
            
              String rowclass="odd";
              String warning="grey"; 
 
            %>    
            <%-- 
                
                return "document.getElementById('" + genId(nm) + "')"; 
            --%>
            <%                                                   
              for (Prerequisite prereq: prereqs) {                                                                                                      
                  %>
               <tr class="<%= rowclass %>">
                 <td bgcolor="<%= warning %>">
                   <input type="checkbox" name='<%= genId("remove") %>' 
                          id='<%= genId("remove") %>' /></td> 
                 <td class="left"><%= prereq.getName() %></td>
                 <td class="left"><%= prereq.getType() %></td>
                 <td>
                   <input type="number" min="1" step="1" name='<%= genId("count") %>' 
                          id='<%= genId("count") %>'
                          size="3" value="<%= prereq.getQuantity() %>"
                          <% idstring = genId("count"); %>
                          onblur='isPosInt("<%= idstring %>")' /></td>
                 <td>
                   <input type="text" name='<%= genId("prereqDescrip") %>' 
                          id='<%= genId("prereqDescrip") %>'  
                          value="<%= prereq.getDescription() %>" /></td>
                 <% if (haveProcessStep) { %>
                 <td>    <%= prereq.getVersion() %></td>
                 <td>
                   <input type="text" name='<%= genId("userVersion") %>' 
                          id='<%= genId("userVersion") %>'  size="5"
                          value="<%= prereq.getUserVersionString() %>" /></td>
                  <% } %>
               </tr>
               <% ix++;  
                 if (rowclass.equals("even")) {
                 rowclass="odd";  warning="grey"; }
                 else { rowclass="even"; warning="black"; }
                 } %>        
             <tbody></table></fieldset>
               <% } %>
               
             <% 
          if (results != null) {  %>
          <fieldset>
          <legend>Required operator inputs</legend>
           <table cellpadding="2"  class="datatable" border="0"> 
             <thead>
               <tr><th bgcolor="red">DELETE</th><th align="left">Label</th>
                 <th align="left">Type</th>
                 <th align="left">Descrip</th>
                 <% if (haveNumeric) { %>
                   <th align="left">Units</th>
                   <th align="left">Min</th><th align="left">Max</th></tr>
                 <% } %>
             </thead>
           <tbody>
            <%  
               rowclass="odd";
               warning="grey";  
               ix = 0;                                          
               for (PrescribedResult result: results) {               
            %>
               <tr class="<%= rowclass %>">
                 <td bgcolor="<%= warning %>"><input type="checkbox" 
                     name='<%= genId("removeResult") %>'
                     id='<%= genId("removeResult") %>' /></td> 
                 <td class="left"><%= result.getLabel() %></td>
                 <td class="left"><%= result.getSemantics() %></td>
                 <td>
                   <input type="text"
                          name='<%= genId("resultDescrip") %>'
                          id='<%= genId("resultDescrip") %>'
                            value="<%= result.getDescription() %>" /></td>
                 <% if (haveNumeric) { 
                   
                 %>
                 <td class="left">
                   <input type="text" name='<%= genId("units") %>' 
      
                          id='<%= genId("units") %>' size="6"
                            value="<%= result.getUnits() %>" 
                            /></td>
                 <td><input type="text" 
                          <% idstring = genId("min"); %>
                            name='<%= idstring %>' 
                            id='<%= idstring %>' size="4"
                           value="<%= result.getMinValue() %>" 
                           onblur="isNumber('<%= idstring %>')"  /></td>
                 <td><input type="text"  <% idstring = genId("max"); %>
                            name='<%= idstring %>' 
                            id='<%= idstring %>' size="4"
                           value="<%= result.getMaxValue() %>" 
                           onblur="isNumber('<%= idstring %>')" /></td>
                 <% } else { %>
                 <td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
                  <% } %>
               </tr>
               <%   if (rowclass.equals("even")) {
                 rowclass="odd";  warning="grey"; }
                 else { rowclass="even"; warning="black"; }            
                ix++;
                 }
                %>  
             <tbody></table></fieldset>       
           <% } %> 
        <p>
        <input type="submit" value="Save edit" />
        <input type="reset"  value="Reset form" />
        </p>
      </form>