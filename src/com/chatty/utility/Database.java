package com.chatty.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class Database {
/*	
	private static final String USER_NAME = "chatty";
	private static final String PASSWORD = "chatty";
	private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:xe";
*/	
	private static final String DB_DRIVER = "oracle.jdbc.OracleDriver";
	
	private static Connection connection;
//	private Statement statement;
//	private PreparedStatement preparedStatement;
	
	
	private Database()
	{
		/*
		 * PreparedStatement updateTotal = con.prepareStatement(updateStatement);
		 * updateTotal.executeUpdate();
		 * con.commit();
		 * updateTotal.close();
		 */
		
		try 
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Europe/Istanbul"));
			Class.forName(DB_DRIVER);
			
		//	connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			connection = DriverManager.getConnection("jdbc:oracle:thin:chatty/chatty@//localhost:1521/orcl");
			connection.setAutoCommit(true);
		//	statement = connection.createStatement();
		//	statement.executeQuery("SET NAMES 'UTF8'");
		//	statement.executeQuery("SET CHARACTER SET 'UTF8'");
		
			
		} 
		catch (Exception exception)
		{
			processException(exception);
		}
	}
	
	static {
		
		new Database();
	}

	public static Connection getConnection() {
		return connection;
	}

	
	public static void processException(Exception e) 
	{
		e.printStackTrace();
		System.out.println("Message: " + e.getMessage());
	//	System.out.println("SQL state: " + e.getSQLState());
	//	System.out.println("Hata kodu: " + e.getErrorCode());
	}
	
	public static PreparedStatement getPreparedStatement(String sql)
	{
		try {
			return connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PreparedStatement getPreparedStatement(String sql, String[] columnNames)
	{
		try {
			return connection.prepareStatement(sql, columnNames);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void close()
	{
		try {
			if(connection != null)
			{
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
