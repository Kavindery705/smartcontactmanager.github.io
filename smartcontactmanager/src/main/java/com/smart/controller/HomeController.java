package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository ur;
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
	
	@RequestMapping("/")
	public String Home(Model m ) {
		m.addAttribute("title", "Home-Smart Contact manager");
		return "home";
	}
	@RequestMapping("/about")
	public String About(Model m ) {
		m.addAttribute("title", "About-Smart Contact manager");
		return "about";
	}
	@RequestMapping("/signup")
	public String SignUp(Model m ) {
		m.addAttribute("title", "SignUp-Smart Contact manager");
		m.addAttribute("user", new User());
		return "signup";
	}
//	@PostMapping("/do_register")
//	public String registerUser(@Validated @ModelAttribute("user") User user,BindingResult result,@RequestParam(value="agreement",defaultValue = "false") boolean agreement,Model m,HttpSession session) {
//		
//		try {
//			System.out.println("Agreement"+agreement);
//			if(result.hasErrors()) {
//				m.addAttribute("user", user);
//				return "signup";
//			}
//			if(!agreement) {
//				 System.out.println("you have not agreed terms and condition");
//				 throw new Exception("you have not agreed terms and condition");
//			}
//			user.setRole("ROLE_USER");
//			user.setEnabled(true);
//			user.setPassword(passwordEncoder.encode(user.getPassword()));
//			
//			System.out.println(user);
//			user.setImageurl("userprofile.png");
//			User save = this.ur.save(user);
//			m.addAttribute("user", save);
//			session.setAttribute("message", new Message("Successfully register", "alert-success"));
//			return "signup";
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			session.setAttribute("message", new Message("Something went wrong!!"+e.getMessage(), "alert-danger"));
//			return "signup";
//		}
//	}
	@PostMapping("/do_register")
	public String registerUser(@Validated @ModelAttribute("user") User user,BindingResult result,@RequestParam(value="agreement",defaultValue = "false") boolean agreement,Model m,HttpSession session) {
		
		try {
			System.out.println("Agreement"+agreement);
			if(result.hasErrors()) {
				m.addAttribute("user", user);
				return "signup";
			}
			if(!agreement) {
				 System.out.println("you have not agreed terms and condition");
				 throw new Exception("you have not agreed terms and condition");
			}
			String otp=genrateOtp();
			String subject = "Otp from SCM";
			String message =""+
					"<div style='border:2px solid #e2e2e2; padding:20px'>"
					+ "<h1>"
					+ "OTP is :"+otp
					+ "</h1>"
					+ "</div>";
			String to = user.getUemail();
			boolean sendEmail = this.es.sendEmail(subject, message, to);
			System.out.println("*************************************");	
			 System.out.println(sendEmail);
			if(sendEmail) {
				session.setAttribute("otp",otp );
				session.setAttribute("user", user);
				
				
					session.setAttribute("message", new Message("we have sent otp to your email ","alert-success"));
					
					return "verify_signup_otp";
			
			}
			else
			{
				session.setAttribute("message", new Message("Check your email id ","alert-warning"));
				System.out.println("*************************************");
				return "signup";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong!!"+e.getMessage(), "alert-danger"));
			return "signup";
		}
	}
	@PostMapping("/verify-signup-otp")
	public String verifyOtp(@RequestParam("otp") String otp,Model m,HttpSession session) {
		
		String myOtp = (String)session.getAttribute("otp");
		User user= (User)session.getAttribute("user");
		if(otp.equals(myOtp)) {
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println(user);
			user.setImageurl("userprofile.png");
			User save = this.ur.save(user);
			m.addAttribute("user", save);
			session.setAttribute("message", new Message("Successfully register", "alert-success"));
			return "signup";
		}
		
		else
		{
			session.setAttribute("message",new Message("You have entered wrong otp","alert-warning"));
			return "forget_email_form";
		}
	}
	@GetMapping("/signin")
	public String login(Model m) {
		m.addAttribute("title", "Login Page");
		return "login";
	}
}
