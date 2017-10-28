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
import com.chatty.dal.UserDAL;
import com.chatty.model.User;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class UserPassword
 */
@WebServlet("/api/user/password")
public class UserPassword extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserPassword() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// forgot password
		setApiResponseConfig(response);
		Utility.checkAndSetSession(request, response);
		Gson gson = new Gson();
		Response apiResponse = new Response();
		HttpSession session = request.getSession();
		if(!Utility.isOnline(session))
		{
			// send password 
			String email = request.getParameter("email");
			if(email == null || email.trim().isEmpty())
			{
				apiResponse.addError("email", "Enter yournemail please.");
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
					User user = UserDAL.getUserByUniqueField("email", email);
					if(user != null)
					{
						String password = Utility.createHash(6);
						if(UserDAL.updatePassword(user.getId(), password))
						{
							// TODO send mail
							apiResponse.addMessage(Response.MESSAGE_TYPE_INFO, "Your password was send to your email.");
						}
						else
						{
							apiResponse.addError("form", "There is an error.");
						}
					}
					else
					{
						apiResponse.addError("form", "There is an error.");
					}
				}
			}
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// update password
		setApiResponseConfig(response);
		Utility.checkAndSetSession(request, response);
		Gson gson = new Gson();
		Response apiResponse = new Response();
		HttpSession session = request.getSession();
		if(Utility.isOnline(session))
		{
			// send password 
			String lastPassword = request.getParameter("lastPassword");
			String password = request.getParameter("password");
			String repeatPassword = request.getParameter("repeatPassword");
			if(lastPassword == null  || lastPassword.trim().isEmpty())
			{
				apiResponse.addError("lastPassword", "Enter your last password.");
			}
			else
			{
				lastPassword = lastPassword.trim();
			}
			
			if(password == null  || password.trim().isEmpty())
			{
				apiResponse.addError("password", "Enter your password please.");
			}
			else
			{
				password = password.trim();
			}
			
			if(repeatPassword == null  || repeatPassword.trim().isEmpty())
			{
				apiResponse.addError("repeatPassword", "Enter password of repeat please.");
			}
			else
			{
				repeatPassword = repeatPassword.trim();
				if(!password.equals(repeatPassword))
				{
					apiResponse.addError("repeatPassword", "Passwords are not same.");
				}
			}
			
			if(!apiResponse.hasError("lastPassword") && !apiResponse.hasError("password") && !apiResponse.hasError("repeatPassword"))
			{
				User user = UserDAL.getUserByUniqueField("user_id", session.getAttribute("userId"));
				if(user != null)
				{

					if(Utility.userIdAndPasswordHash(user.getId(), lastPassword).equals(user.getPassword()))
					{
						if(UserDAL.updatePassword(user.getId(), password))
						{
							// TODO send mail changed your password
							apiResponse.addMessage(Response.MESSAGE_TYPE_SUCCESS, "Your password was changed.");
						}
						else
						{
							apiResponse.addError("form", "Your password could not changed.");
						}
					}
					else
					{
						apiResponse.addError("lastPassword", "Your password is wrong.");
					}
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
