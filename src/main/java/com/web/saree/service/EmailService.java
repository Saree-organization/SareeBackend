package com.web.saree.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // existing OTP method (keep it)
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("chanderisilkelegant@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your One-Time Password (OTP)");
        message.setText("Your OTP for verification is: " + otp + ". It is valid for 5 minutes. Do not share this with anyone.");
        mailSender.send(message);
    }

    // new contact message method
    public void sendContactMessage(String name, String email, String userMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("chanderisilkelegant@gmail.com");
        message.setTo("chanderisilkelegant@gmail.com"); // Admin email
        message.setSubject("New Contact Form Message from " + name);
        message.setText("Sender Name: " + name +
                "\nSender Email: " + email +
                "\n\nMessage:\n" + userMessage);
        mailSender.send(message);
    }
}
