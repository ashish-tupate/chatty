package com.chatty.model;

import java.sql.Timestamp;

public class Group {
	
	public Group(){}
	
	
	
	public Group(int isGroup, int createBy, int status) {
		this.isGroup = isGroup;
		this.createBy = createBy;
		this.status = status;
	}

	public Group(String name, int isGroup, int createBy, int status) {
		this(isGroup, createBy, status);
		this.name = name;
	}

	private int id;
	
	private String name;
	
	private String hash;
	
	private int isGroup;
	
	private int createBy;
	
	private int status;
	
	private Timestamp insertAt;
	
	private Timestamp updateAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public int getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(int isGroup) {
		this.isGroup = isGroup;
	}

	public int getCreateBy() {
		return createBy;
	}

	public void setCreateBy(int createBy) {
		this.createBy = createBy;
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

	public Timestamp getUpdateAt() {
		return updateAt;
	}

	public void setInsertAt(Timestamp insertAt) {
		this.insertAt = insertAt;
	}

	public void setUpdateAt(Timestamp updateAt) {
		this.updateAt = updateAt;
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
		Group other = (Group) obj;
		if (id != other.id)
			return false;
		return true;
	}
	

}
