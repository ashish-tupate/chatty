package com.chatty.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

import com.chatty.model.Group;
import com.chatty.utility.Database;

public class GroupDAL {
	
	private static final String tableName = "GROUPS";
	private static final String primaryKey = "GROUP_ID";

	
	public static int insert(Group group)
	{
		int result = 0;
		group.setHash(getUniqueHash());
		String sql = "INSERT INTO "+tableName+" (NAME, HASH, IS_GROUP, STATUS, CREATED_BY, INSERT_AT) VALUES (?,?,?,?,?,?)";
		try {
			String columnNames[] = {primaryKey};
			PreparedStatement preparedStatement = Database.getPreparedStatement(sql, columnNames);
			preparedStatement.setString(1, group.getName());
			preparedStatement.setString(2, group.getHash());
			preparedStatement.setInt(3, group.getIsGroup());
			preparedStatement.setInt(4, group.getStatus());
			preparedStatement.setInt(5, group.getCreateBy());
			preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
			ResultSet resultSet = null;
			if(preparedStatement.executeUpdate() != 0)
			{
				resultSet = preparedStatement.getGeneratedKeys();
				if(resultSet.next())
				{
					result = resultSet.getInt(1);
				}
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	public static boolean update(Group group)
	{
		boolean result = false;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = Database.getPreparedStatement("UPDATE "+tableName+" SET NAME = ?, STATUS = ?, UPDATE_AT = ? WHERE GROUP_ID = ?");
			preparedStatement.setString(1, group.getName());
			preparedStatement.setInt(2, group.getStatus());
			preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			preparedStatement.setInt(4, group.getId());
			if(preparedStatement.executeUpdate() != 0)
			{
				result = true;
			}
			Database.closer(preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String getUniqueHash()
	{
		String hash = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		while(true)
		{
			String sql = "SELECT GROUP_ID FROM " + tableName + " WHERE HASH = ?";
			try {
				UUID uuid = UUID.randomUUID();
				
				preparedStatement = Database.getPreparedStatement(sql) ;
				preparedStatement.setString(1, uuid.toString());
				resultSet = preparedStatement.executeQuery();
				if(!resultSet.next())
				{
					hash = uuid.toString();
					break;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Database.closer(resultSet, preparedStatement);
		return hash;
	}
	
	public static HashMap<String, Object> checkFriendshipGroup(int userId, int friendId)
	{
		HashMap<String, Object> result = null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT g.HASH, gu.GROUP_USER_ID FUG_ID, gf.GROUP_USER_ID SUG_ID, gu.STATUS FUG_STATUS, gf.STATUS SUG_STATUS ")
			.append("FROM GROUPS g ")
			.append("INNER JOIN GROUP_USERS gu ")
			.append("	ON gu.GROUP_ID = g.GROUP_ID ")
			.append("INNER JOIN GROUP_USERS gf ")
			.append("	ON gf.GROUP_ID = g.GROUP_ID ")
			.append("WHERE g.IS_GROUP = 0 AND gu.USER_ID = ? AND gf.USER_ID = ?");
		
		try {
			PreparedStatement preparedStatement = Database.getPreparedStatement(sb.toString()) ;
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, friendId);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				result = new HashMap<String, Object>();
				result.put("groupHash", resultSet.getString(1));
				result.put("userGroupId", resultSet.getInt(2));
				result.put("friendGroupId", resultSet.getInt(3));
				result.put("userGroupStatus", resultSet.getInt(4));
				result.put("friendGroupStatus", resultSet.getInt(5));
			}
			Database.closer(resultSet, preparedStatement);
		} catch (Exception e) {

		}
		return result;
	}
	
	public static boolean isFriend(int userId, int friendId)
	{
		HashMap<String, Object> result = checkFriendshipGroup(userId, friendId);
		if(result == null)
		{
			return false;
		}
		if((int)result.get("userGroupStatus") != 1 || (int)result.get("friendGroupStatus") != 1)
		{
			return false;
		}
		return true;
	}
	
	
	public static Group getGroupByUniqueField(String type, Object value)
	{
		Group group = null;
		PreparedStatement preparedStatement = null;
		String sql = "SELECT GROUP_ID, NAME, HASH, IS_GROUP, STATUS, CREATED_BY, INSERT_AT, UPDATE_AT FROM "+tableName+" WHERE ";
		Connection connection = Database.getConnection();
		
		try {
			switch (type.toUpperCase()) {
				case "GROUP_ID":
					sql += " GROUP_ID = ?";
					preparedStatement = connection.prepareStatement(sql);
					preparedStatement.setInt(1, (Integer)value);
					break;
				case "HASH":
					sql += " HASH = ?";
					preparedStatement = connection.prepareStatement(sql);
					preparedStatement.setString(1, (String)value);
					break;
			}
			
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				group = new Group();
				setGroupData(group, resultSet);
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return group;
	}
	
	
	public static void setGroupData(Group group, ResultSet resultSet)
	{
		try {
			group.setId(resultSet.getInt(1));
			group.setName(resultSet.getString(2));
			group.setHash(resultSet.getString(3));
			group.setIsGroup(resultSet.getInt(4));
			group.setStatus(resultSet.getInt(5));
			group.setCreateBy(resultSet.getInt(6));
			group.setInsertAt(resultSet.getTimestamp(7));
			group.setUpdateAt(resultSet.getTimestamp(8));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap<String, HashMap<String, Object>> getUserGroup(Integer userId, Integer groupId)
	{
		HashMap<String, HashMap<String, Object>> userGroups = new HashMap<String, HashMap<String, Object>>();
		StringBuilder sb = new StringBuilder();
		sb
			.append("SELECT * FROM ( ")
			.append("( ")
				.append("SELECT g.GROUP_ID, g.HASH GROUP_HASH, g.IS_GROUP, u.HASH CREATED_BY, CONCAT(CONCAT(t.FIRSTNAME, ' '), t.LASTNAME) TITLE ")
				.append("FROM GROUPS g ")
				.append("INNER JOIN GROUP_USERS gu ")
					.append("ON gu.GROUP_ID = g.GROUP_ID AND gu.USER_ID != ? ")
				.append("INNER JOIN USERS u ")
					.append("ON u.USER_ID = g.CREATED_BY ")
				.append("INNER JOIN USERS t ")
					.append("ON t.USER_ID = gu.USER_ID ")
				.append("WHERE g.IS_GROUP = 0 ")
					.append("AND g.GROUP_ID IN ( SELECT sgu.GROUP_ID FROM GROUP_USERS sgu WHERE sgu.USER_ID = ?) ")
			.append(") UNION ( ")
				.append("SELECT g.GROUP_ID, g.HASH groupHash, g.IS_GROUP, u.HASH createdBy, g.NAME title ")
				.append("FROM GROUPS g ")
				.append("INNER JOIN USERS u ")
					.append("ON u.USER_ID = g.CREATED_BY ")
				.append("WHERE g.IS_GROUP = 1 ")
					.append("AND g.GROUP_ID IN ( SELECT sgu.GROUP_ID FROM GROUP_USERS sgu WHERE sgu.USER_ID = ?) ")
			.append(") ) s ");
		
		if(groupId != null)
		{
			sb.append("AND s.GROUP_ID = ? ");
		}
		sb.append("ORDER BY title");
			
			
		PreparedStatement preparedStatement;
		preparedStatement = Database.getPreparedStatement(sb.toString());
		try {
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, userId);
			preparedStatement.setInt(3, userId);
			if(groupId != null)
			{
				preparedStatement.setInt(4, groupId);
			}
			ResultSet resultSet = preparedStatement.executeQuery();
			HashMap<String, Object> listItem;
			while(resultSet.next())
			{
				listItem = new HashMap<String, Object>();
				listItem.put("groupId", resultSet.getInt("GROUP_ID"));
				listItem.put("name", resultSet.getString("TITLE"));
				listItem.put("isGroup", resultSet.getInt("IS_GROUP"));
				listItem.put("createdBy", resultSet.getString("CREATED_BY"));
				userGroups.put(resultSet.getString("GROUP_HASH"), listItem);
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}  
		return userGroups;
	}
	
	public static HashMap<String, HashMap<String, Object>> getUserGroups(Integer userId)
	{
		 return getUserGroup(userId, null);
	}

}
