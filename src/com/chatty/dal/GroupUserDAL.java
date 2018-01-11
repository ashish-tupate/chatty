package com.chatty.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

import com.chatty.model.GroupUser;
import com.chatty.utility.Database;

public class GroupUserDAL {
	
	private static final String tableName = "GROUP_USERS";
	private static final String primaryKey = "GROUP_USER_ID";
	
	public static final int STATUS_ACTIVE = 1;
	public static final int STATUS_DELETED = 2;


	public static int insert(GroupUser groupUser)
	{
		int result = 0; 
		String sql = "INSERT INTO "+tableName+" (USER_ID, GROUP_ID, STATUS, INSERT_AT, UPDATE_AT) VALUES (?,?,?,?,?)";
		try {
			String columnNames[] = {primaryKey};
			PreparedStatement preparedStatement = Database.getPreparedStatement(sql, columnNames);
			preparedStatement.setInt(1, groupUser.getUserId());
			preparedStatement.setInt(2, groupUser.getGroupId());
			preparedStatement.setInt(3, groupUser.getStatus());
			preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
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
	
	
	public static boolean update(GroupUser groupUser)
	{
		boolean result = false;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = Database.getPreparedStatement("UPDATE "+tableName+" SET LAST_SEEN_AT = ?, STATUS = ?, UPDATE_AT = ? WHERE GROUP_USER_ID = ?");
			preparedStatement.setTimestamp(1, groupUser.getLastSeenAt());
			preparedStatement.setInt(2, groupUser.getStatus());
			preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			preparedStatement.setInt(4, groupUser.getId());
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
	
	public static GroupUser getGroupUserById(int id)
	{
		GroupUser groupUser = null;
		PreparedStatement preparedStatement = null;
		String sql = "SELECT GROUP_USER_ID, USER_ID, GROUP_ID, STATUS, INSERT_AT, UPDATE_AT FROM "+tableName+" WHERE GROUP_USER_ID = ?";
		Connection connection = Database.getConnection();
		
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				groupUser = new GroupUser();
				setGroupUserData(groupUser, resultSet);
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return groupUser;
	}
	
	public static GroupUser getGroupUserByGroupIdAndUserId(int groupId, int userId)
	{
		GroupUser groupUser = null;
		PreparedStatement preparedStatement = null;
		String sql = "SELECT GROUP_USER_ID, USER_ID, GROUP_ID, STATUS, INSERT_AT, UPDATE_AT FROM "+tableName+" WHERE USER_ID = ? AND GROUP_ID = ?";
		Connection connection = Database.getConnection();
		
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, groupId);
			
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				groupUser = new GroupUser();
				setGroupUserData(groupUser, resultSet);
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return groupUser;
	}
	
	
	public static void setGroupUserData(GroupUser groupUser, ResultSet resultSet)
	{
		try {
			groupUser.setId(resultSet.getInt(1));
			groupUser.setUserId(resultSet.getInt(2));
			groupUser.setGroupId(resultSet.getInt(3));
			groupUser.setStatus(resultSet.getInt(4));
			groupUser.setInsertAt(resultSet.getTimestamp(5));
			groupUser.setUpdateAt(resultSet.getTimestamp(6));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public static HashMap<String, HashMap<String, Object>> getGroupUsers(Integer groupId)
	{
		HashMap<String, HashMap<String, Object>> users = new HashMap<String, HashMap<String, Object>>();
		StringBuilder sb = new StringBuilder();
		sb
			.append("SELECT u.HASH, CONCAT(CONCAT(u.FIRSTNAME, ' '), u.LASTNAME) fullname, gu.STATUS ")
			.append("FROM GROUP_USERS gu ")
			.append("INNER JOIN USERS u ")
				.append("ON u.USER_ID = gu.USER_ID ")
			.append("WHERE gu.GROUP_ID = ?");
			
		PreparedStatement preparedStatement;
		preparedStatement = Database.getPreparedStatement(sb.toString());
		ResultSet resultSet = null;
		try {
			preparedStatement.setInt(1, groupId);
			resultSet = preparedStatement.executeQuery();
			HashMap<String, Object> listItem;
			while(resultSet.next())
			{
				listItem = new HashMap<String, Object>();
				listItem.put("fullname", resultSet.getString("FULLNAME"));
				listItem.put("status", resultSet.getInt("STATUS"));
				users.put(resultSet.getString(1), listItem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Database.closer(resultSet, preparedStatement);
		return users;
	}
	
}
