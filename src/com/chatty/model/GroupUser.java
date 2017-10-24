package com.chatty.model;

import java.sql.Timestamp;

public class GroupUser {
	
	public GroupUser(){}
	
	public GroupUser(int groupId, int userId, int status) {
		this.groupId = groupId;
		this.userId = userId;
		this.status = status;
	}

	private int id;

	private int groupId;
	
	private int userId;
	
	private int status;
	
	private Timestamp insertAt;
	
	private Timestamp updateAt;
	
	private Timestamp lastSeenAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Timestamp getInsertAt() {
		return insertAt;
	}

	public void setInsertAt(Timestamp insertAt) {
		this.insertAt = insertAt;
	}
	
	public Timestamp getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(Timestamp updateAt) {
		this.updateAt = updateAt;
	}

	public Timestamp getLastSeenAt() {
		return lastSeenAt;
	}

	public void setLastSeenAt(Timestamp lastSeenAt) {
		this.lastSeenAt = lastSeenAt;
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
		GroupUser other = (GroupUser) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
	
}
