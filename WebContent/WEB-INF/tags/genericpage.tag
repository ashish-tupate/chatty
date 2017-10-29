<%@ tag language="java" pageEncoding="UTF-8"%>
<%@attribute name="head" fragment="true" %>
<%@attribute name="header" fragment="true" %>
<%@attribute name="footer" fragment="true" %>
<!DOCTYPE html>
<html class="chattyApp">
	<head>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
		<meta content="yes" name="apple-mobile-web-app-capable">
		<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
		<meta name="robots" content="noindex" />
		<title>Chatty Application</title>
		<link rel="stylesheet" href="/chatty/assets/bootstrap/css/bootstrap.min.css">
		<link rel="stylesheet" href="/chatty/assets/css/font-awesome.min.css">
		<link rel="stylesheet" href="/chatty/assets/css/style.css">
		<script data-main="/chatty/assets/js/main" src="/chatty/assets/js/libs/require.js"></script>
		<jsp:invoke fragment="head"/>
	</head>
	<body id="${sessionScope.userHash}" class="{{bodyClass ? bodyClass: ''}}">
	
		<div class="container">
			<div id="pageheader">
				<jsp:invoke fragment="header"/>
			</div>
			<div id="body">
				<jsp:doBody/>
			</div>
			<div id="pagefooter">
				<jsp:invoke fragment="footer"/>
			</div>
		</div>
		<messenger ng-if="isOnline()"></messenger>
	</body>
</html>