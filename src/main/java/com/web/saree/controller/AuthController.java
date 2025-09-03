package com.web.saree.controller;


import com.web.saree.dto.request.VerifyOtpRequest;
import com.web.saree.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // Allows your React app to connect
public class AuthController {

    @Autowired
    private OtpService otpService;

    /**
     * Endpoint to request an OTP for a given phone number.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload) {
        try {
            String phoneNumber = payload.get("phoneNumber");
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Phone number is required."));
            }

            otpService.generateAndSaveOtp(phoneNumber);

            return ResponseEntity.ok(Map.of("message", "OTP sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to verify the OTP and complete the login/registration.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean isOtpValid = otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());

        if (isOtpValid) {
            return ResponseEntity.ok(Map.of("message", "OTP verified successfully!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP. Please try again."));
        }
    }
}