package com.chatty.model;

import java.sql.Timestamp;

public class Friendship {

	private int id;
	
	private int firstUser;
	
	private int secondUser;
	
	private int status;
	
	private int sendBy;
	
	private Timestamp insertAt;
	
	private Timestamp updateAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFirstUser() {
		return firstUser;
	}

	public void setFirstUser(int firstUser) {
		this.firstUser = firstUser;
	}

	public int getSecondUser() {
		return secondUser;
	}

	public void setSecondUser(int secondUser) {
		this.secondUser = secondUser;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getSendBy() {
		return sendBy;
	}

	public void setSendBy(int sendBy) {
		this.sendBy = sendBy;
	}

	public Timestamp getInsertAt() {
		return insertAt;
	}

	public void setInsertAt(Timestamp timestamp) {
		this.insertAt = timestamp;
	}

	public Timestamp getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(Timestamp timestamp) {
		this.updateAt = timestamp;
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
		Friendship other = (Friendship) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
}
