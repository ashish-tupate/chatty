<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:genericpage>
    <jsp:attribute name="head">
    	  
    </jsp:attribute>
    <jsp:body>
		<div ui-view="content"></div>
    </jsp:body>
</t:genericpage>