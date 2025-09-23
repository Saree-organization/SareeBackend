package com.web.saree.controller;

import com.web.saree.entity.Users;
import com.web.saree.service.UserService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        return userService.findAll();
    }
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        return userService.getOrdersByUserId(userId);
    }

}
