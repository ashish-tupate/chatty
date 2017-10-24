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
import com.chatty.dal.User;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class Activation
 */
@WebServlet("/api/user/activation")
public class UserActivation extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserActivation() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setApiResponseConfig(response);
		
		Gson gson = new Gson();
		Response apiResponse = new Response();
		com.chatty.model.User user = new com.chatty.model.User();
		String hash = request.getParameter("hash");
		
		if(hash == null || hash.trim().isEmpty())
		{
			apiResponse.addError("hash", "Enter activation code please.");
		}
		else
		{
			user = User.getUserByUniqueField("hash", hash);
			if(user == null)
			{
				apiResponse.addError("hash", "This code is not specified.");
			}
			else
			{
				if(user.getActivationAt() == null)
				{
					if(User.setActivation(user))
					{
						apiResponse.addMessage(Response.MESSAGE_TYPE_SUCCESS, "Your account was activated.");
					}
					else
					{
						apiResponse.addError("hash", "Your account was not activated.");
					}
				}
				else
				{
					apiResponse.addMessage(Response.MESSAGE_TYPE_INFO, "Your account was activated before.");
				}
			}
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// request activation code by email
		setApiResponseConfig(response);
		
		Gson gson = new Gson();
		Response apiResponse = new Response();
		com.chatty.model.User user = new com.chatty.model.User();
		HttpSession session = request.getSession();
		if(!Utility.isOnline(session))
		{
			String email = request.getParameter("email");
			
			if(email == null || email.trim().isEmpty())
			{
				apiResponse.addError("email", "Enter email please.");
			}
			else
			{
				email = email.trim();
				if(!Utility.validEmail(email))
				{
					apiResponse.addError("email", "This email is not specified.");
				}
				else
				{
					user = User.getUserByUniqueField("email", email);
					if(user == null)
					{
						apiResponse.addError("email", "There is no account with the email.");
					}
					else
					{
						if(user.getActivationAt() == null)
						{
							// TODO send email
							System.out.println(user.getHash());
							apiResponse.addMessage(Response.MESSAGE_TYPE_SUCCESS, "Your activation code was sent your mail.");
						}
						else
						{
							apiResponse.addMessage(Response.MESSAGE_TYPE_INFO, "Activation was done before.");
						}
					}
				}
			}
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}

}
