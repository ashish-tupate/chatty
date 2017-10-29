
window.contextPath = '/chatty/';

requirejs.config({
	baseUrl: '/chatty/assets/js/',
//	urlArgs: "v=" + 1483394439455,
	paths: {
		jquery: 'libs/jquery-2.2.1.min',
		angular: 'libs/angular.min',
		ngCookies: 'libs/angular-cookies.min',
		ngResource: 'libs/angular-resource.min',
		ngSanitize: 'libs/sanitize',
		uiRouter: 'libs/angular-ui-router.min',
		bootstrap: 'libs/bootstrap.min',
		underscore: 'libs/underscore-min',
		ngDialogPlugin: 'libs/ngDialog.min',
		app: 'app/app',
		Socket: 'app/service/socket',
		Utilities: 'app/service/utilities',
		routeResolver: 'app/service/routeResolver',
		Directive: 'app/directive/directive',
		defaultRoutes: 'app/routes/default',
		custom: 'custom'
	},
	shim: {
		jquery: {
			exports: 'jQuery'
		},
		transform2d: {
			exports: "transform2d",
			deps: ['jquery']
		},
		custom: {
			exports: "custom",
			deps: ['jquery']
		},
		underscore: {
			exports: "underscore"
		},
		angular: {
			exports: "angular",
			deps: ['jquery']
		},
		bootstrap: ['jquery'],
		uiRouter: ['angular'],
		ngCookies: ['angular'],
		ngSanitize: ['angular'],
		ngResource: ['angular'],
		Directive: ['angular'],
		ngDialogPlugin: { 
			exports: 'angular'
		},
		
		app: {
			deps: ['angular'],
			exports: 'app'
		},
		Utilities: ['app'],
		Socket: ['app'],

	},
	waitSeconds: 30
});

requirejs([
	'angular',
	'ngCookies',
	'ngResource',
//	'ngSanitize',
	'ngDialogPlugin',
	'uiRouter',
	'bootstrap',
	'Directive',
	'defaultRoutes',
	'app',
	'Socket',
	'Utilities',
	'jquery',
	'custom'
], function(){
	requirejs(['app'], function () {
		angular.bootstrap(document.getElementsByClassName("chattyApp"), ['App']);
	});
});