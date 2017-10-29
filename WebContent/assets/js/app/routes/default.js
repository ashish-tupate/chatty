'use strict';

define('defaultRoutes', function() {
	var contextPath = '/chatty/';
	return [
		{
			state: 'root',
			url: '',
			abstract: true,
			controller: 'RootController',
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/index.html',
					controller: 'IndexController'
				}
			}
		},
		{
			state: 'root.root',
			url: contextPath,
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/index.html',
					controller: 'IndexController'
				}
			}
		},
		{
			state: 'root.index',
			url: contextPath + 'index.jsp',
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/index.html',
					controller: 'IndexController'
				}
			}
		},

		/*----------------------------------------
			User states
		----------------------------------------*/
		{
			state: 'root.user',
			url: contextPath + 'user/index.jsp',
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/index.html',
					controller: 'UserController'
				}
			}
		},
		{
			state: 'root.userView',
			url: contextPath + 'user/view.jsp?hash',
			params: {
				hash: null
			},
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/view.html',
					controller: 'UserController'
				}
			}
		},
		{
			state: 'root.userLogin',
			url: contextPath+'user/login.jsp?:email',
			params: {
				email: ''
			},
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/login.html',
					controller: 'UserController'
				}
			}
		},
		{
			state: 'root.userLogout',
			url: contextPath + 'user/logout.jsp',
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/login.html',
					controller: 'UserController'
				}
			}
		},
		{
			state: 'root.userRegister',
			url: contextPath + 'user/register.jsp',
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/register.html',
					controller: 'UserController'
				}
			}
		},
		{
			state: 'root.userForgotPassword',
			url: contextPath + 'user/forgot-password.jsp',
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/forgot-password.html',
					controller: 'UserController'
				}
			}
		},
		{
			state: 'root.userChangePassword',
			url: contextPath + 'user/change-password.jsp',
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/change-password.html',
					controller: 'UserController'
				}
			}
		},
		{
			state: 'root.userUpdateProfile',
			url: contextPath + 'user/update-profile.jsp',
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/update-profile.html',
					controller: 'UserController',
				}
			}
		},
		{
			state: 'root.userActivation',
			url: contextPath + 'user/activation.jsp?hash',
			params: {
				hash: ''
			},
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/user/activation.html',
					controller: 'UserActivationController',
				}
			}
		},
		{
			state: 'root.chat',
			url: contextPath + 'chat.jsp?group',
			params: {
				group: ''
			},
			views: {
				'content@': {
					templateUrl: contextPath + 'partials/templates/chat.html',
					controller: 'ChatController',
				}
			}
		}
	];
});