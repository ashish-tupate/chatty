define(['app', 'underscore'], function(app, _){
	var injectParams = ['$window', '$rootScope', '$scope', '$state', 'AppConfig', '$http', '$location', 'Utilities', '$httpParamSerializerJQLike','Socket'];
	var UserController = function ($window, $rootScope, $scope, $state, AppConfig, $http, $location, Utilities, $httpParamSerializerJQLike, Socket) {
		console.log(123123123);
		$scope.userLogin = function(){
			$rootScope.isLoading = true;
			$http({
				method: "post",
				url: AppConfig.urls.restUrl+'user/login',
				data: $httpParamSerializerJQLike($scope.loginForm)
			}).success(function( data, status, headers, config ) {
				$scope.formError = {};
				$rootScope.isLoading = false;
				if(data.status == 200){
					//$state.go('root.user', {}, {reload:true});
					$window.location.href = $state.href('root.user', {}, {absolute: true});
				}
				else if(data.status == 400){
					$scope.formError = data.error;
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		};

		$scope.forgotPassword = function(form){
			$scope.formError = {};
			$rootScope.isLoading = true;
			$http({
				method: "get",
				url: AppConfig.urls.restUrl + 'user/password',
				params: $scope.forgotPasswordForm
			}).success(function( data, status, headers, config ) {
				$rootScope.isLoading = false;
				if(data.status == 200){
					$state.go('root.userLogin', {email:$scope.forgotPasswordForm.email});
				}
				else if(data.status == 400){
					$scope.formError = data.error;
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		};

		$scope.changePassword = function(form){
			$scope.formError = {};
			$rootScope.isLoading = true;
			$http({
				method: "post",
				url: AppConfig.urls.restUrl + 'user/password',
				data: $httpParamSerializerJQLike($scope.changePasswordForm)
			}).success(function( data, status, headers, config ) {
				$rootScope.isLoading = false;
				if(data.status == 200){
					$state.go('root.user');
				}
				else if(data.status == 400){
					$scope.formError = data.error;
				}
				else if(data.status == 401){
					Utilities.unauthorizedCallback();
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		};

		$scope.getProfile = function(userHash){
			console.log(userHash, 111);
			$http({
				method: "get",
				url: AppConfig.urls.restUrl+'user/profile?hash='+(userHash ? userHash : ''),
			}).success(function( data, status, headers, config ) {
				$scope.formError = {};
				if(data.status == 200){
					$scope.userProfileForm = data.data.profile;
				}
				else if(data.status == 400){
					$scope.formError = data.error;
				}
				else if(data.status == 401){
					Utilities.unauthorizedCallback();
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		};
		
		$scope.getUsers = function(form){
			$http({
				method: "get",
				url: AppConfig.urls.restUrl+'users',
			}).success(function( data, status, headers, config ) {
				$scope.formError = {};
				if(data.status == 200){
					$rootScope.users = data.data.users;
				}
				else if(data.status == 400){
					$scope.formError = data.error;
				}
				else if(data.status == 401){
					Utilities.unauthorizedCallback();
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		};

		$scope.updateProfile = function(form){
			$scope.formError = {};
			$rootScope.isLoading = true;
			$http({
				method: "post",
				url: AppConfig.urls.restUrl + 'user/update',
				data: $httpParamSerializerJQLike($scope.userProfileForm)
			}).success(function( data, status, headers, config ) {
				$rootScope.isLoading = false;
				if(data.status == 200){
					$state.go('root.user');
				}
				else if(data.status == 400){
					$scope.formError = data.error;
				}
				else if(data.status == 401){
					Utilities.unauthorizedCallback();
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		};
		
		$scope.setFriendship = function(userHash, status) {
			Socket.emit("friend:set", {userHash:userHash, status:status});
		}
		
		$scope.insertGroup = function(name) {
			Socket.emit("group:insert", {name:name});
		}
		
		$scope.updateGroup = function(hash, name) {
			Socket.emit("group:update", {hash:hash, name:name});
		}
		
		$scope.delteGroup = function(hash) {
			Socket.emit("group:delete", {hash:hash});
		}
		
		$scope.insertUserToGroup = function(groupHash, userHash) {
			Socket.emit("group:member:insert", {groupHash:groupHash, userHash:userHash});
		}
		
		$scope.deleteUserToGroup = function(groupHash, userHash) {
			Socket.emit("group:member:delete", {groupHash:groupHash, userHash:userHash});
		}
		
		$scope.registerForm = {gender:'male'};
		$scope.userRegister = function(){
			$rootScope.isLoading = true;
			$scope.formError = {};
			$http({
				method: "post",
				url: AppConfig.urls.restUrl+'user/register',
				data: $httpParamSerializerJQLike($scope.registerForm)
			}).success(function( data, status, headers, config ) {
				$scope.formError = {};
				$rootScope.isLoading = false;
				if(data.status == 200){
					$state.go('root.userActivation');
				}
				else if (data.status == 400){
					$scope.formError = data.error;
				}
			}).error(function( data, status, headers, config ){
				console.log(data);
			});
		};

		$scope.init = function(){
			console.log("sdfgg");
			// run controller's init by url for default values
			if($state.current.name == 'root.userLogin'){
				if(Utilities.checkUserOnline())
				{
					$state.go('root.userUpdateProfile');
				}
				$scope.loginForm = {};
				var queryString = $location.search();
				if(_.has(queryString, 'email') && queryString.email){
					$scope.loginForm.email = queryString.email;
				}
			}
			else if($state.current.name == 'root.userUpdateProfile'){
				console.log(12313);
				$scope.getProfile();
			}
			else if($state.current.name == 'root.userSetting'){
				$scope.getSetting();
			}
			else if($state.current.name == 'root.userLogout'){
				$scope.userLogout();
			}
			else if($state.current.name == 'root.user'){
				$scope.getUsers();
			}
			else if($state.current.name == 'root.userView'){
				var queryString = $location.search();
				var userHash = '';
				if(_.has(queryString, 'hash') && queryString.hash){
					userHash = queryString.hash;
				}
				$scope.getProfile(userHash);
			}
		};

		$scope.init();
	};

	UserController.$inject = injectParams;
	app.register.controller('UserController', UserController);
});
