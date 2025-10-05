package com.web.saree.controller;

import com.razorpay.RazorpayException;
import com.web.saree.dto.request.ExchangeRequestDTO;
import com.web.saree.dto.request.PaymentVerificationRequest;
import com.web.saree.entity.ExchangeRequest;
import com.web.saree.service.ExchangeService;
import com.web.saree.service.PaymentService;
import com.web.saree.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;
    private final PaymentService paymentService; // Verification के लिए

    // ग्राहक के लिए: एक्सचेंज अनुरोध सबमिट करना
    @PostMapping("/request")
    public ResponseEntity<?> submitRequest(
            @RequestBody ExchangeRequestDTO requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated."));
            }

            ExchangeRequest request = exchangeService.submitExchangeRequest(requestDto, userDetails.getUsername());

            // अगर पेमेंट पेंडिंग है, तो Razorpay की जानकारी वापस भेजें
            if ("PENDING_PAYMENT".equals(request.getExchangeStatus())) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                        "message", "Exchange submitted, awaiting payment for price difference.",
                        "exchangeId", request.getId(),
                        "razorpayOrderId", request.getRazorpayOrderId(),
                        "amount", request.getPriceDifference() // Frontend needs this
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Exchange request submitted successfully. Pickup pending.",
                    "exchangeId", request.getId(),
                    "status", request.getExchangeStatus()
            ));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to submit exchange request."));
        }
    }

    // ग्राहक के लिए: मूल्य अंतर का भुगतान सत्यापित (Verify) करना
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyExchangePayment(@RequestBody PaymentVerificationRequest request) {
        try {
            boolean isVerified = paymentService.verifyExchangePayment(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );
            if (isVerified) {
                return ResponseEntity.ok(Map.of("message", "Exchange payment successful and verified! Your exchange process is now advancing."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Exchange payment verification failed."));
            }
        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Verification Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // ... (Admin endpoints जैसे processReceivedItem को भी यहाँ जोड़ा जा सकता है)
}