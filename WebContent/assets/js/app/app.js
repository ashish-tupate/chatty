
define('app', ['defaultRoutes', 'routeResolver'], function(defaultRoutes){
	'use strict';
	var app = angular.module('App', [
		'ui.router',
		'ngCookies',
		'ngResource',
		'routeResolverServices'
	]);
	
	app.constant('AppConfig', {
		cookiePrefix: 'chattyApp',
		urls : {
			restUrl : 'http://localhost:8787/chatty/api/'
		},
		userLoginState: 'root.userLogin'
	});

	app.config([
		'$httpProvider',
		'$stateProvider',
		'$urlRouterProvider',
		'$locationProvider',
		'routeResolverProvider',
		'$controllerProvider',
		'$compileProvider',
		'$filterProvider',
		'$provide',
		'AppConfig',
		function($httpProvider, $stateProvider, $urlRouterProvider,
				$locationProvider, routeResolverProvider, $controllerProvider,
				$compileProvider, $filterProvider, $provide, AppConfig
		){
			$httpProvider.defaults.headers.common = {};
			$httpProvider.defaults.headers.post = {};
			$httpProvider.defaults.headers.put = {};
			$httpProvider.defaults.headers.patch = {};

			$httpProvider.defaults.headers.common['X-Requested-With'] = "XMLHttpRequest";
			$httpProvider.defaults.headers.common['Access-Control-Allow-Headers'] = "Content-Type, Access-Control-Allow-Headers";
			$httpProvider.defaults.headers.common['Accept'] = '*/*';
		//	$httpProvider.defaults.headers.post['Content-Type'] = 'application/json; charset=utf8;';
			$httpProvider.defaults.headers.common['Content-Type'] = 'application/x-www-form-urlencoded; charset=utf8';
			$httpProvider.defaults.headers.common['Cache-Control'] = 'no-cache';
			

			$httpProvider.defaults.useXDomain = true;
			$httpProvider.defaults.withCredentials = true;

			$urlRouterProvider.otherwise('/chatty/404.jsp');

			$locationProvider.html5Mode({
				enabled: true,
				requireBase: false
			});

			$locationProvider.hashPrefix('!');

			app.register = {
				controller: $controllerProvider.register,
				directive: $compileProvider.directive,
				filter: $filterProvider.register,
				factory: $provide.factory,
				service: $provide.service
			};

			var route = routeResolverProvider.route;
			angular.forEach(defaultRoutes, function (s) {
				$stateProvider.state(s.state, route.resolve(s));
			});
		}
	]);

	app.run(["$window", "$rootScope", "$state", "$location", "$http", "Utilities", "AppConfig", 'Socket', function($window, $rootScope, $state, $location, $http, Utilities, AppConfig, Socket){
		$rootScope.online = navigator.onLine;
		$rootScope.$state = $state;
		
		$rootScope.userHash = document.getElementsByTagName("body")[0].id;
		
		$rootScope.logout = function(){
			$http({
				method: "get",
				url: AppConfig.urls.restUrl + 'user/logout',
			}).success(function( data, status, headers, config ) {
				$window.location.href = $state.href('root.index', {}, {absolute: true});
			});
		};
		$rootScope.isOnline = function(){
			return !!Utilities.checkUserOnline();
		};
		
		$rootScope.getFAClass = function(status){
			return Utilities.getFAClass(status);
		};
		
		
		if($rootScope.isOnline())
		{
			$http({
				method: "get",
				url: AppConfig.urls.restUrl+'chat/list',
			}).success(function( data, status, headers, config ) {
				if(data.status == 200){
					$rootScope.rooms = data.data.groups;
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		}
		
	}]);
	
	return app;
});
