package com.web.saree.service;

import com.razorpay.RazorpayException;
import com.web.saree.dto.request.ExchangeRequestDTO;
import com.web.saree.entity.*;
import com.web.saree.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRequestRepository exchangeRepo;
    private final OrderItemRepository orderItemRepo;
    private final VariantRepository variantRepo;
    private final UserRepository userRepo;
    private final PaymentService paymentService;

    private final int EXCHANGE_WINDOW_DAYS = 15; // आपकी एक्सचेंज पॉलिसी के अनुसार

    @Transactional
    public ExchangeRequest submitExchangeRequest(ExchangeRequestDTO dto, String userEmail) {
        // 1. Fetch Entities
        OrderItem oldItem = orderItemRepo.findById(dto.getOrderItemId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid OrderItem ID."));
        Variant newVariant = variantRepo.findById(dto.getNewVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid New Variant ID."));
        Users user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // 2. Validation Checks
        if (!oldItem.getOrder().getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("This order item does not belong to the authenticated user.");
        }

        if (oldItem.getOrder().getCreatedAt().plusDays(EXCHANGE_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Exchange window has expired for this item.");
        }

        // Stock Check (Stock will be reserved only after payment or auto-approval)
        if (newVariant.getStock() < oldItem.getQuantity()) {
            throw new IllegalArgumentException("The requested new Saree is currently out of stock in the required quantity.");
        }

        // 3. Calculate Price Difference
        double oldPrice = oldItem.getPrice() * oldItem.getQuantity();
        double newPrice = newVariant.getPriceAfterDiscount() * oldItem.getQuantity();
        double priceDifference = newPrice - oldPrice;

        // 4. Create and Save Request (Initial State)
        ExchangeRequest request = new ExchangeRequest();
        request.setOldOrderItem(oldItem);
        request.setNewVariant(newVariant);
        request.setUser(user);
        request.setReason(dto.getReason());
        request.setPriceDifference(priceDifference);
        request = exchangeRepo.save(request); // Save to get the ID for Razorpay

        // 5. Handle Price Difference Payment/Refund
        if (priceDifference > 0) {
            // New Saree is more expensive -> Requires payment
            try {
                Map<String, Object> razorpayDetails = paymentService.createExchangePaymentOrder(request.getId(), priceDifference);

                // Razorpay ID अब ExchangeRequest Entity में सेव है (PaymentService में)
                request.setPaymentStatus("PENDING_PAYMENT");
                request.setExchangeStatus("PENDING_PAYMENT");

            } catch (RazorpayException e) {
                request.setPaymentStatus("FAILED");
                request.setExchangeStatus("REJECTED_PAYMENT_ISSUE");
                exchangeRepo.save(request);
                throw new RuntimeException("Could not create payment order: " + e.getMessage());
            }

        } else {
            // New Saree is same or cheaper
            request.setPaymentStatus("NOT_REQUIRED");
            request.setExchangeStatus("APPROVED_PICKUP_PENDING"); // Auto-approve for pickup

            // Stock Reservation: तुरंत स्टॉक रिज़र्व करें
            newVariant.setStock(newVariant.getStock() - oldItem.getQuantity());
            variantRepo.save(newVariant);
        }

        oldItem.getOrder().setOrderStatus("Exchange_Processing"); // Mark the original order

        return exchangeRepo.save(request);
    }

    // ... (Admin/Warehouse Side Logic - processItemReceived method, etc.)
}