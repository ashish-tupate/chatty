package com.chatty.websocket;

import java.util.HashMap;

/*
 * groupHash: {id:Integer, users: {userHash:hasAccess}}
 */
public class Group extends HashMap<String, HashMap<String, Object>> {
	
	private static final long serialVersionUID = 202558669863207258L;
	private static Group INSTANCE = new Group();
	
	private static String idKey = "id";
	private static String userKey = "users";
	
	private Group(){}
	
	public static Group getInstance()
	{
		return INSTANCE;
	}
	
	public void putGroupHash(String groupHash, int groupId)
	{
		if(!INSTANCE.containsKey(groupHash))
		{
			HashMap<String, Object> groupData = new HashMap<String, Object>();
			groupData.put(idKey, groupId);
			groupData.put(userKey, new HashMap<String, Integer>());
			INSTANCE.put(groupHash, groupData);
		}
	}
	
	public Integer getGroupId(String groupHash)
	{
		if(INSTANCE.containsKey(groupHash))
		{
			return (Integer)INSTANCE.get(groupHash).get(idKey);
		}
		return null;
	}
	
	public void putUser(String userHash)
	{
		
	}

}
