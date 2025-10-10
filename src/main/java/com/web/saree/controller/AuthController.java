package com.web.saree.controller;

import com.web.saree.dto.request.VerifyOtpRequest;
import com.web.saree.service.OtpService;
import com.web.saree.service.UserService; // Assume you have a UserService for database operations
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserService userService; // Autowire your UserService

    @PostMapping("/send-otp-register")
    public ResponseEntity<?> sendOtpForRegistration(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
            }

            // Check if user already exists
            if (userService.isUserExists(email)) {
                return ResponseEntity.badRequest().body(Map.of("message", "User with this email already exists."));
            }

            // If user does not exist, proceed to generate and send OTP
            otpService.generateAndSaveOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/send-otp-login")
    public ResponseEntity<?> sendOtpForLogin(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
            }

            // Check if user exists for login
            if (!userService.isUserExists(email)) {
                return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
            }

            // If user exists, proceed to generate and send OTP
            otpService.generateAndSaveOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp-register")
    public ResponseEntity<?> verifyOtpRegister(@RequestBody VerifyOtpRequest request) {
        try {
            // ✨ FIX: Use the method that returns token AND role
            Map<String, String> result = otpService.verifyOtpAndGenerateTokenAndRole(request.getEmail(), request.getOtp());

            // Send both token and role to the frontend
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful! Auto-logging you in.",
                    "token", result.get("token"),
                    "role", result.get("role")
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred."));
        }
    }
    @CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
    @PostMapping("/verify-otp-login")
    public ResponseEntity<?> verifyOtpLogin(@RequestBody VerifyOtpRequest request) {
        try {
            Map<String, String> result = otpService.verifyOtpAndGenerateTokenAndRole(request.getEmail(), request.getOtp());

            // Send both token and role to the frontend
            return ResponseEntity.ok(Map.of(
                    "message", "OTP verified successfully!",
                    "token", result.get("token"),
                    "role", result.get("role") // Returning the user's role
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred."));
        }
    }
}
