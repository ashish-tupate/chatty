package com.chatty.api.module.user;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.chatty.api.MyHttpServlet;
import com.chatty.api.Response;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class Logout
 */
@WebServlet("/api/user/logout")
public class UserLogout extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserLogout() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setApiResponseConfig(response);
		
		Gson gson = new Gson();
		Response apiResponse = new Response();
		HttpSession session = request.getSession();
		if(session != null)
		{
			session.removeAttribute("userId");
			session.removeAttribute("userHash");
		}
		Utility.deleteSessionCookie(request, response);
		apiResponse.addMessage(Response.MESSAGE_TYPE_INFO, "Logged out.");
		response.getWriter().print(gson.toJson(apiResponse));
	}
}
