package com.chatty.api.module.user;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chatty.api.MyHttpServlet;
import com.chatty.api.Response;
import com.chatty.model.User;
import com.chatty.utility.Utility;
import com.google.gson.Gson;

/**
 * Servlet implementation class Register
 */
@WebServlet("/api/user/register")
public class UserRegister extends MyHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserRegister() {
        super();
    }
    
    

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setApiResponseConfig(response);
		
		Gson gson = new Gson();
		Response apiResponse = new Response();
		User registerUser = new User();
		
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String repeatPassword = request.getParameter("repeatPassword");
		String firstname = request.getParameter("firstname");
		String lastname = request.getParameter("lastname");
		String gender = request.getParameter("gender");
		
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
				if(com.chatty.dal.User.checkEmailAlreadyTaken(0, email))
				{
					apiResponse.addError("email", "This email is belong to other account.");
				}
				else
				{
					registerUser.setEmail(email);
				}
			}
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
			else
			{
				registerUser.setPassword(password);
			}
		}
		
		if(firstname == null || firstname.trim().isEmpty())
		{
			apiResponse.addError("firstname", "Enter your firstname please.");
		}
		else
		{
			firstname = firstname.trim();
			registerUser.setFirstname(firstname);
		}
		
		if(lastname == null || lastname.trim().isEmpty())
		{
			apiResponse.addError("lastname", "Enter your lastsurname please.");
		}
		else
		{
			lastname = lastname.trim();
			registerUser.setLastname(lastname);
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
				registerUser.setGender(gender);
			}
		}
		
		if(apiResponse.getError().isEmpty())
		{
			int result = com.chatty.dal.User.insert(registerUser);
			if( result == 0)
			{
				apiResponse.addError("form", "There is an error.");
			}
			else
			{
				apiResponse.addMessage(Response.MESSAGE_TYPE_SUCCESS, "Your registration process was done.");
				User recordedUser = com.chatty.dal.User.getUserByUniqueField("USER_ID", result);
				Utility.sendMail(email, "Wellcome", String.format("Your registration is successful. You must complete your activation with, http://localhost:8787/chatty/user/activation.jsp?hash=%s", recordedUser.getHash()));
			}
		}
		response.getWriter().print(gson.toJson(apiResponse));
	}

}
