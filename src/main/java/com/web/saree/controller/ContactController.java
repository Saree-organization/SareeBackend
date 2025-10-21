package com.web.saree.controller;

import com.web.saree.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public String sendContactEmail(@RequestBody Map<String, String> data) {
        String name = data.get("name");
        String email = data.get("email");
        String message = data.get("message");

        try {
            emailService.sendContactMessage(name, email, message);
            return "Message sent successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send message.";
        }
    }
}
