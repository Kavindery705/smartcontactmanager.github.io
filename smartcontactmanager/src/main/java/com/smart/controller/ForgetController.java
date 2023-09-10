package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgetController {
	@Autowired
	private UserRepository ur;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
//	Random random = new Random(0000);
	private static String genrateOtp() {
		int len = 4;
		String numbers = "0123456789";
		Random random = new Random();
		char otp[] = new char[len];
		for(int i =0;i<otp.length;i++)
		{
			otp[i] = numbers.charAt(random.nextInt(numbers.length()));
		}
		return String.valueOf(otp);
	}
	@Autowired
	private EmailService es;
	
	
	
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email,HttpSession session) {
		
//		int otp = random.nextInt(9999);
		System.out.println("***********************");
		System.out.println(genrateOtp());
		String otp=genrateOtp();
		String subject = "Otp from SCM";
		String message =""+
				"<div style='border:2px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is :"+otp
				+ "</h1>"
				+ "</div>";
		String to = email;
		boolean sendEmail = this.es.sendEmail(subject, message, to);
		System.out.println("*************************************");	
		 System.out.println(sendEmail);
		if(sendEmail) {
			session.setAttribute("otp",otp );
			session.setAttribute("email", email);
			User user = this.ur.getUserByUserName(email);
			if(user==null) {
				session.setAttribute("message",new Message("User doesnt exist!! Check your emailid ","alert-warning"));
				return "forget_email_form";
			}
			else
			{
				session.setAttribute("message", new Message("we have sent otp to your email ","alert-success"));
				
				
				return "verify_otp";
			}
				
			
			
		}
		else
		{
			session.setAttribute("message", new Message("Check your email id ","alert-warning"));
			System.out.println("*************************************");
			return "forget_email_form";
		}
	}
	@RequestMapping("/forgot")
	public String openEmailForm(HttpSession session) {
		return "forget_email_form";
	}
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") String otp,HttpSession session) {
		
		String myOtp = (String)session.getAttribute("otp");
		String email = (String)session.getAttribute("email");
		if(otp.equals(myOtp)) {
			return "password_change_form";
		}
		
		else
		{
			session.setAttribute("message",new Message("You have entered wrong otp","alert-warning"));
			return "forget_email_form";
		}
	}
	@PostMapping("/change-forget-password")
	public String changeForgetPass(@RequestParam("password") String password,HttpSession session) {
		String email = (String)session.getAttribute("email");
		User user = this.ur.getUserByUserName(email);
		user.setPassword(this.passwordEncoder.encode(password));
		this.ur.save(user);
//		session.setAttribute("message",new Message("passeord change successfully!","alert-success"));
		return "redirect:/signin?change=password changed successfully!!";
	}
}
