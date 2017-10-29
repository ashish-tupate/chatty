'use strict';

define(['app', 'underscore'], function(app, _){
	var injectParams = ['$rootScope', '$scope', '$state','$http', '$location', 'AppConfig'];
	var UserActivationController = function ($rootScope, $scope, $state, $http, $location, AppConfig) {
		$scope.activationForm = {};

		$scope.userActivation = function(){
			$rootScope.isLoading = true;
			$scope.formError = {};
			$http({
				method: "get",
				url: AppConfig.urls.restUrl+'user/activation',
				params: $scope.activationForm,
			}).success(function( data, status, headers, config ) {
				$rootScope.isLoading = false;
				if(data.status == 200){
					$state.go('root.userLogin');
				}
				else if (data.status == 400){
					$scope.formError = data.error;
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		};
		
		var queryString = $location.search();
		if(_.has(queryString, 'hash')){
			$scope.activationForm.hash = queryString.hash;
			$scope.userActivation();
		}
	};
	UserActivationController.$inject = injectParams;
	app.register.controller('UserActivationController', UserActivationController);
});