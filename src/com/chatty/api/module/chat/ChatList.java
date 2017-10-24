package com.chatty.api.module.chat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.chatty.api.MyHttpServlet;
import com.chatty.api.Response;
import com.chatty.dal.Group;
import com.chatty.dal.GroupUser;
import com.chatty.dal.Message;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class ChatList
 */
@WebServlet("/api/chat/list")
public class ChatList extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChatList() {
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
			HashMap<String, HashMap<String, Object>> userGroups = Group.getUserGroups((Integer)session.getAttribute("userId"));
			for (Map.Entry<String, HashMap<String, Object>> entry : userGroups.entrySet())
			{
				HashMap<String, Object> values = entry.getValue();
				
				values.put("users", GroupUser.getGroupUsers((Integer)values.get("groupId")));
				
				values.put("messages", Message.getUserLastMessagesByGroup((Integer)session.getAttribute("userId"), (Integer)values.get("groupId")));
				
				values.remove("groupId");
			}
			
			apiResponse.addData("groups", userGroups);
		}
		else
		{
			apiResponse.setStatus(401);
		}
		
		response.getWriter().print(gson.toJson(apiResponse));
	}

}
