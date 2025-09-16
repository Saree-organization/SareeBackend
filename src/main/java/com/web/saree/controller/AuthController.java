package com.web.saree.controller;

import com.web.saree.dto.request.VerifyOtpRequest;
import com.web.saree.service.OtpService;
import com.web.saree.service.UserService;
import com.web.saree.security.JwtUtils;
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
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    // REGISTRATION ENDPOINTS
    @PostMapping("/send-otp-register")
    public ResponseEntity<?> sendOtpRegister(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
            }

            if (userService.isUserExists(email)) {
                return ResponseEntity.status(409).body(Map.of("message", "User with this email already exists. Please log in instead."));
            }

            otpService.generateAndSaveOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp-register")
    public ResponseEntity<?> verifyOtpRegister(@RequestBody VerifyOtpRequest request) {
        try {
            String email = request.getEmail();
            String otp = request.getOtp();

            // Correctly calling the new verifyOtp method
            otpService.verifyOtp(email, otp);
            userService.registerNewUser(email);

            return ResponseEntity.ok(Map.of("message", "Registration successful!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred."));
        }
    }

    // LOGIN ENDPOINTS
    @PostMapping("/send-otp-login")
    public ResponseEntity<?> sendOtpLogin(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
            }

            if (!userService.isUserExists(email)) {
                return ResponseEntity.status(404).body(Map.of("message", "User not found. Please register first."));
            }

            otpService.generateAndSaveOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp-login")
    public ResponseEntity<?> verifyOtpLogin(@RequestBody VerifyOtpRequest request) {
        try {
            String email = request.getEmail();
            String otp = request.getOtp();

            // Correctly calling the method that verifies and generates a token
            String jwt = otpService.verifyOtpAndGenerateToken(email, otp);

            return ResponseEntity.ok(Map.of("message", "Login successful!", "token", jwt));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred."));
        }
    }
}