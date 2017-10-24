package com.chatty.dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.chatty.api.Response;
import com.chatty.model.User;
import com.chatty.utility.Database;
import com.chatty.websocket.Backbone;

public class Friendship {

	private static final String tableName = "FRIENDSHIPS";
	private static final String primaryKey = "FRIENDSHIP_ID";
	
	public static final int STATUS_WAITING = 1;
	public static final int STATUS_APPROVED = 2;
	public static final int STATUS_DENIED = 3;
	public static final int STATUS_CANCELLED = 4;
	public static final int STATUS_DELETED = 5;
	
	public static final String STATUS_TEXT_ADD = "add";
	public static final String STATUS_TEXT_APPROVE = "approve";
	public static final String STATUS_TEXT_DENY = "deny";
	public static final String STATUS_TEXT_CANCEL = "cancel";
	public static final String STATUS_TEXT_DELETE = "delete";
	
	public static final int SENDER_FIRST_USER = 0;
	public static final int SENDER_SECOND_USER = 1;
	
	public static ArrayList<HashMap<String, Object>> getFriendsByStatus(int userId, String status)
	{
		status = status.toLowerCase();
		ArrayList<HashMap<String, Object>> friends = new ArrayList<HashMap<String, Object>>();
		PreparedStatement preparedStatement;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("SELECT u.HASH, u.FIRSTNAME, u.LASTNAME, u.GENDER ")
				.append("FROM ( ")
				.append("SELECT CASE WHEN f.FIRST_USER = ? THEN f.SECOND_USER ELSE f.FIRST_USER END USER_ID ")
				.append("FROM FRIENDSHIPS f ");
			
			if(status.equals("active"))
			{
				stringBuilder.append("WHERE (f.FIRST_USER = ? OR f.SECOND_USER = ?) AND f.STATUS = ? ");
			}
			else if (status.equals("incoming")) {
				stringBuilder.append("WHERE ((f.FIRST_USER = ? AND f.SEND_BY = ?) OR (f.SECOND_USER = ? AND f.SEND_BY = ?)) AND f.STATUS = ? ");
			}
			else if(status.equals("outgoing"))
			{
				stringBuilder.append("WHERE ((f.FIRST_USER = ? AND f.SEND_BY = ?) OR (f.SECOND_USER = ? AND f.SEND_BY = ?)) AND f.STATUS = ? ");
			}
					
			stringBuilder.append(") subQuery ")
				.append("INNER JOIN USERS u ")
				.append("ON u.USER_ID = subQuery.USER_ID ")
				.append("WHERE u.ACTIVATION_AT IS NOT NULL ")
				.append("ORDER BY u.FIRSTNAME, u.LASTNAME ");
			
			preparedStatement = Database.getPreparedStatement(stringBuilder.toString());
			preparedStatement.setInt(1, userId);
			if(status.equals("active"))
			{
				preparedStatement.setInt(2, userId);
				preparedStatement.setInt(3, userId);
				preparedStatement.setInt(4, STATUS_APPROVED);
			}
			else if (status.equals("incoming")) {
				preparedStatement.setInt(2, userId);
				preparedStatement.setInt(3, SENDER_SECOND_USER);
				preparedStatement.setInt(4, userId);
				preparedStatement.setInt(5, SENDER_FIRST_USER);
				preparedStatement.setInt(6, STATUS_WAITING);
			}
			else if(status.equals("outgoing"))
			{
				preparedStatement.setInt(2, userId);
				preparedStatement.setInt(3, SENDER_FIRST_USER);
				preparedStatement.setInt(4, userId);
				preparedStatement.setInt(5, SENDER_SECOND_USER);
				preparedStatement.setInt(6, STATUS_WAITING);
			}
			
			ResultSet resultSet = preparedStatement.executeQuery();
			HashMap<String, Object> listItem;
			while(resultSet.next())
			{
				listItem = new HashMap<String, Object>();
				listItem.put("hash", resultSet.getString(1));
				listItem.put("firstname", resultSet.getString(2));
				listItem.put("lastname", resultSet.getString(3));
				listItem.put("gender", resultSet.getString(4));
				listItem.put("online", Backbone.isOnline(resultSet.getString(1)));
				friends.add(listItem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return friends;
		
	}
	
	
	public static com.chatty.model.Friendship getFriendship(User operant, User processing)
	{
		com.chatty.model.Friendship friendship = null;
	//	int operantId = operant.getId();
		if(operant.getId() > processing.getId())
		{
			User temp = operant;
			operant = processing;
			processing = temp;
			temp = null;
		}
		
		PreparedStatement preparedStatement;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("SELECT f.FRIENDSHIP_ID, f.FIRST_USER, f.SECOND_USER, f.STATUS, f.SEND_BY, f.INSERT_AT, f.UPDATE_AT ")
				.append("FROM "+tableName+" f ")
				.append("WHERE f.FIRST_USER = ? AND f.SECOND_USER = ?");

			preparedStatement = Database.getPreparedStatement(stringBuilder.toString());
			preparedStatement.setInt(1, operant.getId());
			preparedStatement.setInt(2, processing.getId());
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				friendship = new com.chatty.model.Friendship();
				friendship.setId(resultSet.getInt(1));
				friendship.setFirstUser(resultSet.getInt(2));
				friendship.setSecondUser(resultSet.getInt(3));
				friendship.setStatus(resultSet.getInt(4));
				friendship.setSendBy(resultSet.getInt(5));
				friendship.setInsertAt(resultSet.getTimestamp(6));
				friendship.setUpdateAt(resultSet.getTimestamp(7));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return friendship;
	}
	
	
	public static int insert(com.chatty.model.Friendship friendship)
	{
		String sql = "INSERT INTO "+tableName+" (FIRST_USER, SECOND_USER, STATUS, SEND_BY, INSERT_AT) VALUES (?,?,?,?,?)";
		try {
			String columnNames[] = {primaryKey};
			PreparedStatement preparedStatement = Database.getPreparedStatement(sql, columnNames);
			preparedStatement.setInt(1, friendship.getFirstUser());
			preparedStatement.setInt(2, friendship.getSecondUser());
			preparedStatement.setInt(3, friendship.getStatus());
			preparedStatement.setInt(4, friendship.getSendBy());
			preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			if(preparedStatement.executeUpdate() != 0)
			{
				ResultSet resultSet = preparedStatement.getGeneratedKeys();
				if(resultSet.next())
				{
					return resultSet.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	
	public static boolean update(com.chatty.model.Friendship friendship)
	{
		PreparedStatement ps;
		try {
			ps = Database.getPreparedStatement("UPDATE "+tableName+" SET STATUS = ?, SEND_BY = ?, UPDATE_AT = ? WHERE FRIENDSHIP_ID = ?");
			ps.setInt(1, friendship.getStatus());
			ps.setInt(2, friendship.getSendBy());
			ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			ps.setInt(4, friendship.getId());
			if(ps.executeUpdate() != 0)
			{
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	public static HashMap<String, String> set(String userHash, String friendHash, String status)
	{
		HashMap<String, String> apiResponse = new HashMap<>();
		if(status == null || status.trim().isEmpty())
		{
			status = STATUS_TEXT_ADD;
		}

		status = status.trim();
		Set<String> statuses = new HashSet<>();
		statuses.add(STATUS_TEXT_ADD);
		statuses.add(STATUS_TEXT_APPROVE);
		statuses.add(STATUS_TEXT_DENY);
		statuses.add(STATUS_TEXT_CANCEL);
		statuses.add(STATUS_TEXT_DELETE);
		
		
		if(!statuses.contains(status))
		{
			apiResponse.put("status", "This status is not specified.");
		}
		
		if(friendHash == null || friendHash.trim().isEmpty())
		{
			apiResponse.put("hash", "There is no person information.");
		}
		
		if(apiResponse.get("status") == null && apiResponse.get("hash") == null)
		{

			friendHash = friendHash.trim();
			User friend = com.chatty.dal.User.getUserByUniqueField("hash", friendHash);
			if(friend == null)
			{
				apiResponse.put("hash", "The person was not found.");
			}
			else
			{
				User user = com.chatty.dal.User.getUserByUniqueField("hash", userHash);
				if(user == null)
				{
					apiResponse.put("form", "There is an error.");
				}
				else
				{
					com.chatty.model.Friendship friendship = getFriendship(user, friend);
					if(friendship == null)
					{
						System.out.println("friendship is null");
						// is insert
						if(status.equals(STATUS_TEXT_ADD))
						{
							friendship = new com.chatty.model.Friendship();
							if(user.getId() > friend.getId())
							{
								friendship.setFirstUser(friend.getId());
								friendship.setSecondUser(user.getId());
								friendship.setSendBy(1);
							}
							else
							{
								friendship.setFirstUser(user.getId());
								friendship.setSecondUser(friend.getId());
								friendship.setSendBy(0);
							}
							friendship.setStatus(STATUS_WAITING);
							int friendshipKey = insert(friendship);
							if(friendshipKey == 0)
							{
								System.out.println("friendship is not insertted");
								apiResponse.put("form", "There is an error.");
							}
							else
							{
								System.out.println("friend ship inserted " + friendshipKey);
								apiResponse.put(Response.MESSAGE_TYPE_SUCCESS, "Your friend request sent.");
							}
						}
						else
						{
							System.out.println("you just could add your friend.");
							apiResponse.put("form", "There is an error.");
						}
					}
					else
					{
						System.out.println("friendship id: " + friendship.getId());
						boolean hasError = true;
						String messageText = null;
						if(status.equals(STATUS_TEXT_ADD) && (friendship.getStatus() == STATUS_DELETED || friendship.getStatus() == STATUS_DENIED || friendship.getStatus() == STATUS_CANCELLED))
						{
							System.out.println("add & deleted");
							friendship.setStatus(STATUS_WAITING);
							if(user.getId() > friend.getId())
							{
								friendship.setSendBy(1);
							}
							else
							{
								friendship.setSendBy(0);
							}
							hasError = false;
							messageText = "Your friend request sent.";
						}
						else if(status.equals(STATUS_TEXT_APPROVE) && friendship.getStatus() == STATUS_WAITING )
						{
							System.out.println("approve & waiting");
							if((user.getId() > friend.getId() && friendship.getSendBy() == 0) || (user.getId() < friend.getId() && friendship.getSendBy() == 1))
							{
								System.out.println("aprove & waitin & ok");
								friendship.setStatus(STATUS_APPROVED);
								hasError = false;
								messageText = "You accepted that friend request which you have.";
								
								// set message group process
							}
						}
						else if(status.equals(STATUS_TEXT_DENY) && friendship.getStatus() == STATUS_WAITING )
						{
							System.out.println("deny & waiting");
							if((user.getId() > friend.getId() && friendship.getSendBy() == 0) || (user.getId() < friend.getId() && friendship.getSendBy() == 1))
							{
								System.out.println("deny & waiting & ok");
								friendship.setStatus(STATUS_DENIED);
								hasError = false;
								messageText = "You didn't accept that friend request which you have.";
							}
						}
						else if(status.equals(STATUS_TEXT_CANCEL) && friendship.getStatus() == STATUS_WAITING )
						{
							System.out.println("cancel & waiintg");
							if((user.getId() > friend.getId() && friendship.getSendBy() == 1) || (user.getId() < friend.getId() && friendship.getSendBy() == 0))
							{
								System.out.println("cancel & waiintg & ok");
								friendship.setStatus(STATUS_CANCELLED);
								hasError = false;
								messageText = "You cancelled that your friend request which was sent by you.";
							}
						}
						else if(status.equals(STATUS_TEXT_DELETE) && friendship.getStatus() == STATUS_APPROVED)
						{
							System.out.println("delete & approved");
							friendship.setStatus(STATUS_DELETED);
							hasError = false;
							messageText = "You deleted your friend.";
							
							// set message group status 
						}
						
						if(hasError == false)
						{
							System.out.println("no error");
							if(update(friendship))
							{
								System.out.println("updateddd");
								if(messageText != null)
								{
									System.out.println("has meesssage text");
								//	apiResponse.addMessage(Response.MESSAGE_TYPE_INFO, messageText);
									apiResponse.put(Response.MESSAGE_TYPE_SUCCESS, messageText);
								}
							}
							else
							{
								System.out.println("couldnt update");
								apiResponse.put("form", "There is an error.");
							}
						}
						else
						{
							System.out.println("has errorrrrr12");
							apiResponse.put("form", "There is an error.");
						}
					}
				}
			}
		}
		return apiResponse;
	}

	public static String getFriendshipStatus(int userId, int friendId)
	{
		int byWhom = userId;
		String result = Friendship.STATUS_TEXT_ADD;
		if(userId > friendId)
		{
			int temp = userId;
			userId = friendId;
			friendId = temp;
		}
		PreparedStatement preparedStatement;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("SELECT f.STATUS, f.SEND_BY ")
				.append("FROM FRIENDSHIPS f ")
				.append("WHERE (f.FIRST_USER = ? AND f.SECOND_USER = ?) ")
				.append("AND (f.STATUS = ? OR f.STATUS = ? ) ");
			
			preparedStatement = Database.getPreparedStatement(stringBuilder.toString());
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, friendId);
			preparedStatement.setInt(3, Friendship.STATUS_WAITING);
			preparedStatement.setInt(4, Friendship.STATUS_APPROVED);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				if(resultSet.getInt(1) == Friendship.STATUS_APPROVED)
				{
					result = STATUS_TEXT_DELETE;
				}
				else
				{
					if((byWhom == userId && resultSet.getInt(2) == 0) || (byWhom == friendId && resultSet.getInt(2) == 1))
					{
						result = STATUS_TEXT_CANCEL;
					}
					else
					{
						result = STATUS_TEXT_DENY;
					}
					
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
