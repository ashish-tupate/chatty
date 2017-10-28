package com.chatty.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.chatty.api.Response;
import com.chatty.dal.FriendshipDAL;
import com.chatty.dal.GroupDAL;
import com.chatty.dal.GroupUserDAL;
import com.chatty.dal.MessageDAL;
import com.chatty.dal.UserDAL;
import com.chatty.model.GroupUser;
import com.chatty.model.User;
import com.chatty.model.Group;

@ApplicationScoped
@ServerEndpoint( 
		  value="/chat/{userHash}", 
		  decoders = Decoder.class, 
		  encoders = Encoder.class,
		  configurator = GetHttpSessionConfigurator.class)
public class Server{
	HttpSession httpSession;
		
    @OnOpen
    public void open(Session session, @PathParam("userHash") String userHash, EndpointConfig config) throws Exception {
    	/*
    	 * has http session
    	 * 		T ->
    	 * 			check user hash
    	 * 				Valid 	->
    	 * 							has already session
    	 * 								T -> add session
    	 * 								F -> add user hash and session id
    	 * 								
    	 * 
    	 * 				Unvalid ->
    	 * 		F -> 
    	 */
    	
    	boolean sendConnectMessage = false;
    	this.httpSession = (HttpSession) config.getUserProperties()
                .get(HttpSession.class.getName());
    	
    	if(httpSession.getAttribute("userId") == null)
    	{
    		session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "You must be authenticate."));
    		return;
    	}
    	if(userHash == null || userHash.trim().isEmpty())
    	{
    		session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "You must have user code."));
    		return;
    	}

		// valid user
		userHash = userHash.trim();
		if(Backbone.getUsers().containsKey(userHash))
		{
			if(Backbone.getUsers().get(userHash).isEmpty())
			{
				sendConnectMessage = true;
			}
			Backbone.getUsers().get(userHash).add(session.getId());
			Backbone.getSessions().put(session.getId(), new SocketUserSession(userHash, session));
		}
		else
		{
			User user = UserDAL.getUserByUniqueField("hash", userHash);
			if(user != null)
			{
				Backbone.getUsers().put(userHash, new HashSet<String>());
				sendConnectMessage = true;
			}
			else
			{
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "Your hash code didn't match."));
				return;
			}
		}
    	
    	Backbone.getSessions().put(session.getId(), new SocketUserSession(userHash, session));
    	
    	if(Backbone.getUsers().containsKey(userHash))
    	{            		
    		Backbone.getUsers().get(userHash).add(session.getId());
    	}
    	if(sendConnectMessage)
    	{
            Message message = new Message();
            message.put("userHash", userHash);
            // socket process
            message.put("sp", "socket:connect");
            
            broadcast(message);	
    	}
    }
    
    
    @OnMessage
    public void onMessage(Session session, Message message) throws IOException, EncodeException {
    	String socketProcess = (String)message.get("sp"); 
    	String userHash = Backbone.getSessions().get(session.getId()).getUserHash();
    	User socketUser = UserDAL.getUserByUniqueField("hash", userHash);
        switch (socketProcess) {
			case "friend:set":
				messageFriendSet(message, socketUser);
			break;
			case "group:insert":
				messageGroupInsert(message, socketUser);
			break;
			case "group:update":
				messageGroupUpdate(message, socketUser);
			break;
			case "group:delete":
				messageGroupDelete(message, socketUser);
			break;
			case "group:member:insert":
				messageGroupMemberInsert(message, socketUser);
			break;
			case "group:member:delete":
				messageGroupMemberDelete(message, socketUser);
			break;
			case "message:send":
				messageMessageSend(message, socketUser);
			break;
			case "message:delete":
				messageMessageDelete(message, socketUser);
			break;
		}

    }
    
    private void messageMessageDelete(Message message, User socketUser) {
		if(message.get("messageId") == null) {
			return;
		}

		if(MessageDAL.deleteUserMessage(socketUser.getId(), (Integer)message.get("messageId")) > 0)
		{
			com.chatty.model.Message messageDetail = MessageDAL.getMessage((Integer)message.get("messageId"));
			Message userMessage = new Message("message:delete");
			userMessage.put("groupHash", messageDetail.getGroupId());
			userMessage.put("messageId", messageDetail.getId());
			sendToUser(userMessage, socketUser.getHash());
		}
	}


	private void messageMessageSend(Message message, User socketUser) {
		if(message.get("groupHash") == null) {
			return;
		}
		String groupHash = (String)message.get("groupHash");
		Integer messageGroupId = Backbone.getGroups().getGroupIdByHash(groupHash);
		if(messageGroupId == null || !Backbone.getGroups().getGroupUsers(groupHash).contains(socketUser.getHash()))
		{
			return;
		}
		
		com.chatty.model.Message userMessageData = new com.chatty.model.Message();
		userMessageData.setGroupId(messageGroupId);
		userMessageData.setUserId(socketUser.getId());
		userMessageData.setContent((String)message.get("text"));
		Integer messageId = null;
		if((messageId = MessageDAL.insert(userMessageData)) != 0)
		{
			Message userMessage = new Message("message:send");
			userMessage.put("groupHash", groupHash);
			HashMap<String, Object> messageData = new HashMap<>();
			messageData.put("id", messageId);
			messageData.put("owner", socketUser.getHash());
			messageData.put("text", userMessageData.getContent());
			userMessage.put("message", messageData);
			sendToRoom(userMessage, groupHash);
		}
	}
	

	private void messageGroupMemberDelete(Message message, User socketUser) {
		if(message.get("groupHash") == null || message.get("userHash") == null) {
			return;
		}
		Group groupDelete = GroupDAL.getGroupByUniqueField("hash", (String)message.get("groupHash"));
		if(groupDelete == null || groupDelete.getCreateBy() != socketUser.getId() || message.get("userHash") != socketUser.getHash())
		{
			return;
		}
		User deletedUser;
		if(socketUser.getHash().equals(message.get("userHash")))
		{
			deletedUser = socketUser;
		}
		else
		{
			deletedUser = UserDAL.getUserByUniqueField("hash", message.get("userHash"));
		}
		GroupUser friendGroupDataDeleted = GroupUserDAL.getGroupUserByGroupIdAndUserId(groupDelete.getId(), deletedUser.getId());
		if(friendGroupDataDeleted == null || friendGroupDataDeleted.getStatus() == 1)
		{
			return;
		}
		friendGroupDataDeleted.setStatus(0);
		if(GroupUserDAL.update(friendGroupDataDeleted))
		{			
			Message userMessage = new Message("group:member:delete");
			userMessage.put("groupHash", groupDelete.getHash());
			userMessage.put("userHash", deletedUser.getHash());
			userMessage.put("userStatus", 0);
			sendToRoom(userMessage, groupDelete.getHash());
			Backbone.getGroups().getGroupUsers(groupDelete.getHash()).remove(deletedUser.getHash());
		}
	}


	private void messageGroupMemberInsert(Message message, User socketUser)
    {
		if(message.get("groupHash") == null || message.get("userHash") == null) {
			return;
		}
		User groupMemberInsert = UserDAL.getUserByUniqueField("hash", (String)message.get("userHash"));
		if(!GroupDAL.isFriend(socketUser.getId(), groupMemberInsert.getId()))
		{
			return;
		}
		
		Group group = GroupDAL.getGroupByUniqueField("hash", (String)message.get("groupHash"));
		if(group == null || group.getStatus() != 1)
		{
			return;
		}
		GroupUser userGroupData = GroupUserDAL.getGroupUserByGroupIdAndUserId(group.getId(), socketUser.getId());
		if(userGroupData == null || userGroupData.getStatus() != 1)
		{
			return;
		}
		
		GroupUser friendGroupData = GroupUserDAL.getGroupUserByGroupIdAndUserId(group.getId(), groupMemberInsert.getId());
		if(friendGroupData == null)
		{
			// insert
			friendGroupData = new GroupUser();
			friendGroupData.setGroupId(group.getId());
			friendGroupData.setUserId(groupMemberInsert.getId());
			friendGroupData.setStatus(1);
			if(GroupUserDAL.insert(friendGroupData) != 0)
			{
				Backbone.getGroups().addUserToGroup(group.getHash(), groupMemberInsert.getHash());
				Message userMessage = new Message("group:member:insert");
				userMessage.put("groupHash", group.getHash());
				userMessage.put("userHash", groupMemberInsert.getHash());
				userMessage.put("fullname", groupMemberInsert.getFullname());
				userMessage.put("userStatus", 1);
				sendToRoom(userMessage, group.getHash());
			}
		}
		else
		{
			// update
			if(friendGroupData.getStatus() != 1)
			{
				friendGroupData.setStatus(1);
				if(GroupUserDAL.update(friendGroupData))
				{
					Backbone.getGroups().addUserToGroup(group.getHash(), groupMemberInsert.getHash());
					Message userMessage = new Message("group:member:insert");
					userMessage.put("groupHash", group.getHash());
					userMessage.put("userHash", groupMemberInsert.getHash());
					userMessage.put("fullname", groupMemberInsert.getFullname());
					userMessage.put("userStatus", 1);
					sendToRoom(userMessage, group.getHash());
				}
			}
		}
	}


	private void messageGroupDelete(Message message, User socketUser) {
		if(message.get("hash") == null) {
			return;
		}
		Group deleteGroup = GroupDAL.getGroupByUniqueField("hash", message.get("hash"));
		if(deleteGroup != null && deleteGroup.getCreateBy() == socketUser.getId())
		{
			deleteGroup.setStatus(0);
			if(GroupDAL.update(deleteGroup))
			{
				Message userMessage = new Message("group:update");
				userMessage.put("hash", deleteGroup.getHash());
				userMessage.put("status", deleteGroup.getStatus());
				sendToRoom(userMessage, deleteGroup.getHash());
				Backbone.getGroups().getGroups().remove(deleteGroup.getHash());
			}
		}	
	}


	private void messageGroupUpdate(Message message, User socketUser) {
		if(message.get("hash") == null || message.get("name") == null) {
			return;
		}
		Group updateGroup = GroupDAL.getGroupByUniqueField("hash", message.get("hash"));
		if(updateGroup != null && updateGroup.getCreateBy() == socketUser.getId())
		{
			updateGroup.setName((String)message.get("name"));
			if(GroupDAL.update(updateGroup))
			{
				Message userMessage = new Message("group:update");
				userMessage.put("hash", updateGroup.getHash());
				userMessage.put("name", updateGroup.getName());
				sendToRoom(userMessage, updateGroup.getHash());
			}
		}
	}


	private void messageGroupInsert(Message message, User socketUser) {
		if(message.get("name") == null) {
			return;
		}
	
		// create group
		int groupId = GroupDAL.insert(new Group((String)message.get("name"), 1, socketUser.getId(), 1));
		if(groupId != 0)
		{
			// user attent to groups 
			int groupUserId = GroupUserDAL.insert(new GroupUser(groupId, socketUser.getId(), 1));
			
			if(groupUserId != 0)
			{
				Group group = GroupDAL.getGroupByUniqueField("group_id", groupId);
				Backbone.getGroups().addGroup(group.getHash(), group.getId());
				Backbone.getGroups().addUserToGroup(group.getHash(), socketUser.getHash());

				Message userMessage = new Message("group:insert");
				userMessage.put("hash", group.getHash());
				userMessage.put("name", group.getName());
				userMessage.put("isGroup", 1);
				userMessage.put("createdBy", socketUser.getHash());
				HashMap<String, HashMap<String, Object>> users = new HashMap<String, HashMap<String, Object>>();
				HashMap<String, Object> userDetail = new HashMap<String, Object>();
				userDetail.put("fullname", socketUser.getFirstname() + " " + socketUser.getLastname());
				userDetail.put("status", 1);
				users.put(socketUser.getHash(), userDetail);
				userMessage.put("users", users);
				sendToUser(userMessage, socketUser.getHash());
				
			}
		}
	}


	private void messageFriendSet(Message message, User socketUser) {
		if(message.get("userHash") == null || message.get("status") == null) {
			return;
		}

		String friendHash = (String)message.get("userHash");
		String status = (String)message.get("status");
		HashMap<String, String> friendshipSet = FriendshipDAL.set(socketUser.getHash(), friendHash, status);
		
		if(friendshipSet.size() == 0 || friendshipSet.get(Response.MESSAGE_TYPE_SUCCESS) == null){
			return;
		}
		
		User friendObj = UserDAL.getUserByUniqueField("hash", friendHash);
		
		Message newMessage = new Message("friend:set");
		newMessage.put("userHash", friendHash);
		newMessage.put("status", FriendshipDAL.getFriendshipStatus(socketUser.getId(), friendObj.getId()));
		
		Message friendNewMessage = new Message("friend:set");
		friendNewMessage.put("userHash", socketUser.getHash());
		friendNewMessage.put("status", FriendshipDAL.getFriendshipStatus(friendObj.getId(), socketUser.getId()));
		
		if(status.equals(FriendshipDAL.STATUS_TEXT_APPROVE))
		{
			HashMap<String, Object> friendGroup = GroupDAL.checkFriendshipGroup(socketUser.getId(), friendObj.getId());
			newMessage.put("groupHash", friendGroup.get("groupHash"));
			friendNewMessage.put("groupHash", friendGroup.get("groupHash"));
		}
		
		sendToUser(newMessage, socketUser.getHash());
		sendToUser(friendNewMessage, friendHash);
	}


	@OnClose
    public void onClose(Session session) {
    	System.out.println("Server disconnect...");
    	disconnectBroadcast(session);
    }


    @OnError
    public void onError(Throwable error) {
    	System.out.println("on error socket");
    	System.out.println(error);
    }
    
    @Asynchronous
    private void disconnectBroadcast(Session session)
    {
    	try {
			Thread.sleep(10000);
	    	String sessionId = session.getId();
	        HashMap<String, Object> result = Backbone.removeSession(sessionId);
	        if(!result.isEmpty() && !(Boolean)result.get("hasSession"))
	        {
	            Message message = new Message();
	            message.put("userHash", result.get("userHash"));
	            message.put("sp", "socket:disconnect");
	            broadcast(message);
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    private void broadcast(Message message) 
    {
    	for (Map.Entry<String, SocketUserSession> entry : Backbone.getSessions().entrySet()) {
    	    String key = entry.getKey();
    	    SocketUserSession value = entry.getValue();
    	    if(value.getSession().isOpen())
    	    {
    	    	System.out.println(key + " is openn");
    	    	try {
					value.getSession().getBasicRemote().sendObject(message);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (EncodeException e) {
					e.printStackTrace();
				}
    	    }
    	    else
    	    {
    	    	System.out.println(key + " in not open");
    	    	Backbone.removeSession(key);
    	    }
    	}
    	/*
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote()
                        .sendObject(message);
                    System.out.println(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }
    
    private void sendToSession(Message message, String sessionHash)
    {
    	Session session = Backbone.getSessions().get(sessionHash).getSession();
	    if(session != null && session.isOpen())
	    {
	    	try {
				session.getBasicRemote().sendObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
	    }
	    else
	    {
	    	Backbone.removeSession(sessionHash);
	    }
    }
    
    private void sendToUser(Message message, String userHash)
    {
		for (String sessionHash : Backbone.getUsers().get(userHash)) {
			sendToSession(message, sessionHash);
		}
    }
    
    private void sendToUser(Message message, String userHash, String exceptSession)
    {
		for (String sessionHash : Backbone.getUsers().get(userHash)) {
			if(!sessionHash.equals(exceptSession))
			{
				sendToSession(message, sessionHash);
			}
		}
    }
    
    private void sendToRoom(Message message, String groupHash)
    {
    	HashSet<String> groupUsers = Backbone.getGroups().getGroupUsers(groupHash);
    	for (String userHash : groupUsers)
    	{
    		sendToUser(message, userHash);
    	}
    	
    }

}
