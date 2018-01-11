package com.chatty.utility;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Utility {

	private Utility(){}
	
	public static boolean validEmail(String email)
	{
		String regex = "^(.+)@(.+)$";
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
		java.util.regex.Matcher matcher = pattern.matcher((CharSequence) email);
		return matcher.matches();
	}
	
	public static String md5(String input) {
	    String result = input;
	    if(input != null) {
	    	java.security.MessageDigest md;
			try {
				md = java.security.MessageDigest.getInstance("SHA-1"); // SHA-1, MD5
		        md.update(input.getBytes());
		        java.math.BigInteger hash = new java.math.BigInteger(1, md.digest());
		        result = hash.toString(16);
		        while(result.length() < 40) { //40 for SHA-1, 32 for MD5
		            result = "0" + result;
		        }
			} catch (java.security.NoSuchAlgorithmException e) {
				e.printStackTrace();
				result = null;
			} //or "SHA-1"
	    }
	    return result;
	}
	
	public static String userIdAndPasswordHash(int userId, String password)
	{
		return Utility.md5(String.format("%d_%s", userId, password));
	}
	
	public static boolean sendMail(String to, String title, String content)
	{
	    final String from ="yourmailaddress@gmail.com";
	    final  String password ="yourpassword";

	    Properties props = new Properties();
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.auth", "true");  
	    props.setProperty("mail.transport.protocol", "smtp");     
	    props.setProperty("mail.host", "smtp.gmail.com");  
	    
	    props.put("mail.smtp.port", "587");  
	    props.put("mail.debug", "true");  
	    props.put("mail.smtp.socketFactory.port", "587");  
	    props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");  
	    props.put("mail.smtp.socketFactory.fallback", "false");  
	    Session session = Session.getInstance(props,  
	    new javax.mail.Authenticator() {
	       protected PasswordAuthentication getPasswordAuthentication() {  
	    	   return new PasswordAuthentication(from,password);  
	       }  
	   });  

		   //session.setDebug(true);  
		   Transport transport;
		try {
			transport = session.getTransport();
			   InternetAddress addressFrom = new InternetAddress(from);  

			   MimeMessage message = new MimeMessage(session);  
			   message.setSender(addressFrom);  
			   message.setSubject(title);  
			   message.setContent(content, "text/plain");  
			   message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));  

			   transport.connect();  
			   Transport.send(message);  
			   transport.close();
		} catch (Exception e) {
			e.printStackTrace();
		}  

		   
	    return true;
	}
	
	
	public static void checkAndSetSession(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
	{
		javax.servlet.http.HttpSession session = request.getSession();
		if((session == null || session.getAttribute("userId") == null))
		{
			javax.servlet.http.Cookie[] cookies = request.getCookies();
			if(cookies != null && cookies.length > 0)
			{
				for (javax.servlet.http.Cookie cookie : cookies) {
					if(cookie.getName().equals(com.chatty.dal.UserSessionDAL.getCookieName()))
					{
						int userId = com.chatty.dal.UserSessionDAL.checkUserSession(cookie.getValue());
						if(userId != 0)
						{
							com.chatty.model.User user = com.chatty.dal.UserDAL.getUserByUniqueField("user_id", userId);
							if(user != null)
							{
								session = request.getSession(true);
								session.setAttribute("userId", user.getId());
								session.setAttribute("userHash", user.getHash());
							}
						}
						else
						{
							deleteSessionCookie(request, response);
						}
						break;
					}
				}
			}

		}
	}
	
	public static void deleteSessionCookie(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
	{
		javax.servlet.http.Cookie[] cookies = request.getCookies();
		for (javax.servlet.http.Cookie cookie : cookies) {
			if(cookie.getName().equals(com.chatty.dal.UserSessionDAL.getCookieName()))
			{
				com.chatty.dal.UserSessionDAL.delete(cookie.getValue());
				cookie.setPath(request.getContextPath());
				cookie.setMaxAge(0);
				response.addCookie(cookie);
				break;
			}
		}
	}
	
	
	public static boolean isOnline(javax.servlet.http.HttpSession session)
	{
		return (session != null && session.getAttribute("userId") != null);
	}
	
	public static String createHash(int length)
	{
		Character[] characters = {
				'q','w','e','r','t','y','u','i','o','p','a','s','d','f','g','h','j','k',    'z','x','c','v','b','n','m', // l
				'Q','W','E','R','T','Y','U',    'O','P','A','S','D','F','G','H','J','K','L','Z','X','C','V','B','N','M' // I
		};
		StringBuilder result = new StringBuilder();
		java.util.Random random = new java.util.Random();
		int characterLength = characters.length;
		for (int i = 0; i < length; i++) {
			result.append(characters[random.nextInt(characterLength)]);
		}
		return result.toString();  
	}
	
}
