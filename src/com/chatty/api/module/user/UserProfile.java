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
import com.chatty.model.User;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class UserProfile
 */
@WebServlet("/api/user/profile")
public class UserProfile extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserProfile() {
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
			String hash = request.getParameter("hash");
			HashMap<String, String> user = new HashMap<>();
			if(hash == null || hash.trim().isEmpty())
			{
				User sessionUser = UserDAL.getUserByUniqueField("user_id", session.getAttribute("userId"));
				if(sessionUser != null)
				{
					user.put("email", sessionUser.getEmail());
					user.put("firstname", sessionUser.getFirstname());
					user.put("lastname", sessionUser.getLastname());
					user.put("gender", sessionUser.getGender());
				}
			}
			else
			{
				hash = hash.trim();
				User recordUser = UserDAL.getUserByUniqueField("hash", hash);
				if(recordUser != null)
				{
					user.put("firstname", recordUser.getFirstname());
					user.put("lastname", recordUser.getLastname());
					user.put("gender", recordUser.getGender());
				}
			}
			apiResponse.addData("profile", user);
		}
		else
		{
			apiResponse.setStatus(401);
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}

}
