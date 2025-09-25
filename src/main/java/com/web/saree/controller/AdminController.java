package com.web.saree.controller;

import com.web.saree.entity.Users;
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
    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        return userService.findAll();
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

}
