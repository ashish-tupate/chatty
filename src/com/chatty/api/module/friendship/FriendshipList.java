package com.chatty.api.module.friendship;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.chatty.api.MyHttpServlet;
import com.chatty.api.Response;
import com.chatty.dal.FriendshipDAL;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class FriendshipList
 */
@WebServlet("/api/friendships")
public class FriendshipList extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FriendshipList() {
        super();
        // TODO Auto-generated constructor stub
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
			String friendshipStatus = request.getParameter("status");
			Set<String> statuses = new HashSet<>();
			statuses.add("incoming");
			statuses.add("outgoing");
			statuses.add("active");
			if(friendshipStatus == null || friendshipStatus.trim().isEmpty())
			{
				friendshipStatus = "active";
			}
			else
			{
				friendshipStatus = friendshipStatus.trim();
				if(!statuses.contains(friendshipStatus))
				{
					friendshipStatus = "active";
				}
			}
			apiResponse.addData("friends", FriendshipDAL.getFriendsByStatus((Integer)session.getAttribute("userId"), friendshipStatus));
		}
		else
		{
			apiResponse.setStatus(401);
		}
		
		response.getWriter().print(gson.toJson(apiResponse));
	}

}
