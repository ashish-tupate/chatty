package com.chatty.api.module.friendship;

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
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class FriendshipSet
 */
@WebServlet("/api/friendship/set")
public class FriendshipSet extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FriendshipSet() {
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
			String status = request.getParameter("status");
			String friendHash = request.getParameter("hash");
			
			HashMap<String, String> friendshipSet = com.chatty.dal.Friendship.set((String)session.getAttribute("userHash"), friendHash, status);
			if(friendshipSet.size() != 0)
			{
				if(friendshipSet.get(Response.MESSAGE_TYPE_SUCCESS) != null)
				{
					apiResponse.addMessage(Response.MESSAGE_TYPE_SUCCESS, friendshipSet.get(Response.MESSAGE_TYPE_SUCCESS));
				}
				else
				{
					apiResponse.setError(friendshipSet);
				}	
			}
		}
		else
		{
			apiResponse.setStatus(401);
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}
}

