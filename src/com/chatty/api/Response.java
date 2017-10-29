package com.chatty.api;

import java.util.HashMap;

public class Response {
	
	public static final String MESSAGE_TYPE_SUCCESS = "success";
	public static final String MESSAGE_TYPE_INFO = "info";
	public static final String MESSAGE_TYPE_WARNING = "warning";
	public static final String MESSAGE_TYPE_DANGER = "danger";

	private HashMap<String, Object> data;
	
	private Object meta;
	
	// http://www.restapitutorial.com/httpstatuscodes.html
	private int status = 200;
	
	private HashMap<String, String> error;

	public Object getData() {
		return this.data;
	}

	public void setData(HashMap<String, Object> data) {
		this.data = data;
	}
	
	public void addData(String key, Object value)
	{
		if(this.data == null || this.data.size() == 0)
		{
			this.data = new HashMap<String, Object>();
		}
		this.data.put(key, value);
	}
	
	/**
	 * 
	 * @param type one of them MESSAGE_TYPE_ fields
	 * @param message
	 */
	public void addMessage(String type, String message)
	{
		HashMap<String, String> msgObject = new HashMap<>();
		msgObject.put("type", type);
		msgObject.put("text", message);
		addData("message", msgObject);
	}

	public Object getMeta() {
		return meta;
	}

	public void setMeta(Object meta) {
		this.meta = meta;
	}

	public HashMap<String, String> getError() {
		if(error == null)
		{
			return new HashMap<String, String>();
		}
		return error;
	}

	public void setError(HashMap<String, String> error) {
		this.error = error;
	}
	
	public void addError(String key, String value)
	{
		if(error == null)
		{
			this.error = new HashMap<String, String>();
		}
		
		if(!error.containsKey(key))
		{
			error.put(key, value);
		}
		this.status = 400;
	}
	
	public boolean hasError(String field)
	{
		if(error == null)
		{
			return false;
		}
		return error.containsKey(field);
	}
	
	
	public int getStatus() {
		return status;
	}

	
	public void setStatus(int status) {
		this.status = status;
	}

}
