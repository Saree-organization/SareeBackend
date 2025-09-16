package com.web.saree.service;

import com.web.saree.entity.Users;
import com.web.saree.reopository.UserRepository;
import com.web.saree.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        user.setEmail(email);
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
        System.out.println("Generated OTP for " + email + ": " + otp);
    }

    // New method for simple OTP verification
    public void verifyOtp(String email, String otp) {
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
            // OTP is valid, but we don't clear it here since the AuthController
            // will call another service to complete registration or login.
            // Clearing the OTP will be the responsibility of the calling service/controller.
        } else {
            throw new RuntimeException("Invalid OTP.");
        }
    }

    public String verifyOtpAndGenerateToken(String email, String otp) {
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

            return jwtUtils.generateTokenFromEmail(email);
        } else {
            throw new RuntimeException("Invalid OTP.");
        }
    }
}