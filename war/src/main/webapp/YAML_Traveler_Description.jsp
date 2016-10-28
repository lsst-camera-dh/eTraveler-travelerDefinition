<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>  
<!DOCTYPE html>

<sql:query var="subsQ">
    select name, shortName from Subsystem order by name;
</sql:query>
<sql:query var="hwsQ">
    select name, description from HardwareStatus where isStatusValue=1 order by name;
</sql:query>
<sql:query var="pgQ">
    select name from PermissionGroup where maskBit > 0;
</sql:query>
<sql:query var="semQ">
    select name, (strcmp(name, 'signature')=0) as onlyRequired from InputSemantics;
</sql:query>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>YAML Traveler Description</title>
     <link href="http://srs.slac.stanford.edu/Commons/css/srsCommons.jsp?experimentName=LSST-CAMERA" 
           rel="stylesheet" type="text/css" />
    <link href="css/backendStyle.css" type="text/css" rel="stylesheet" /> 
  
  </head>
  <body>
    
    <div><p>This document describes the form of a YAML file providing a traveler definition and
      the keywords which may be used in it.</p>
      <h2> General Features</h2>
      <ul>
        <li><p>May start with the lines</p>
          <pre>%YAML 1.1
--- </pre>
          <p>however this is not required.  The parser used by eTraveler back-end is compliant with 
            the 1.1 specification.</p>
        </li>
        <li>
          <p>The YAML document must consist of a single top-level YAML node which describes a process.  All other
          nodes are descendants of this one.  Descendant nodes describe a descendent process, a prerequisite
          for some process, a required inut or an optional input (to be suppliced by the operator when the
          process is executed) or a relationship task.   These terms are explained in more detail below.</p>
        </li>
        <li>
          <p>Each process node is a dict; that is, a collection of key-value pairs.  Some keys take a simple
          scalar as value; others take a list.  In all cases the key is a string scalar.  In the remainder
          of this document a key will be represented by the corresponding string followed by a colon, e.g.</p>
          <pre>Name:</pre>
          <p>as this is the way it appears in the YAML file.</p>
        </li>
        <li><p>The keys come from a known collection of allowable keys.  Most of them are optional.</p></li>
        <li><p>Values for some keys will be inherited from the node's parent.</p></li> 
      </ul>
      <h2>Keys Taking a Scalar Value</h2>
      <p>Key names in <span class='redError'><b>red</b></span> are not yet fully implemented.</p>
      <dl>
        <dt>AddLabel:</dt>
        <dd>Value is label to be added to the component on which the traveler is being executed.  The
          label must have already been defined.<br /><br /></dd>
        <dt>Clone:</dt>
        <dd>Request that  copy of another node be reinserted at another location in
          the procedeure. The value of Clone: must match the value of <a href="#Name">Name:</a>
          for a previously described process step.  The step <em>and all its descendants</em>
          will be inserted. The only other keywords which may appear in a Clone: step are
          Version: (which must match the value for Version: for the step being 
          referenced) and Condition:<br /><br />
        </dd>
        <dt>Condition:</dt>
        <dd>A text string describing the condition under which the operator should select this node.
          It only has an effect if the node is a child of a <a href="#Selection">Selection</a>.<br /><br />
        </dd>
        <dt id="Description">Description:</dt>
        <dd>Complete description and instructions for the step, displayed when the traveler
          is being executed. This can be very long (up to 16k bytes). If the process step does not
          require such a long description, the <a href="#Short">ShortDescription:</a> field may suffice.  
          It's limited to 255 characters. If ShortDescription: has been specified and Description: has not, 
          the value for for ShortDescription: will be copied to Description when the file is ingested. 
        </dd> 
        <dt>HardwareGroup:</dt>
        <dd>The hardware group the traveler acts on.  The key is required for the root
          element and ignored everywhere else (its value is inherited from the root).
          <br /><br /></dd>
        <dt>MaxIteration:</dt>
        <dd>
          The number of tries allowed without special, privileged intervention.
          Defaults to 1.<br /><br />
        </dd>
        <dt id="Name">Name:</dt>
        <dd>
          The name of the process step.  This string value must not include any
          embedded blanks, must not start with a hypen, nor may it
          include any of the following characters:
          <pre>;,:?!}{]['"/$#&amp;=*^</pre>
        </dd>
        <dt>NewLocation:</dt>
        <dd>
          Location to which component is to be moved.  Must be defined in
          the database for the operator's current site or must be the 
          string '(?)' in
          which case operator will be prompted with menu of known locations.
          <br /><br />
        </dd>
        <dt>NewStatus:</dt>
        <dd>
          <p>Status to which component is to be set. Must be defined in database
          as a known status value or must be the string
          '(?)'
          in which case the operator will be prompted with a menu of all
          known status values. Status values available in the current
          database are:</p>

          <display:table name="${hwsQ.rows}" class="datatable" >
              <display:column property="name" title="Name" sortable="true"
                              headerClass="sortable" style="text-align:left" />
              <display:column property="description" title="Description"
                                style="text-align:left" />
              
          </display:table>
          
          <br /><br />

        </dd>
        <dt>RefName:</dt>
        <dd>
          The name of a process step already ingested in the database from 
          some other traveler of the same hardware group as this one.
          This step (and all descendents, as for treatment of Clone:) is,
          for all practical purposes, inserted in the new traveler.
          The only other keys which may appear in a node with RefName: are
          RefVersion: and Condition:
          <br /><br />
        </dd>
        <dt>RefVersion:</dt>
        <dd>
          Version of process step referenced here. This key is ignored unless
          RefName: is also present.  If RefName: is specified and RefVersion:
          is not, RefVersion: defaults to 'last'; the highest versioned
          process step in the data base with name = value of RefName: and 
          the same hardware group as this traveler.<br /><br />
        </dd>
        <dt>RemoveLabel:</dt>
        <dd>Value is label to be removed from the component on which the traveler is being executed.  The
          label must have already been defined (and should be associated with
          the component at the start of the step).<br /><br /></dd>
        <dt id="Short">ShortDescription:</dt>
        <dd>
          Description of process step, limited to 255 characters.  It appears in various Front-end
          tables where a short description is needed.  See also <a href='#Description'>Description:</a>.
          <br /><br />
        </dd>
        <dt>Subsystem:</dt>
        <dd>
          Subsystem to which the traveler belongs. Ignored for all steps except the
          root step. If supplied, value must match the "Short Name" of a 
          subsystem known to the database.  If not supplied, the traveler 
	  will be given an associated
          subsystem of <b>Default</b> when it is ingested. The collection
          of known subsystems is experiment- and database-dependent. 
          The following subsystems are known to the 
          currently-selected experiment and database:
          <br /><br />

          
          <display:table name="${subsQ.rows}" class="datatable" >
              <display:column property="name" title="Name" sortable="true"
                              headerClass="sortable" style="text-align:left" />
              <display:column property="shortName" title="Short Name"
                              sortable="true" headerClass="sortable" 
                              style="text-align:left" />
              
          </display:table>
          
          <br /><br />
        </dd>
        <dt>UserVersionString:</dt>
        <dd>
          User-supplied version identification required for Job Harness
          steps, so far unnecessary and ignored for other steps. See
          <a href="#TravelerActions">TravelerActions</a> below. <br /><br />
        </dd>
        <dt>Version:</dt>
        <dd>
          Version of a step.  The triple (Name, Version, HardwareGroup)
          uniquely identifies a process step (recall HardwareGroup is inherited
          from the root step of a traveler). Value defaults to '1'. It may
          be specified as a positive integer or by the string 'next', in which
          case eTraveler will assign the next available version to the step: 1
          if there are no other steps with that name and hardware group, 
          otherwise the smallest integer greater than the version of any other
          pre-existing steps of that name and hardware group.<br /><br />
        </dd>
        
      </dl>
      <h2>Keys Taking a List Value</h2>
      <dl>
        <dt id="OptionalInputs">OptionalInputs:</dt>
        <dd>
          The value for this key is a list of Input Nodes. The operator will
          see a form with a row for each optional input.  The operator need
          not fill in any such rows to complete the step. An Input Node
          is a dict with the following fields:
          <dl>
            <dt>Label:</dt>
              <dd>Required. Used to prompt operator for a sensible value </dd>
            <dt>InputSemantics: </dt>
            <dd>
              Required. Value must be one of a set of known values.
              The value 'signature' may only be used for 
              <a href="#RequiredInputs">RequiredInputs</a>.
             <display:table name="${semQ.rows}" class="datatable" >
            <display:column property="name" title="Semantic Type" 
                          sortable="true"
                          headerClass="sortable" style="text-align:left" />
            <display:column property="onlyRequired" title="RequiredInputs Only"
                   />
         </display:table>

            </dd>
            <dt>Units: </dt>
            <dd>Optional information for operator</dd>
            <dt>MinValue:, MaxValue: </dt>
            <dd>Optional for inputs of type 'int' or 'float'. If supplied in
              this case Front-end will not accept operator inputs which
              violate the limits.
            </dd>
            <dt>Description: </dt>
            <dd>Optional.  Could be useful if label is not sufficient to 
              clearly indicate what operator input should be.</dd>
          </dl>
        </dd>
        <dt><br />Prerequisites:</dt>
	<dd>The value for this key is a list of Prerequisite nodes. A Prerequisite
        node is a dict with the following fields:
        <dl>
	  <dt>PrerequisiteType:</dt>
          <dd>Required.  Value must be one of the following: PROCESS_STEP,
          TEST_EQUIPMENT, CONSUMABLE, PREPARATION.  (One more value, COMPONENT, is
          allowed but deprecated. If used its value must be the name of some
          hardware type already defined in the database.)
          </dd>
          <dt>Name:</dt> 
          <dd>Required.  If PrerequisiteType was COMPONENT, its value must be the name
          of a hardware type already defined in the database.  If TEST_EQUIPMENT, it
          <em>may</em> be a known hardware type name.  If PROCESS_STEP, it must match
          the name of another step, preferably in the same traveler.  For CONSUMABLE
          and PREPARATION, name should be descriptive, e.g. "gloves" would be a
          CONSUMABLE.  A PREPARATION step might have a name like "instructions" or
          "preliminaries".
          </dd>
          <dt>UserVersionString:</dt>
          <dd>Ignored for types other than PROCESS_STEP.  If prerequisite is a PROCESS_STEP,
          values for Name, UserVersionString must match an actual process step.</dd>
          <dt>Quantity:</dt>
          <dd>Optional; defaults to 1.  If present, must be a positive integer. Mostly
          useful for CONSUMABLEs.</dd>
          <dt>Description:</dt>
          <dd>Optional.  Especially useful for PREREQUISITE and TEST_EQUIPMENT.</dd>
        </dl>
	</dd>	
    
        <dt id="PermissionGroups" name="PermissionGroups"><br />PermissionGroups:</dt>
	<dd>Optional.  A list of permission roles whose members may execute
            the step.  If omitted, defaults to all known permission groups
            associated with the traveler's subsystem.  The roles
            known to eTraveler for the current database which may appear 
         here are
         <display:table name="${pgQ.rows}" class="datatable" >
            <display:column property="name" title="Role" sortable="true"
                            headerClass="sortable" style="text-align:left" />
         </display:table>
        <dt><br />RelationshipTasks:</dt>
        <dd>
	The value for this key is a list of RelationshipTask nodes.  Each such node
        is a dict with three fields.  The first two are required, the third is optional.
        <dl>
           <dt>RelationshipName:</dt>
           <dd>Must match the name of a relationship type previously defined in the
             database.</dd>
           <dt>RelationshipAction:</dt>
           <dd>One of a known, enumerated set.  Currently that set includes 'assign',
            'install', 'deassign' and 'uninstall'.           
           </dd>
           <dt>RelationshipSlot:</dt>
           <dd>May be one of the slot names belonging to the relationship specified by RelationshipName, 
               or the string 'ALL'. <%-- or the string '(?)', in which case the operator will be presented with
               a menu of possibilities.--%> If not supplied, defaults to 'ALL' (old behavior). If there only
               is one slot and it is specified by name in the YAML file, that specification will be changed
               to 'ALL' when the file is ingested.
           </dd>	   
        </dl>
        </dd>
        <dt name="RequiredInputs" id="RequiredInputs" ><br />RequiredInputs:</dt>
        <dd>
          The value for this key is a list of Input Nodes. The operator will
          see a form with a row for each optional input.  The operator <em>must</em>
          fill in any such rows to complete the step.  See 
          <a href="#OptionalInputs">OptionalInputs:</a> above for a detailed
          description of Input Nodes which applies to both RequiredInputs:
	  and OptionalInputs:. 
         <p>A required input whose semantic type is 'signature' may
           have one additional subitem <b>Role:</b> whose value must
           either be '(?)' or must come from the same set of values 
           which can be used with the 
           <a href="#PermissionGroups">PermissionGroups:</a> key,
           If the Role: keyword is omitted, its value will, by default,
           be '(?)'; that is, the signatures required will
           be determined dynamically when the traveler is run.</p>
        </dd>
        <dt id="Selection"><br />Selection:</dt>
        <dd>Indicates that the process step has children, one of which
          is to be executed as selected by the Operator at execution time.
          The value of this key is a list of process nodes.
        </dd>
        <dt><br />Sequence:</dt>
        <dd>Indicates that the process step has children which are to be 
          executed in sequence.  Value is a list of process nodes.</dd>
        <dt id="TravelerActions"><br />TravelerActions:</dt>
	<dd>Value is a list, taken from a predefined set of properties in the database.
         Commonly-used properties include HarnessedJob and Automatable. Other known
         values will be supplied automatically when corresponding scalar keys are
         used.  These include SetHardwareStatus (NewStatus:), 
         SetHardwareLocation (NewLocation:), 
         AddLabel (AddLabel:) and 
         RemoveLabel (RemoveLabel:)
         If any of these appear under TravelerActions: without
         the corresponding key, at execution time the Operator will be prompted with
         a menu of suitable possibilities. 
        </dd>
      </dl>
      <h2 id='ref_clone'>Referenced and Cloned Steps</h2>
      <p>In addition to 'regular' process steps, one may also refer to existing
          steps.  There are two different methods, depending on whether you
          want to refer to a step already defined in the current traveler or
          one defined in another, already-ingested traveler. In either case most
          of the regular contents of a step are inappropriate and will either
          cause an error if present or will be ignored.  Definitions for
      cloned or reference step do not include child steps (nor Prerequistes,
          RequiredResults, etc.) because these things have already been
          defined in the step being cloned or referred to. </p>
      <ul><li>
              For cloned steps, the only keywords which may be present
              are Clone: (required) and Version: (optional).  The value
              must be the name of a step defined above in the same traveler.
              If the keyword Version: is present, the associated value
              must be 'cloned' (with or without quotes).
          </li>
          <li>
              For reference steps, use the keyword RefName: whose value must
              match the name of a step in some already-ingested traveler for
              the same hardware group.   The keyword RefVersion: is optional.
              Its default value is 'last'; that is, refer to the step in
              the db with correct name and hardware group of the highest 
              version number.  You may also specify a positive integer value.
              <b>WARNING:</b> When travelers using a reference are exported
                the fact that the step was originally a reference will not
                be preserved.
          </li>
      </ul>
      <h3>Restrictions</h3>
      <p>Within a traveler, all steps with the same name must also have the
      same version.  This implies certain restrictions on regular steps,
      cloned steps and reference steps, namely</p>
      <ul>
          <li>There may be only one 'regular' step with a given name 
              (value of keyword Name:) in a
              traveler definition.</li>
          <li>For any clone step, the value of Clone: must be a string 
              which has already appeared
              as the value of Name: for some prior regular step. </li>
          <li>If there is a ref step (keyword RefName:) the value of RefName:
              must be different from the names of any regular steps or clones.
          </li>
          <li>
              If there are two steps using the keyword RefName: with the same
              value, they must also have the same value for RefVersion: (or
              they can both omit RefVersion: so the value defaults to 'last')
          </li>
      </ul>
      <%-- Sections to perhaps be added later
     
      <h2 id='Examples' >Examples</h2>
       --%>
    </div>
    <div>
      <h3 id='References'>References</h3>
      <ol>
        <li><a href="http://yaml.org/spec/1.1/">YAML 1.1 Specification</a></li>
        <li><a href="https://confluence.slac.stanford.edu/x/3gViCQ">eTraveler 
            Definition User's Manual</a>
        </li>
      </ol>
    </div>
  </body>
</html>
