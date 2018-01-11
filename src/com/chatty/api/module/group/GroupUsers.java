package com.chatty.api.module.group;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.chatty.api.MyHttpServlet;
import com.chatty.api.Response;
import com.chatty.dal.GroupDAL;
import com.chatty.model.Group;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class GroupDetail
 */
@WebServlet("/api/group/users")
public class GroupUsers extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GroupUsers() {
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
			String groupHash = request.getParameter("group"); 
			if( groupHash != null && !groupHash.trim().isEmpty() )
			{
				Group group = GroupDAL.getGroupByUniqueField("hash", groupHash.trim());
				if(group != null)
				{
					apiResponse.addData("users", GroupDAL.getGroupUsersByUserFriends(group.getId(), (int)session.getAttribute("userId")));					
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
