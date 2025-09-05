package com.web.saree.service;

import com.web.saree.entity.Users;
import com.web.saree.reopository.UserRepository;
import com.web.saree.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${msg91.authkey}")
    private String authKey;

    @Value("${msg91.otp.templateId}")
    private String templateId;

    public void generateAndSaveOtp(String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        Users user = userRepository.findByPhoneNumber(phoneNumber)
                .orElse(new Users());

        user.setPhoneNumber(phoneNumber);
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);

        // ✅ Send OTP via MSG91 API
        String url = "https://control.msg91.com/api/v5/otp?mobile=91" + phoneNumber
                + "&authkey=" + authKey
                + "&template_id=" + templateId
                + "&otp=" + otp;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to send OTP via MSG91.");
        }
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
