package com.chatty.api.module.user;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.chatty.api.MyHttpServlet;
import com.chatty.api.Response;
import com.chatty.dal.UserDAL;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class List
 */
@WebServlet("/api/users")
public class UserList extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserList() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setApiResponseConfig(response);
		Utility.checkAndSetSession(request, response);
		Gson gson = new Gson();
		Response apiResponse = new Response();
		HttpSession session = request.getSession();
		if(Utility.isOnline(session))
		{
			HashMap<String, Object> users = UserDAL.getAllUser((Integer) session.getAttribute("userId"));
			apiResponse.addData("users", users);
		}
		else
		{
			apiResponse.setStatus(401);
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}

}
