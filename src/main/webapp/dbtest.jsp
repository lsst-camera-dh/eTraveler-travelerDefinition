<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="db" uri="http://lsstcorp.org/etravelerbackenddbwar/DbTest" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello from DbTest!</h1>

<p>Calling DbTest main..  </p>
<p>
          ${db:mainFromJsp()}
</p>

</body>
</html>
