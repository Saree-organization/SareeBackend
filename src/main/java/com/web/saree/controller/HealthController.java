package com.web.saree.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String root() {
        return "Saree Backend is Live";
    }

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}
