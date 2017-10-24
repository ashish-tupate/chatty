package com.chatty.model;

import java.sql.Timestamp;

public class User {
	
	private int id;
	
	private String email;
	
	private String firstname;
	
	private String lastname;
	
	private String gender;
	
	private String hash;
	
	private int profilePhoto;
	
	private String password;
	
	private Timestamp insertAt;
	
	private Timestamp updateAt;
	
	private Timestamp activationAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash){
		this.hash = hash;
	}

	public int getProfilePhoto() {
		return profilePhoto;
	}

	public void setProfilePhoto(int profilePhoto) {
		this.profilePhoto = profilePhoto;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Timestamp getActivationAt() {
		return activationAt;
	}
	
	public void setActivationAt(Timestamp timestamp) {
		this.activationAt = timestamp;
	}
	
	public String getFullname() {
		return firstname + " " + lastname;
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
		User other = (User) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
