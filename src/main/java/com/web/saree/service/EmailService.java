// File: com/web/saree/service/EmailService.java

package com.web.saree.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("chanderisilkelegant@gmail.com"); // Must match spring.mail.username
        message.setTo(toEmail);
        message.setSubject("Your One-Time Password (OTP)");
        message.setText("Your OTP for verification is: " + otp + ". It is valid for 5 minutes. Do not share this with anyone.");
        mailSender.send(message);
    }
    public void sendContactMessage(String name, String email, String messageBody) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("chanderisilkelegant@gmail.com"); // Your Gmail
        message.setTo("chanderisilkelegant@gmail.com");   // Your inbox
        message.setSubject("New Message from " + name);
        message.setText("Name: " + name + "\nEmail: " + email + "\n\nMessage:\n" + messageBody);
        message.setReplyTo(email); // So you can reply directly to user
        mailSender.send(message);
    }

}