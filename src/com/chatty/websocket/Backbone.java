package com.chatty.websocket;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import com.chatty.utility.Database;

public class Backbone {
	
	public static int counter = 0;
	

	public static HashMap<String, SocketUserSession> sessions = new HashMap<String, SocketUserSession>();

	
	// userHash -> {sessionId}
	public static HashMap<String, HashSet<String>> users = new HashMap<String, HashSet<String>>();
	
	
	public static Groups groups = new Groups();

	
	static {
		// set hashes of all activated user with empty session list
		setUsers();
		
		// set groups with group users
		setGroups();
	}

	
	private Backbone(){ }
	
	
	public static void setUsers()
	{
		PreparedStatement ps;
		try {
			String sql = "SELECT HASH FROM USERS WHERE ACTIVATION_AT IS NOT NULL";
			ps = Database.getPreparedStatement(sql);
			ResultSet resultSet = ps.executeQuery();
			while(resultSet.next())
			{
				users.put(resultSet.getString(1), new HashSet<String>());
			}
			Database.closer(resultSet, ps);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setGroups()
	{
		PreparedStatement ps;
		try {
			String sql = "SELECT GROUP_ID, HASH FROM GROUPS";
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT u.HASH ")
				.append("FROM GROUP_USERS gu ")
				.append("INNER JOIN USERS u ")
				.append("ON u.USER_ID = gu.USER_ID ")
				.append("WHERE gu.GROUP_ID = ? ")
				.append("AND gu.STATUS = 1 ");
					 
			ps = Database.getPreparedStatement(sql);
			ResultSet resultSet = ps.executeQuery();
				
			ResultSet rsGroupUsers = null;
			PreparedStatement psGroupUsers = null;
			
			while(resultSet.next())
			{
				groups.addGroup(resultSet.getString(2), resultSet.getInt(1));
				psGroupUsers = Database.getPreparedStatement(sb.toString());
				psGroupUsers.setInt(1, resultSet.getInt(1));
				rsGroupUsers = psGroupUsers.executeQuery();
				while (rsGroupUsers.next()) {
					groups.addUserToGroup(resultSet.getString(2), rsGroupUsers.getString(1));
				}
				
			}
			Database.closer(rsGroupUsers, psGroupUsers);
			Database.closer(resultSet, ps);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public static HashMap<String, SocketUserSession> getSessions() {
		return sessions;
	}

	
	public static void setSessions(HashMap<String, SocketUserSession> sessions) {
		Backbone.sessions = sessions;
	}

	
	public static HashMap<String, HashSet<String>> getUsers() {
		return users;
	}

	
	public static void setUsers(HashMap<String, HashSet<String>> users) {
		Backbone.users = users;
	}

	
	public static Groups getGroups() {
		return groups;
	}

		
	public static void setGroups(Groups groups) {
		Backbone.groups = groups;
	}
		
	
	public static HashMap<String, Object> removeSession(String sessionId)
	{
		HashMap<String, Object> result = new HashMap<String, Object>();
		getSessions().remove(sessionId);
		String key;
		for (Map.Entry<String, HashSet<String>> entry : getUsers().entrySet())
		{
			key = entry.getKey();
			HashSet<String> value = entry.getValue();
			if(value.contains(sessionId))
			{
				value.remove(sessionId);
				result.put("userHash", key);
				result.put("hasSession", !value.isEmpty());
			}
	    }
		return result;
	}
		
	
	public static boolean isOnline(String userId)
	{
		return users != null && users.get(userId) != null && !users.get(userId).isEmpty();
	}
		
	
	public static String getUserBySessionId(String sessionId)
	{
		if(getSessions().get(sessionId) !=null)
		{
			return getSessions().get(sessionId).getUserHash();
		}
		return null;
	}

	
	public static boolean hasSocketSession(String userHash)
	{
		return !getUsers().get(userHash).isEmpty();
	}
	
	
	static class Groups {
		
		private static HashMap<String, HashMap<String, Object>> data = new HashMap<String, HashMap<String, Object>>();
		
		private static String idKey = "id";
		
		private static String userKey = "users";
		
		private Groups(){}
		
		public static HashMap<String, HashMap<String, Object>> getGroups()
		{
			return data;
		}
		
		public static void addGroup(String groupHash, Integer groupId)
		{
			if(!data.containsKey(groupHash))
			{
				HashMap<String, Object> groupData = new HashMap<>();
				groupData.put(idKey, groupId);
				groupData.put(userKey, new HashSet<>());
				data.put(groupHash, groupData);
			}
		}
		
		public static void deleteGroup(String groupHash)
		{
			if(data.containsKey(groupHash))
			{
				data.remove(groupHash);
			}
		}
		
		public static Integer getGroupIdByHash(String groupHash)
		{
			if(data.containsKey(groupHash))
			{
				return (Integer)data.get(groupHash).get(idKey);
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		public static void addUserToGroup(String groupHash, String userHash)
		{
			if(data.containsKey(groupHash))
			{
				((HashSet<String>)data.get(groupHash).get(userKey)).add(userHash);
			}
		}
		
		@SuppressWarnings("unchecked")
		public static HashSet<String> getGroupUsers(String groupHash)
		{
			if(data.containsKey(groupHash))
			{
				return ((HashSet<String>) data.get(groupHash).get(userKey));
			}
			return null;
		}
	}
}
