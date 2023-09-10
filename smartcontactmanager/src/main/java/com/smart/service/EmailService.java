package com.smart.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;


@Service
public class EmailService {

	
		
	public boolean sendEmail(String subject , String message , String to) {
		boolean f = false;
		String from = "yadavkavi705@gmail.com";
	Properties properties = System.getProperties();

	properties.put("mail.smtp.host", "smtp.gmail.com");
	properties.put("mail.smtp.port", "465");
	properties.put("mail.smtp.ssl.enable", "true");
	properties.put("mail.smtp.auth", "true");

	Session session = Session.getInstance(properties, new Authenticator() {

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			// TODO Auto-generated method stub
			return new PasswordAuthentication("yadavkavi705@gmail.com", "ayhslpurrgvdgvcx");
		}
		 
	});
	session.setDebug(true);

	MimeMessage m = new MimeMessage(session);
	
	try {
		m.setFrom(from);
		m.addRecipient(RecipientType.TO,new InternetAddress(to));
		m.setSubject(subject);
//		m.setText(message);
		m.setContent(message,"text/html");
		Transport.send(m);
		System.out.println("otp sent");
		f=true;
	} catch (MessagingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return f;
	}
	
}
