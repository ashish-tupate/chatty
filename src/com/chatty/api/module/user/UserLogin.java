package com.chatty.api.module.user;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.chatty.api.MyHttpServlet;
import com.chatty.api.Response;
import com.chatty.dal.UserSession;
import com.chatty.model.User;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class Login
 */
@WebServlet("/api/user/login")
public class UserLogin extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserLogin() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// check login
		setApiResponseConfig(response);
		Utility.checkAndSetSession(request, response);
		Gson gson = new Gson();
		Response apiResponse = new Response();
		HttpSession session = request.getSession();
		if(!Utility.isOnline(session))
		{
			apiResponse.setStatus(401);
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setApiResponseConfig(response);
		
		Gson gson = new Gson();
		Response apiResponse = new Response();
		User user = null;
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		if(password == null  || password.trim().isEmpty())
		{
			apiResponse.addError("password", "Enter your password please.");
		}
		else
		{
			password = password.trim();
		}
		
		if(email == null || email.trim().isEmpty())
		{
			apiResponse.addError("email", "Enter your email please.");
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
				user = com.chatty.dal.User.getUserByUniqueField("email", email);
				if(user == null)
				{
					apiResponse.addError("email", "There is no account with the email.");
				}
				else
				{
					if(user.getActivationAt() == null)
					{
						apiResponse.addError("form", "You must activate your account for use.");
					}
					else
					{
						if(!password.isEmpty() && Utility.userIdAndPasswordHash(user.getId(), password).equals(user.getPassword()))
						{
							HttpSession session = request.getSession(true);
							session.setAttribute("userId", user.getId());
							session.setAttribute("userHash", user.getHash());
							String rememberMe = UserSession.insert(user.getId());
							if(rememberMe != null)
							{
								Cookie rememberMeCookie = new Cookie(UserSession.getCookieName(), rememberMe);
								rememberMeCookie.setPath(request.getContextPath());
							//	rememberMeCookie.setHttpOnly(true); // session check with js by cookie
								rememberMeCookie.setMaxAge(2592000); // 60x60x24x30
								response.addCookie(rememberMeCookie);
							}
						}
						else
						{
							apiResponse.addError("password", "Password is wrong.");
						}
					}
				}
			}
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}
}
