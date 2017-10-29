'use strict';

define(['app'], function(app){

	var injectParams = ['$rootScope'];
	var RootController = function ($rootScope) {

	};
	RootController.$inject = injectParams;
	app.register.controller('RootController', RootController);
});