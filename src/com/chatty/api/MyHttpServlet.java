package com.chatty.api;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import com.chatty.utility.Database;

public class MyHttpServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		Database.close();
	}
	
	protected static void setApiResponseConfig(HttpServletResponse response)
	{
		response.setContentType("application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		
	    response.addHeader("Access-Control-Allow-Origin", "*");
	    response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
	    response.addHeader("Access-Control-Allow-Headers", "Content-Type");
	}
}
