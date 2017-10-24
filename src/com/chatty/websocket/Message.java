package com.chatty.websocket;

import java.util.HashMap;

public class Message extends HashMap<String, Object> {

	private static final long serialVersionUID = 3747539363721505792L;

	public Message(){}
	
	public Message(String serviceProcess)
	{
		this.put("sp", serviceProcess);
	}
/*
    private String from;
    private String to;
    private String content;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
    
   */ 
    
}
