package com.chatty.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import com.chatty.model.Message;
import com.chatty.utility.Database;

public class MessageDAL {
	
	private static final String tableName = "MESSAGES";
	private static final String primaryKey = "MESSAGE_ID";

	
	public static int insert(Message message)
	{
		int result = 0;
		String sql = "INSERT INTO "+tableName+" (GROUP_ID, USER_ID, CONTENT, INSERT_AT) VALUES (?,?,?,?)";
		try {
			String columnNames[] = {primaryKey};
			PreparedStatement preparedStatement = Database.getPreparedStatement(sql, columnNames);
			preparedStatement.setInt(1, message.getGroupId());
			preparedStatement.setInt(2, message.getUserId());
			preparedStatement.setString(3, message.getContent());
			preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
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
	
	
	public static Message getMessage(Integer messageId)
	{
		Message message = null;
		PreparedStatement preparedStatement = null;
		String sql = "SELECT MESSAGE_ID, GROUP_ID, USER_ID, CONTENT, INSERT_AT FROM "+tableName+" WHERE MESSAGE_ID = ?";
		Connection connection = Database.getConnection();
		
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, messageId);
			
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				message = new Message();
				setMessageData(message, resultSet);
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	
	public static void setMessageData(Message message, ResultSet resultSet)
	{
		try {
			message.setId(resultSet.getInt(1));
			message.setGroupId(resultSet.getInt(2));
			message.setUserId(resultSet.getInt(3));
			message.setContent(resultSet.getString(4));
			message.setInsertAt(resultSet.getTimestamp(5));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public static ArrayList<HashMap<String, Object>> getUserLastMessagesByGroup(Integer userId, Integer groupId)
	{
		ArrayList<HashMap<String, Object>> messages = new ArrayList<HashMap<String, Object>>();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
		.append("SELECT m.MESSAGE_ID, u.HASH, m.CONTENT ")
		.append("FROM GROUP_USERS gu ")
		.append("INNER JOIN MESSAGES m ")
			.append("ON m.GROUP_ID = gu.GROUP_ID ")
		.append("INNER JOIN USERS u ")
			.append("ON u.USER_ID = m.USER_ID ")
		.append("WHERE gu.GROUP_ID = ? ")
			.append("AND gu.USER_ID = ? ")
			.append("AND m.MESSAGE_ID NOT IN( ")
				.append("SELECT dm.MESSAGE_ID FROM DELETED_MESSAGES dm WHERE dm.USER_ID = gu.USER_ID ")
			.append(") ")
			.append("AND TO_TIMESTAMP(TO_CHAR( m.INSERT_AT , 'yyyy-mm-dd HH24:mi:ss'), 'yyyy-mm-dd HH24:mi:ss') <= TO_TIMESTAMP(TO_CHAR(gu.UPDATE_AT, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd HH24:mi:ss') ")
			.append("ORDER BY m.MESSAGE_ID DESC ")
			.append("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
		
		PreparedStatement preparedStatement;
		preparedStatement = Database.getPreparedStatement(stringBuilder.toString());
		ResultSet resultSet = null;
		try {
			preparedStatement.setInt(1, groupId);
			preparedStatement.setInt(2, userId);
			resultSet = preparedStatement.executeQuery();
			HashMap<String, Object> listItem;
			while(resultSet.next())
			{
				listItem = new HashMap<String, Object>();
				listItem.put("id", resultSet.getInt(1));
				listItem.put("text", resultSet.getClob(3));
				listItem.put("sender", resultSet.getString(2));
				messages.add(listItem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Database.closer(resultSet, preparedStatement);
		return messages;
	}
	
	public static boolean canDeleteMesssageByUser(Integer userId, Integer messageId)
	{
		boolean result = false;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
			.append("SELECT COUNT(m.MESSAGE_ID) ")
			.append("FROM MESSAGES m ")
			.append("INNER JOIN GROUPS g ")
				.append("ON g.GROUP_ID = m.GROUP_ID ")
			.append("INNER JOIN GROUP_USERS gu ")
				.append("ON gu.GROUP_ID = g.GROUP_ID ")
			.append("WHERE m.MESSAGE_ID = ? ")
				.append("AND gu.USER_ID = ? ")
				.append("AND NOT EXISTS ( ")
					.append("SELECT * FROM DELETED_MESSAGES dm WHERE dm.MESSAGE_ID = m.MESSAGE_ID AND dm.USER_ID = ? ")
			.append(")");
		
		PreparedStatement preparedStatement = Database.getPreparedStatement(stringBuilder.toString());
		ResultSet resultSet = null;
		try {
			preparedStatement.setInt(1, messageId);
			preparedStatement.setInt(2, userId);
			preparedStatement.setInt(3, userId);
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Database.closer(resultSet, preparedStatement);
		return result;
	}
	
	public static Integer deleteUserMessage(Integer userId, Integer messageId)
	{
		int result = 0;
		if(canDeleteMesssageByUser(userId, messageId))
		{
			String sql = "INSERT INTO DELETED_MESSAGES (USER_ID, MESSAGE_ID, INSERT_AT) VALUES (?,?,?)";
			try {
				String columnNames[] = {"DELETED_MESSAGE_ID"};
				PreparedStatement preparedStatement = Database.getPreparedStatement(sql, columnNames);
				preparedStatement.setInt(1, userId);
				preparedStatement.setInt(2, messageId);
				preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
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
		}
		return result;
	}

}
