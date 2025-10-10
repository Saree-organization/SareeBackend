package com.web.saree.service;

import com.web.saree.entity.Users;
import com.web.saree.repository.UserRepository;
import com.web.saree.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    private static final long OTP_VALID_DURATION_MINUTES = 5;

    public void generateAndSaveOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        Users user = userRepository.findByEmail(email)
                .orElse(new Users());

        // Note: user.role will be "USER" by default here if it's a new user

        user.setEmail(email);
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
        System.out.println("Generated OTP for " + email + ": " + otp);
    }

    // This method is used by both Login and Register (for auto-login)
    public Map<String, String> verifyOtpAndGenerateTokenAndRole(String email, String otp) {
        Optional<Users> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        Users user = userOptional.get();

        if (user.getOtpGeneratedTime() == null ||
                user.getOtpGeneratedTime().plusMinutes(OTP_VALID_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (otp.equals(user.getOtp())) {
            user.setOtp(null);
            user.setOtpGeneratedTime(null);
            userRepository.save(user);

            // ✨ FIX: Use the new JwtUtils method to encode the role in the token
            String token = jwtUtils.generateTokenFromEmailAndRole(user.getEmail(), user.getRole());

            // Prepare Result
            Map<String, String> result = new HashMap<>();
            result.put("token", token);
            result.put("role", user.getRole());

            return result;
        } else {
            throw new RuntimeException("Invalid OTP.");
        }
    }
}