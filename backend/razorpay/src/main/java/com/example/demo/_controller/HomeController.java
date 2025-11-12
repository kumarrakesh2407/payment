package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/")
public class HomeController {
    
    @GetMapping
    public ResponseEntity<String> home() {
        String response = "Razorpay Integration Backend is running!\n\n" +
                        "Available endpoints:\n" +
                        "- GET  /api/payment/ - Check payment API status\n" +
                        "- GET  /api/payment/test - Test endpoint\n" +
                        "- POST /api/payment/create-order - Create a new order\n" +
                        "- POST /api/payment/verify-payment - Verify payment";
        
        System.out.println("Handling root endpoint");
        return ResponseEntity.ok(response);
    }
}
