'use strict';

define(['app', 'ngCookies', 'ngSanitize'], function (app) {
	app.service('Utilities', ['$rootScope', 'AppConfig', '$cookies', '$sce', '$http', '$window', '$state', function ($rootScope, AppConfig, $cookies, $sce, $http, $window, $state) {
		return {
			getCookie: function(name){
				return $cookies.get(AppConfig.cookiePrefix + name);
			},
			setCookie: function(name, value){
				$cookies.put(AppConfig.cookiePrefix + name, value);
			},
			deleteCookie: function(name){
				$cookies.remove(AppConfig.cookiePrefix + name);
				$cookies.remove(AppConfig.cookiePrefix + name);
			},
			searchQueryDelay: (function(){
				var timer = 0;
				return function (callback, ms) {
					clearTimeout(timer);
					timer = setTimeout(callback, ms);
				}
			})(),
			trustHTML: function(html){
				return $sce.trustAsHtml(html);
			},
			checkUserOnline: function(){
				return $cookies.get('rememberMe');
			},
			unauthorizedCallback: function (){
				$window.location.href = $state.href('root.userLogin', {}, {absolute: true});
			},
			goProfile: function(userHash){
				$state.go('root.userView', {hash:userHash});
			},
			getFAClass: function(text){
				var faClass = '';
				switch (text) {
				case 'add':
					faClass = 'fa-plus';
					break;
				case 'delete':
					faClass = 'fa-minus';
					break;
				case 'deny':
					faClass = 'fa-remove';
					break;
				case 'approve':
					faClass = 'fa-check';
					break;
				case 'cancel':
					faClass = 'fa-reply';
					break;
				}
				return faClass;
			}
		};
	}]);
});