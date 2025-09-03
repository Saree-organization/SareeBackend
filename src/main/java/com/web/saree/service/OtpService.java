package com.web.saree.service;


import com.web.saree.entity.Users;
import com.web.saree.reopository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Generates a 6-digit OTP, saves it to the user's record in the database,
     * and simulates sending it.
     * @param phoneNumber The user's full phone number.
     * @return The generated OTP.
     */
    public String generateAndSaveOtp(String phoneNumber) {
        // Generate a 6-digit OTP
        String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));

        // Find user by phone number or create a new one if not exists
        Users user = userRepository.findByPhoneNumber(phoneNumber)
                .orElse(new Users());

        user.setPhoneNumber(phoneNumber);
        user.setOtp(otp);
        userRepository.save(user);

        // In a real application, integrate with an SMS gateway (e.g., Twilio) here.
        System.out.println("Generated OTP for " + phoneNumber + " is: " + otp);

        return otp;
    }

    /**
     * Verifies if the provided OTP matches the one stored in the database for the given phone number.
     * @param phoneNumber The user's full phone number.
     * @param otp The 6-digit OTP entered by the user.
     * @return true if the OTP is valid, false otherwise.
     */
    public boolean verifyOtp(String phoneNumber, String otp) {
        Optional<Users> userOptional = userRepository.findByPhoneNumber(phoneNumber);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            // Check if the provided OTP matches the one in the database
            if (otp.equals(user.getOtp())) {
                // For security, clear the OTP after successful verification
                user.setOtp(null);
                userRepository.save(user);
                return true; // OTP is correct
            }
        }
        return false; // User not found or OTP is incorrect
    }
}