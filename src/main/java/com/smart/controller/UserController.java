package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for common response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		
		 String userName = principal.getName();
		 System.out.println("USERNAME "+ userName);
		 
		//get the user using username(email)
		 
		 User user = userRepository.getUserByUserName(userName);
		 
		 System.out.println("USER "+user);
		 
		 model.addAttribute("user",user);
		
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
	 
		model.addAttribute("title", "User Dashboard");
	 
		return "normal/user_dashboard";
	}
	
	
	//open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact");
		
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,   Principal principal, HttpSession session) {
		
		try {
		
		String name =  principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		if(file.isEmpty()) {
			//
			System.out.println("File is Empty");
			contact.setImage("contact.png");
		}
		else {
			//upload the file to folder and update the name to contact(image) field in database
			
			contact.setImage(file.getOriginalFilename());
			
			File  saveFile = new ClassPathResource("static/img").getFile();
			
			Path path=  Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is Uploaded");
		}
		
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("added to data");
		
		System.out.println("Data "+contact);
		
		//success alert
		
		session.setAttribute("message", new Message("Contact Added Successfully !!", "success"));
		
		}catch (Exception e) {
			
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			
			//error message alert
			
			session.setAttribute("message", new Message("Something Went Wrong Try Again !!", "danger"));
			
			
		}
		
		return "normal/add_contact_form";
	}
	
	//show or view contacts handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page , Model m, Principal principal) {
		
		m.addAttribute("title","User Contacts");
		
		//retrieve contacts from database
			String userName = principal.getName();
			
			User user = this.userRepository.getUserByUserName(userName);
			
			Pageable pageable = PageRequest.of(page, 5);
			
		 Page<Contact> contacts	= this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		 m.addAttribute("contacts",contacts);
		
		 m.addAttribute("currentPage",page);
		 
		 m.addAttribute("totalPages",contacts.getTotalPages());
		 
		return "normal/show_contacts";
		
	}
	
	//show particular contact details
	
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid,Model m, Principal principal) {
		
		System.out.println("CID"+cid);
		
	    Optional<Contact> contactOptional  =	this.contactRepository.findById(cid);
	    
	    Contact contact = contactOptional.get();
	    
	    String userName = principal.getName();
	    
	    User user = this.userRepository.getUserByUserName(userName);
	    
	    if(user.getId() == contact.getUser().getId())
	    {
	    	m.addAttribute("contact",contact);
	    	m.addAttribute("title",contact.getName());
	    }
	    
	    
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, Principal principal,HttpSession session) {
		
	  Contact contact =	this.contactRepository.findById(cid).get();
	   
	  // contact.setUser(null);
	   
	   User user = this.userRepository.getUserByUserName(principal.getName());
	   
	   user.getContacts().remove(contact);
	   
	   this.userRepository.save(user);
	   
	    	this.contactRepository.delete(contact);
	    	session.setAttribute("message", new Message("Contact Deleted Successfully", "success"));
	    
	  
		return "redirect:/user/show-contacts/0";
	}

	//open update form handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model model) {
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact",contact);
		
		model.addAttribute("title","Update Form");
		return "normal/update_form";
	}
	
	//update button handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal) 
	{
		
		try {
			
			 Contact oldContactDetailContact =  this.contactRepository.findById(contact.getCid()).get();
			
			if(!file.isEmpty())
			{
				//file work
				//delete old pic 
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				
				File file2 = new File(deleteFile, oldContactDetailContact.getImage());
				
				file2.delete();
				
				
				//update new one
				
				File  saveFile = new ClassPathResource("static/img").getFile();
				
				Path path=  Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				
			}
			else {
				contact.setImage(oldContactDetailContact.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is Updated Successfully", "success"));
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		System.out.println("CONTACT Name "+contact.getName());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m) 
	{
		
		m.addAttribute("title","Profile Page");
		
		return "normal/profile";
	}
	
	
	//open settings handler\
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changepassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		
		
		
		System.out.println("OLD PASSWORD "+oldPassword);
		System.out.println("NEW PASSWORD "+newPassword);
		
	 String userNameString =	principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userNameString);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Password Updated Successfully", "success"));
		}
		else {
			session.setAttribute("message", new Message("OldPassword didnot match", "danger"));
		}
		
		
		return "redirect:/user/index";
	}
	
	
	
	
	
}
