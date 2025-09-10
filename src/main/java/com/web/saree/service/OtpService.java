package com.web.saree.service;

import com.web.saree.entity.Users;
import com.web.saree.reopository.UserRepository;
import com.web.saree.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static final long OTP_VALID_DURATION_MINUTES = 5;

    // The Twilio credentials are no longer used for sending OTP,
    // but the @Value annotations are kept here to avoid errors
    // if other parts of the application still expect them.
    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    public void generateAndSaveOtp(String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        Users user = userRepository.findByPhoneNumber(phoneNumber)
                .orElse(new Users());

        user.setPhoneNumber(phoneNumber);
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);

        // ✅ Instead of sending the OTP via Twilio, it is now printed to the console.
        System.out.println("Generated OTP for " + phoneNumber + ": " + otp);

    }

    public String verifyOtpAndGenerateToken(String phoneNumber, String otp) {
        Optional<Users> userOptional = userRepository.findByPhoneNumber(phoneNumber);

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

            return jwtUtils.generateTokenFromPhoneNumber(phoneNumber);
        } else {
            throw new RuntimeException("Invalid OTP.");
        }
    }
}
