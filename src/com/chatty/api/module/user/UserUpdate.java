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
import com.chatty.model.User;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class UserUpdate
 */
@WebServlet("/api/user/update")
public class UserUpdate extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserUpdate() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setApiResponseConfig(response);
		Utility.checkAndSetSession(request, response);
		Gson gson = new Gson();
		Response apiResponse = new Response();
		HttpSession session = request.getSession();
		if(Utility.isOnline(session))
		{
			// send password 
			String firstname = request.getParameter("firstname");
			String lastname = request.getParameter("lastname");
			String gender = request.getParameter("gender");
			
			User user = com.chatty.dal.User.getUserByUniqueField("user_id", session.getAttribute("userId"));
			
			if(firstname == null || firstname.trim().isEmpty())
			{
				apiResponse.addError("firstname", "Enter your firstname please.");
			}
			else
			{
				firstname = firstname.trim();
				user.setFirstname(firstname);
			}
			
			if(lastname == null || lastname.trim().isEmpty())
			{
				apiResponse.addError("lastname", "Enter your lastname please.");
			}
			else
			{
				lastname = lastname.trim();
				user.setLastname(lastname);
			}
			
			if(gender == null || gender.trim().isEmpty())
			{
				apiResponse.addError("gender", "Enter your gender please.");
			}
			else
			{
				gender = gender.trim();
				if(!(gender.equals(com.chatty.dal.User.GENDER_MALE) || gender.equals(com.chatty.dal.User.GENDER_FEMALE)))
				{
					apiResponse.addError("gender", "This gender is not specified.");
				}
				else
				{
					user.setGender(gender);
				}
			}
			if(apiResponse.getError().isEmpty())
			{
				if(com.chatty.dal.User.update(user))
				{
					apiResponse.addMessage(Response.MESSAGE_TYPE_SUCCESS, "Updating was done.");
				}
				else
				{
					apiResponse.addError("form", "Updating could not finish.");
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
