package com.web.saree.controller;

import com.web.saree.dto.request.VerifyOtpRequest;
import com.web.saree.service.OtpService;
import com.web.saree.service.UserService; // Assume you have a UserService for database operations
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
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
    public ResponseEntity<?> verifyOtpRegister(@RequestBody VerifyOtpRequest request, HttpServletResponse response) {
        try {
            Map<String, String> result = otpService.verifyOtpAndGenerateTokenAndRole(request.getEmail(), request.getOtp());

            String token = result.get("token");
            String role = result.get("role");

            // ✨ NEW: JWT को HttpOnly Cookie में सेट करें
            ResponseCookie cookie = ResponseCookie.from("authToken", token) // Cookie का नाम: authToken
                    .httpOnly(true)
                    .secure(true) // AWS पर HTTPS के लिए TRUE
                    .path("/")
                    .maxAge(3600 * 24 * 7) // उदाहरण: 7 दिन (या आपके JWT Expiration के अनुसार)
                    .sameSite("Lax") // CSRF सुरक्षा के लिए
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Response Body से 'token' हटा दें
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful! Auto-logging you in.",
                    "role", role
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred."));
        }
    }
    @PostMapping("/verify-otp-login")
    public ResponseEntity<?> verifyOtpLogin(@RequestBody VerifyOtpRequest request, HttpServletResponse response) {
        try {
            Map<String, String> result = otpService.verifyOtpAndGenerateTokenAndRole(request.getEmail(), request.getOtp());

            String token = result.get("token");
            String role = result.get("role");

            // ✨ NEW: JWT को HttpOnly Cookie में सेट करें
            ResponseCookie cookie = ResponseCookie.from("authToken", token)
                    .httpOnly(true)
                    .secure(true) // Production/AWS पर HTTPS के लिए TRUE
                    .path("/")
                    .maxAge(3600 * 24 * 7)
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Response Body से 'token' हटा दें
            return ResponseEntity.ok(Map.of(
                    "message", "OTP verified successfully!",
                    "role", role
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred."));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Expired Cookie सेट करके ब्राउज़र से पुरानी Cookie हटा दें
        ResponseCookie cookie = ResponseCookie.from("authToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // MaxAge को 0 सेट करने से Cookie तुरंत expire हो जाती है
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully!"));
    }
}
