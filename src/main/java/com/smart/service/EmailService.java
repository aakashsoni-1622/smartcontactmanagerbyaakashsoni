package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to)
	{
		
		boolean f=false;
		
		String from = "soniaakash55@gmail.com";
		//variable for gmail
		String host = "smtp.gmail.com";
		
		//get the system properties
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES "+properties);
		
		//setting imp information to properties object
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//get the session object
	   Session session = Session.getInstance(properties, new Authenticator() {

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			
			return new PasswordAuthentication("soniaakash55@gmail.com", "Bansiwala@1622");
			
		}
		   	
		   
	   
	   });
	   
	   session.setDebug(true);
	   
	   //compose the message(text or multimedia)
	   MimeMessage m = new MimeMessage(session);
	   
	   try {
		
		 //from email id
		   m.setFrom(from);
		 
		  //adding recipient
		   m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		   
		   //adding subject to message
		   m.setSubject(subject);
		   
		   //adding text to message
		   m.setText(message);
		   
		   //send the message using transport class
		   
		   Transport.send(m);
		   
		   System.out.println("sent successfully.......");
		   
	} catch (Exception e) {
		e.printStackTrace();
	}
	return f;
	   
	   
	   
		
	}
	
}
