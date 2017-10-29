'use strict';

define(['app', 'ngCookies', 'ngSanitize'], function (app) {
	app.service('Socket', ['$rootScope', '$http', 'AppConfig', function ($rootScope, $http, AppConfig) {
		if(!$rootScope.hasOwnProperty('users') || !$rootScope.hasOwnProperty('rooms'))
		{
			/*
			 * {roomHash:{status:true, users:{userHashes}, messages:[{msgid:111, sender:userHash, text:"hello world"}]}}
			 * 
			 */
			$rootScope.rooms = [];
			
			$rootScope.users = [];
		}
		
		var events = []; // key, callback func
		events['socket:connect'] = function(data){
			if($rootScope.users.hasOwnProperty(data.userHash))
			{
				$rootScope.$apply(function () {
					 $rootScope.users[data.userHash]["online"] = true;
			    });
			}
		};
		
		events['socket:disconnect'] = function(data){
			if($rootScope.users.hasOwnProperty(data.userHash))
			{
				$rootScope.$apply(function () {
					 $rootScope.users[data.userHash]["online"] = false;
			    });
			}
		};
		
		events['friend:set'] = function(data){
			if($rootScope.users.hasOwnProperty(data.userHash))
			{
				$rootScope.$apply(function () {
					$rootScope.users[data.userHash]["status"] = data.status;
					if(typeof data.groupHash != 'undefined')
					{
						$http({
							method: "get",
							url: AppConfig.urls.restUrl+'group/detail?group='+data.groupHash,
						}).success(function( dataRest, status, headers, config ) {
							if(dataRest.status == 200){
								$rootScope.rooms[data.groupHash] = dataRest.data.group;
							}
						}).error(function( data, status, headers, config ){
							console.log(data);
						});
					}
			    });
			}
		};
		
		
		events['group:insert'] = function(data){			
			 $rootScope.$apply(function () {
				if(!$rootScope.rooms.hasOwnProperty(data.hash))
				{
					$rootScope.rooms[data.hash] = {
							name:data.name,
							isGroup:data.isGroup,
							createdBy:data.createdBy,
							users:data.users,
							messages:[]
					};
				}
		    });
		};
		
		events['group:update'] = function(data){
			$rootScope.$apply(function () {
				if($rootScope.rooms.hasOwnProperty(data.hash))
				{
					$rootScope.rooms[data.hash]["name"] = data.name;
				}
		    });
		};
		
		events['group:delete'] = function(data){
			// TODO:
		};
		
		events['group:member:insert'] = function(data){
			$rootScope.$apply(function () {
				if($rootScope.userHash == data.userHash)
				{
					$http({
						method: "get",
						url: AppConfig.urls.restUrl+'group/detail?group='+data.groupHash,
					}).success(function( dataRest, status, headers, config ) {
						if(dataRest.status == 200){
							$rootScope.rooms[data.groupHash] = dataRest.data.group;
						}
					}).error(function( data, status, headers, config ){
						console.log(data);
					});
				}
				else
				{
					if($rootScope.rooms.hasOwnProperty(data.groupHash))
					{
						$rootScope.rooms[data.groupHash]["users"][data.userHash] = {fullname:data.fullname, status:data.userStatus};
					}
				}
		    });
		};
		
		events['group:member:delete'] = function(data){
			$rootScope.$apply(function () {
				if($rootScope.rooms.hasOwnProperty(data.groupHash))
				{
					$rootScope.rooms[data.groupHash]["users"][data.userHash] = {fullname:data.fullname, status:data.userStatus};
				}
		    });
		};
		
		events['message:send'] = function(data){
			$rootScope.$apply(function () {
				if(!$rootScope.rooms.hasOwnProperty(data.groupHash))
				{
					$rootScope.rooms[data.groupHash] = [];
				}
				if(!$rootScope.rooms[data.groupHash].hasOwnProperty("messages"))
				{
					$rootScope.rooms[data.groupHash]["messages"] = [];
				}
				$rootScope.rooms[data.groupHash]["messages"].push(data.message);
		    });
		};
		
		events['message:delete'] = function(data){
			$rootScope.$apply(function () {
				if(!$rootScope.rooms.hasOwnProperty(data.groupHash))
				{
					$rootScope.rooms[data.groupHash] = [];
				}
				if(!$rootScope.rooms[data.groupHash].hasOwnProperty("messages"))
				{
					$rootScope.rooms[data.groupHash]["messages"] = [];
				}
				for(message in $rootScope.rooms[data.groupHash]["messages"])
				{
					if(message['id'] == data.messageId)
					{
						message['status'] = 0;
						break;
					}
				}
		    });
		};
		
		var emit = function(type, obj){};
		
		var on = function(type, callback){};
		
		if(document.body.id)
		{
			window.ws = new WebSocket("ws://localhost:8787/chatty/chat/"+document.body.id);

			emit = function(type, obj){
				// socket process
				obj['sp'] = type;
				ws.send(JSON.stringify(obj));
			};
			
			on = function(type, callback){
				events[type] = callback;
			};
			
		    ws.onmessage = function(event) {
		    	var d = JSON.parse(event.data);
		    	if(events.hasOwnProperty(d['sp']))
		    	{
		    		events[d['sp']](d);
		    	}
		    	else
		    	{
		    		console.error("Unknown " + d['sp'] + " events type.");
		    	}
		    };
		}
		return {
			emit: emit,
			on: on
		};
	}]);
});