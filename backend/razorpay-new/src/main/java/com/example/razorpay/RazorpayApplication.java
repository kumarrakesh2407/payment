package com.example.razorpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RazorpayApplication {

    public static void main(String[] args) {
        SpringApplication.run(RazorpayApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Backend is running!\n" +
               "Test endpoint: /api/test";
    }

    @GetMapping("/api/test")
    public String test() {
        return "Backend API is working!";
    }
}
