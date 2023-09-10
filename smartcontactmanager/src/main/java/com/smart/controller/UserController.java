package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository ur;
	
	@Autowired
	private ContactRepository cr;
	
	@Autowired
	private MyOrderRepository orderRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {
		String userName = principal.getName();
		
		System.out.println(userName);
		
		User user = this.ur.getUserByUserName(userName);
		System.out.println(user);
		m.addAttribute("user", user);
	}
	
	
	@RequestMapping("/index")
	public String dashboard(Model m,Principal principal) {
		m.addAttribute("title", "User Dashboard");
		addCommonData(m, principal);
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m,Principal principal) {
		addCommonData(m, principal);
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//contact form data 
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact
			,@RequestParam("image") MultipartFile file,
			Model m,Principal principal,HttpSession session) {
		addCommonData(m, principal);
		
		try {
			
		
		String userName = principal.getName();
		
		System.out.println(userName);
		
		User user = this.ur.getUserByUserName(userName);
		if(file.isEmpty()) {
			contact.setImageurl("contact.png");
		}
		else
		{
			contact.setImageurl(file.getOriginalFilename());
			
			File file2 = new ClassPathResource("static/image").getFile();
			Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
			System.out.println(path);
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("image is uploaded");
		}
		
		contact.setUser(user);

		user.getContacts().add(contact);
		this.ur.save(user);
		
		System.out.println(contact);
		session.setAttribute("message",new Message("Contact is added !!","alert-success"));
		
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message",new Message("something went wrong Try Again !!","alert-danger"));
		}
		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") int page, Model m ,Principal principal,HttpSession session) {
		
		addCommonData(m, principal);
		m.addAttribute("title", "Show Contacts");
		String name = principal.getName();
		User user = this.ur.getUserByUserName(name);
		
		Pageable pageable = PageRequest.of(page, 4);
		
		Page<Contact> contactlist = this.cr.findContactByUser(user.getUid(), pageable);
		if(!contactlist.isEmpty()) {
			
			m.addAttribute("contacts", contactlist);
			m.addAttribute("currentPage", page);
			m.addAttribute("totalpage", contactlist.getTotalPages());
		}
		else
		{
			session.setAttribute("message", new Message("No Contact Available Add Some Contacts!","alert-warning"));
			return "redirect:/user/add-contact";
		}
		
		return "normal/show-contacts";
	}
	
	@RequestMapping("/contact/{cid}")
	public String showContactDetail(@PathVariable("cid") int cid,Model m,Principal principal) {
		addCommonData(m, principal);
		String name = principal.getName();
		User user = this.ur.getUserByUserName(name);
		
		System.out.println(cid);
		Optional<Contact> find = this.cr.findById(cid);
		Contact contact = find.get();
		if(user.getUid() == contact.getUser().getUid()) {
			m.addAttribute("title", contact.getName());
			m.addAttribute("contact", contact);
			
		}
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") int cid,Model m,Principal principal,HttpSession session) {
		addCommonData(m, principal);
		String name = principal.getName();
		User user = this.ur.getUserByUserName(name);
		Optional<Contact> findById = this.cr.findById(cid);
		Contact contact = findById.get();
		if(user.getUid() == contact.getUser().getUid()) {
			contact.setUser(null);
			
			session.setAttribute("message", new Message("contact deleted successfully..","alert-success"));
			this.cr.delete(contact);
			
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	//update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") int cid,Model m,Principal principal) {
		addCommonData(m, principal);
		m.addAttribute("title","Update-contact");
		Contact contact = this.cr.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update-form";
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute("contact") Contact contact,@RequestParam("image") MultipartFile file,Model m , Principal principal,HttpSession session) {
		System.out.println("******************************");
		System.out.println(contact.getCid());
		contact.setCid(contact.getCid());
		try {
			Contact oldContact = this.cr.findById(contact.getCid()).get();
			if(!file.isEmpty()) {
				
				File deletefile = new ClassPathResource("static/image").getFile();
				File f = new File(deletefile,oldContact.getImageurl());
				f.delete();
				
				File file2 = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file2+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImageurl(file.getOriginalFilename());
			}
			else
			{
				contact.setImageurl(oldContact.getImageurl());
			}
			String name = principal.getName();
			User user = this.ur.getUserByUserName(name);
			contact.setUser(user);
			Contact save = this.cr.save(contact);
			System.out.println(save);
			session.setAttribute("message",new Message("Contact is updated","success"));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return "redirect:/user/contact/"+contact.getCid();
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title", "Profile view");
		return "normal/profile";
	}
	
	//delete your profile
	@GetMapping("/profile/delete/{uid}")
	public String deleteProfile(@PathVariable("uid") int uid,Model m,Principal principal,HttpSession session) {
		addCommonData(m, principal);
		String name = principal.getName();
		User user = this.ur.getUserByUserName(name);
		this.ur.delete(user);
		
		return "redirect:/";
	}
	//update Profile
	@PostMapping("/update-profile/{uid}")
	public String updateProfile(@PathVariable("uid") int uid) {
		return "normal/update-profile";
	}
	
	@PostMapping("/process-profile")
	public String processupdate(@ModelAttribute("user") User user,@RequestParam("image") MultipartFile file ,Model m ,Principal principal,HttpSession session) {
		
		user.setUid(user.getUid());
		try {
			User olduser = this.ur.getUserByUserName(principal.getName());
			
			if(!file.isEmpty()) {
				
				File deletefile = new ClassPathResource("static/image").getFile();
				File f = new File(deletefile,olduser.getImageurl());
				f.delete();
				
				File file2 = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file2+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				user.setImageurl(file.getOriginalFilename());
				
			}
			else
			{
				user.setImageurl(olduser.getImageurl());
			}
			
			user.setRole(olduser.getRole());
			user.setEnabled(true);
			user.setContacts(olduser.getContacts());
			this.ur.save(user);
			session.setAttribute("message", new Message("User updated","success"));
		} catch (Exception e) {
			// TODO: handle exception
			session.setAttribute("message", new Message("User not updated","danger"));
		}
		return "redirect:/user/profile";
		
	}
	
	//open setting handler
	@GetMapping("/settings")
	public String openSetting() {
		return "normal/settings";
	}
	@PostMapping("/change-password")
	public String changePass(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,Model m,HttpSession session) {
		m.addAttribute("title","change password");
		User user = this.ur.getUserByUserName(principal.getName());
		if(this.passwordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(this.passwordEncoder.encode(newPassword));
			this.ur.save(user);
			session.setAttribute("message", new Message("password changed successfully","alert-success"));
		}
		else
		{
			session.setAttribute("message", new Message("old password not matched ","alert-warning"));
			return "redirect:/user/change-password";
		}
		return "redirect:/user/index";
	}
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data , Principal principal) throws Exception {
		System.out.println(data);
		int amt =Integer.parseInt(data.get("amount").toString());
		
				var client = new RazorpayClient("rzp_test_j5tVXEsrEmOdcZ","pZR26WlkyBt91gNyeAUgJRbw");
				JSONObject options = new JSONObject();
				options.put("amount",amt*100);
				options.put("currency", "INR");
				options.put("receipt", "txn_123456");
				Order order = client.Orders.create(options);
				System.out.println(order);
				MyOrder myOrder = new MyOrder();
				myOrder.setAmount(order.get("amount"));
				myOrder.setOrderId(order.get("id"));
				myOrder.setPaymentId(null);
				myOrder.setStatus("created");
				myOrder.setUser(this.ur.getUserByUserName(principal.getName()));
				myOrder.setReceipt(order.get("receipt"));
				
				this.orderRepo.save(myOrder);
				
				return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data , Principal principal){
		
		MyOrder order = this.orderRepo.findByOrderId(data.get("order_id").toString());
		order.setPaymentId(data.get("payment_id").toString());
		order.setStatus(data.get("status").toString());
		this.orderRepo.save(order);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
}
