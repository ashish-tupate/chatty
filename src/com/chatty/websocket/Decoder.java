package com.chatty.websocket;


import javax.websocket.DecodeException;
import javax.websocket.EndpointConfig;
import com.google.gson.Gson;

public class Decoder implements javax.websocket.Decoder.Text<Message>{
	private static Gson gson = new Gson();
    
    
	@Override
    public Message decode(String s) throws DecodeException {
		return (Message)gson.fromJson(s, Message.class);
    }
 
    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }
 
    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }
 
    @Override
    public void destroy() {
        // Close resources
    }
    
}
