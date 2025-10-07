package com.web.saree.controller;

import com.web.saree.entity.Users;
import com.web.saree.service.OrderService;
import com.web.saree.service.UserService;
import com.web.saree.service.VariantService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final VariantService variantService;
    private final OrderService orderService;
    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return userService.findAll(page, size);
    }
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        return userService.getOrdersByUserId(userId);
    }


    @PutMapping("/sarees/{sareeId}/variants/{variantId}")
    public ResponseEntity<?> updateVariant(
            @PathVariable Long sareeId,
            @PathVariable Long variantId,
            @RequestBody Map<String, Object> updates
    ) {
        System.out.println ("sareeId: " + sareeId + ", variantId: " + variantId + ", updates: " + updates);
        return variantService.updateVariant(sareeId, variantId, updates);
    }

    @PutMapping("/paymentChangeStatus/{razorpayOrderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String razorpayOrderId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        orderService.updateOrderStatus(razorpayOrderId, status);  // use long if service accepts long
        return ResponseEntity.ok().build();
    }



}
