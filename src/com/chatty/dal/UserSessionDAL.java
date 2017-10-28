package com.chatty.dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import com.chatty.utility.Database;

public class UserSessionDAL {
	private static final String tableName = "USER_SESSIONS";
	private static final String cookieName = "rememberMe";
	
	public static String insert(int userId)
	{
		String hash = getUniqueHash();
		if(hash != null)
		{
			String sql = "INSERT INTO "+tableName+" (USER_ID, HASH, INSERT_AT) VALUES (?,?,?)";
			try {
				PreparedStatement preparedStatement = Database.getPreparedStatement(sql) ;
				preparedStatement.setInt(1,  userId);
				preparedStatement.setString(2, hash);
				preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				if(preparedStatement.executeUpdate() != 0)
				{
					Database.closer(preparedStatement);
					return hash;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	public static void delete(String hash)
	{
		String sql = "DELETE FROM "+tableName+" WHERE HASH = ?";
		try {
			PreparedStatement preparedStatement = Database.getPreparedStatement(sql) ;
			preparedStatement.setString(1, hash);
			preparedStatement.executeUpdate();
			Database.closer(preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public static String getUniqueHash()
	{
		String hash = null;
		while(true)
		{
			UUID uuid = UUID.randomUUID();
			hash = uuid.toString();
			if(checkUserSession(hash) == 0)
			{
				break;
			}
		}
		return hash;
	}
	
	public static int checkUserSession(String hash)
	{
		int userId = 0;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = Database.getPreparedStatement("SELECT USER_ID FROM "+tableName+" WHERE HASH = ?");
			preparedStatement.setString(1, hash);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				userId = resultSet.getInt(1);
			}
			Database.closer(resultSet, preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userId;
	}


	public static String getCookieName() {
		return cookieName;
	}
	
	
	
}
