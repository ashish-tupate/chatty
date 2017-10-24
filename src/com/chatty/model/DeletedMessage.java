package com.chatty.model;

import java.sql.Timestamp;

public class DeletedMessage {
	
	private int id;
	
	private int messageId;
	
	private int userId;
	
	private Timestamp insertAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Timestamp getInsertAt() {
		return insertAt;
	}

	public void setInsertAt() {
		this.insertAt = new Timestamp(System.currentTimeMillis());;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeletedMessage other = (DeletedMessage) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
}
