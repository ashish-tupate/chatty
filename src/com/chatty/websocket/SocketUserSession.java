package com.chatty.websocket;

import javax.websocket.Session;

public class SocketUserSession {
	
	public SocketUserSession(){};
	
	public SocketUserSession(String userHash, Session session){
		this.userHash = userHash;
		this.session = session;
	};
	
	private String userHash;
	
	private Session session;

	public String getUserHash() {
		return userHash;
	}

	public SocketUserSession setUserHash(String newHash) {
		userHash = newHash;
		return this;
	}

	public Session getSession() {
		return session;
	}

	public SocketUserSession setSession(Session newSession) {
		session = newSession;
		return this;
	}
	
	

}
