'use strict';

define(['app'], function(app){

	var injectParams = ['$rootScope', '$scope'];
	var IndexController = function ($rootScope, $scope) {

	};
	IndexController.$inject = injectParams;
	app.register.controller('IndexController', IndexController);
});