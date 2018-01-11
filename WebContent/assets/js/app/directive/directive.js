'use strict';

define(['app', 'jquery', 'underscore'], function(app, $, _) {
	
	app.directive('messenger', ["$http", "AppConfig", "$state", "$location", "Socket", '$sce', '$compile', function($http, AppConfig, $state, $location, Socket, $sce, $compile) {
		return {
			restrict: "E",
			replace: true,
			templateUrl: window.contextPath + "partials/templates/messenger.html",
			link : function(scope, element, attrs, controller) {
				element.find('.create-group-btn').on('click', function(){
					element.find('[name=group]').removeClass('d-none');
				});
				
				scope.createGroupName = '';
				element.find('[ng-model="createGroupName"]').bind("keydown keypress", function (event) {
		            if(event.which === 13) {
		                scope.$apply(function (){
							scope.createGroupName = scope.createGroupName.trim();
							if(scope.createGroupName != '')
							{
								Socket.emit('group:insert', {name:scope.createGroupName});
								element.find('[name=group]').addClass('d-none');
							}
		                });
		                event.preventDefault();
		            }
		        });
				
				scope.getRoomMessages = function(roomHash){
					return scope.rooms[roomHash]['messages'];
				}
				
				scope.openConversation = function(roomHash){
					if($(element).find('[data-room-hash="'+roomHash+'"]').length)
					{
						$(element).find('[data-room-hash="'+roomHash+'"]').removeClass('minimize');
					}
					else
					{
						//  data-ng-bind="roomHash=\''+roomHash+'\'"
						$(element).find('#group-boxes').prepend($compile('<messenger-box data-room-hash="'+roomHash+'"></messenger-box>')( scope ));
					}
				};
				
				element.on('keydown keypress', '[data-room-hash] [name="message"]', function (event) {
		            if(event.which === 13) {
		            	if(event.currentTarget.value != '')
		            	{
		            		Socket.emit('message:send', {
		            			groupHash:$(event.currentTarget).closest('[data-room-hash]').data('room-hash'), 
		            			text:event.currentTarget.value
		            		});
		            		event.currentTarget.value = '';
		            	}
		                event.preventDefault();
		            }
		        });
			}
		};
	}]);
	

	app.directive("messengerBox", ['$rootScope', '$parse', '$http', 'AppConfig', 'Socket', function($rootScope, $parse, $http, AppConfig, Socket) {
		return {
			templateUrl: window.contextPath+'partials/templates/messenger.box.tpl.html',
			scope: true,
			restrict: 'E',
			replace:true,
			controller: function($scope, $element, $attrs, $controller) {
				$scope.close = function()
				{
					$element.closest('[data-room-hash]').remove();
				};
				
				$scope.minimize = function()
				{
					$element.closest('[data-room-hash]').addClass('minimize');
				};
				$scope.maximize = function()
				{
					$element.closest('[data-room-hash]').removeClass('minimize');
				};
				
				$scope.addUser = function(groupHash)
				{
					$http({
						method: "get",
						url: AppConfig.urls.restUrl + 'group/users',
						params: {group:groupHash}
					}).success(function( data, status, headers, config ) {
						if(data.status == 200){
							$scope.availableGroupUsers = data.data.users;
							$scope.availableGroupHash = groupHash;
							$('#group-user-modal #exampleModalLabel').text($rootScope.rooms[groupHash]['name'])
							$('#group-user-modal').modal('show');
						}
						else if(data.status == 400){
							$scope.formError = data.error;
						}
					}).error(function( data, status, headers, config ){
						console.log(data);
					});
				};
				
				$scope.addUserToGroup = function()
				{
					var newUsers = [];
					var checkedUsers = $('.available-group-user-hash:checked');
					if(checkedUsers.length)
					{
						for(var i=0;i<checkedUsers.length;i++)
						{
							newUsers.push(checkedUsers[i].value);
						}
						Socket.emit('group:member:insert', {groupHash:$scope.availableGroupHash, users:newUsers});
					}
					$('#group-user-modal').modal('hide');
				};
				
				$scope.removeMe = function(groupHash)
				{
					Socket.emit('group:member:delete', {groupHash:groupHash, userHash:$scope.userHash});
				};
				
				$scope.isActive = function(groupHash)
				{
					if($rootScope.rooms && 
							$rootScope.rooms.hasOwnProperty(groupHash) && 
							$rootScope.rooms[groupHash].hasOwnProperty('users') && 
							$rootScope.rooms[groupHash]['users'].hasOwnProperty($rootScope.userHash) && 
							$rootScope.rooms[groupHash]['users'][$rootScope.userHash]['status'] == 1
					)
					{
						return true;
					}
					return false;
				}
			},
			link: function($scope, $element, $attrs)
			{
				$scope.roomHash = $attrs.roomHash;
				$scope.getUserName = function(userHash)
				{
					if($rootScope.userHash == userHash)
					{
						return 'You';

					}
					else
					{
						return $rootScope.users[userHash] ? $rootScope.users[userHash]['firstname']+ ' ' + $rootScope.users[userHash]['lastname'] : '';
					}
					
					
				};
				
				$scope.currentMessageUser = '';
				$scope.messageBoxByUser = function(isGroup, userHash)
				{
					var result = isGroup && $scope.currentMessageUser != userHash;
					$scope.currentMessageUser = userHash;
					return result;
				};
				

			}
		};
	}]);
});
