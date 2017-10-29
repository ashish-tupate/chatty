package com.chatty.model;

import java.sql.Timestamp;

public class Photo {
	
	private int id;
	
	private int userId;
	
	private String name;
	
	private Timestamp insertAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		Photo other = (Photo) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
}
