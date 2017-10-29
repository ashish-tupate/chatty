package com.chatty.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

import com.chatty.model.User;
import com.chatty.utility.Database;
import com.chatty.utility.Utility;
import com.chatty.websocket.Backbone;

public class UserDAL {
	
	public static final String GENDER_MALE = "male";
	public static final String GENDER_FEMALE = "female";
	
	private static final String tableName = "USERS";
	private static final String primaryKey = "USER_ID";

	public static int insert(User user)
	{
		String hash = getUniqueHash();
		if(hash != null)
		{
			user.setHash(getUniqueHash());
			String sql = "INSERT INTO "+tableName+" (HASH, EMAIL, FIRSTNAME, LASTNAME, GENDER, PASSWORD, INSERT_AT, UPDATE_AT) VALUES (?,?,?,?,?,?,?,?)";
			
			try {
				String userKey[] = {primaryKey};
				PreparedStatement preparedStatement = Database.getPreparedStatement(sql, userKey) ;
				preparedStatement.setString(1,  user.getHash());
				preparedStatement.setString(2, user.getEmail());
				preparedStatement.setString(3, user.getFirstname());
				preparedStatement.setString(4, user.getLastname());
				preparedStatement.setString(5, user.getGender());
				preparedStatement.setString(6, "temp");
				preparedStatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
				preparedStatement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
				if(preparedStatement.executeUpdate() != 0)
				{
					ResultSet resultSet = preparedStatement.getGeneratedKeys();
					if(resultSet.next())
					{
						int lastInsertId = resultSet.getInt(1);
						updatePassword(lastInsertId, user.getPassword());
						// send activation email
					//	Utility.sendMail(user.getEmail(), "Kayıt işleminiz gerçekleşti", "Lütfen hesabınızın aktivasyonu için mailinize gelen linki tarayıcınızda çalıştırın.");
						Database.closer(resultSet);
						return lastInsertId;
					}
				}
				Database.closer(preparedStatement);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	
	public static String getUniqueHash()
	{
		String hash = null;
		PreparedStatement preparedStatement;
		ResultSet resultSet;
		while(true)
		{
			String sql = "SELECT USER_ID FROM " + tableName + " WHERE hash = ?";
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
	
	
	public static boolean checkEmailAlreadyTaken(int userId, String email)
	{
		boolean result = false;
		String sql = "SELECT * FROM "+tableName+" WHERE USER_ID != ? AND EMAIL = ?";
		try {
			PreparedStatement preparedStatement = Database.getPreparedStatement(sql) ;
			preparedStatement.setInt(1, userId);
			preparedStatement.setString(2, email);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				result = true;
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	public static boolean updatePassword(int userId, String password)
	{
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = Database.getPreparedStatement("UPDATE "+tableName+" SET PASSWORD = ? WHERE USER_ID = ?");
			preparedStatement.setString(1, Utility.userIdAndPasswordHash(userId, password));
			preparedStatement.setInt(2, userId);
			if(preparedStatement.executeUpdate() != 0)
			{
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Database.closer(preparedStatement);
		return result;
	}
	
	public static boolean update(User user)
	{
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = Database.getPreparedStatement("UPDATE "+tableName+" SET FIRSTNAME = ?, LASTNAME = ?, GENDER = ? WHERE USER_ID = ?");
			preparedStatement.setString(1, user.getFirstname());
			preparedStatement.setString(2, user.getLastname());
			preparedStatement.setString(3, user.getGender());
			preparedStatement.setInt(4, user.getId());
			if(preparedStatement.executeUpdate() != 0)
			{
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Database.closer(preparedStatement);
		return false;
	}


	public static boolean setActivation(User user)
	{
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = Database.getPreparedStatement("UPDATE "+tableName+" SET ACTIVATION_AT = ? WHERE USER_ID = ?");
			preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			preparedStatement.setInt(2, user.getId());
			if(preparedStatement.executeUpdate() != 0)
			{
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Database.closer(preparedStatement);
		return result;
	}

	
	public static User getUserByUniqueField(String type, Object value)
	{
		User user = null;
		PreparedStatement preparedStatement = null;
		String sql = "SELECT USER_ID, HASH, EMAIL, PROFILE_PHOTO, FIRSTNAME, LASTNAME, GENDER, PASSWORD, ACTIVATION_AT, INSERT_AT, UPDATE_AT FROM "+tableName+" WHERE ";
		Connection connection = Database.getConnection();
		
		try {
			switch (type.toUpperCase()) {
				case "USER_ID":
					sql += " USER_ID = ?";
					preparedStatement = connection.prepareStatement(sql);
					preparedStatement.setInt(1, (Integer)value);
					break;
				case "HASH":
					sql += " HASH = ?";
					preparedStatement = connection.prepareStatement(sql);
					preparedStatement.setString(1, (String)value);
					break;
				case "EMAIL":
					sql += " EMAIL = ?";
					preparedStatement = connection.prepareStatement(sql);
					preparedStatement.setString(1, (String)value);
					break;
			}
			
		
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				user = new User();
				setUserData(user, resultSet);
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}
	
	
	public static void setUserData(User user, ResultSet resultSet)
	{
		try {
			user.setId(resultSet.getInt(1));
			user.setHash(resultSet.getString(2));
			user.setEmail(resultSet.getString(3));
			user.setProfilePhoto(resultSet.getInt(4));
			user.setFirstname(resultSet.getString(5));
			user.setLastname(resultSet.getString(6));
			user.setGender(resultSet.getString(7));
			user.setPassword(resultSet.getString(8));
			user.setActivationAt(resultSet.getTimestamp(9));
			user.setInsertAt(resultSet.getTimestamp(10));
			user.setUpdateAt(resultSet.getTimestamp(11));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public static HashMap<String, Object> getAllUser(Integer userId)
	{
		HashMap<String, Object> allUsers = new HashMap<String, Object>();
		PreparedStatement preparedStatement;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			//	https://stackoverflow.com/questions/1532461/stringbuilder-vs-string-concatenation-in-tostring-in-java
			stringBuilder.append("SELECT u.HASH, u.FIRSTNAME, u.LASTNAME, u.GENDER, u.USER_ID ")
				.append("FROM USERS u ")
				.append("WHERE u.ACTIVATION_AT IS NOT NULL ")
				.append("AND u.USER_ID != ? ")
				.append("ORDER BY u.FIRSTNAME, u.LASTNAME ");
			
			preparedStatement = Database.getPreparedStatement(stringBuilder.toString());
			preparedStatement.setInt(1, userId);
			ResultSet resultSet = preparedStatement.executeQuery();
			HashMap<String, Object> listItem;
			while(resultSet.next())
			{
				listItem = new HashMap<String, Object>();
				listItem.put("firstname", resultSet.getString(2));
				listItem.put("lastname", resultSet.getString(3));
				listItem.put("gender", resultSet.getString(4));
				listItem.put("status", FriendshipDAL.getFriendshipStatus(userId, resultSet.getInt(5)));
				listItem.put("online", Backbone.isOnline(resultSet.getString(1)));
				allUsers.put(resultSet.getString(1), listItem);
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allUsers;
	}

}
