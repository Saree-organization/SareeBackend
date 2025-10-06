package com.web.saree.controller;

import com.web.saree.dto.request.ContactForm;
import com.web.saree.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/contact")
    public ResponseEntity<String> sendContact(@RequestBody ContactForm form) {
        emailService.sendContactMessage(form.getName(), form.getEmail(), form.getMessage());
        return ResponseEntity.ok("Message sent successfully");
    }
}