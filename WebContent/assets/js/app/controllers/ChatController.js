'use strict';

define(['app'], function(app){
	var injectParams = ['$scope'];
	var ChatController = function ($scope) {

	};

	ChatController.$inject = injectParams;
	app.register.controller('ChatController', ChatController);
});
