package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.service.EmailService;

@Controller
public class ForgotController {

	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	
	 @PostMapping("/forgot")
	public String sendOtp(@RequestParam("email") String email,HttpSession session) {
		
		 System.out.println("EMAIL "+email);
		 
		 //generating otp
		 
		 
		 int otp= random.nextInt(999999);
		 
		 System.out.println("OTP "+otp);
		 
		 String subject = "OTP From SmartContactManager By AakashSoni";
		 
		 String message = "Your One Time Password is  "+ otp +" And it is Valid Only for 10 minutes";
		 
		 String to = email;
		 
		 //sent otp
		 boolean flag = this.emailService.sendEmail(subject, message, to);
		 
		 if(flag)
		 {
			
			 return "verify_otp";
		 }
		 else {
			 
			 session.setAttribute("message", "Check your email id");
			 return "forgot_email_form";	
		}
		 
	
	}
}
