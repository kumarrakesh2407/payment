package com.example.demo.controller;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    
    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Payment API is running! Try /test endpoint");
    }
    
    @CrossOrigin(origins = "http://localhost:3000", 
        allowedHeaders = "*", 
        methods = {RequestMethod.GET, RequestMethod.OPTIONS},
        allowCredentials = "true")
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend is running!");
    }

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @CrossOrigin(origins = "http://localhost:3000", 
        allowedHeaders = "*", 
        methods = {RequestMethod.POST, RequestMethod.OPTIONS},
        allowCredentials = "true")
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        System.out.println("Received create-order request with amount: " + orderRequest.getAmount() + " " + orderRequest.getCurrency());
        try {
            if (orderRequest.getAmount() <= 0) {
                System.err.println("Invalid amount: " + orderRequest.getAmount());
                return ResponseEntity.badRequest().body("{\"message\": \"Amount must be greater than 0\"}");
            }
            
            if (razorpayKeyId == null || razorpayKeyId.isEmpty() || 
                razorpayKeySecret == null || razorpayKeySecret.isEmpty()) {
                System.err.println("Razorpay keys are not configured properly");
                return ResponseEntity.status(500).body("{\"message\": \"Server configuration error\"}");
            }
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequestJson = new JSONObject();
            orderRequestJson.put("amount", orderRequest.getAmount() * 100); // Convert to paise
            orderRequestJson.put("currency", orderRequest.getCurrency());
            orderRequestJson.put("receipt", "order_" + System.currentTimeMillis());
            orderRequestJson.put("payment_capture", 1); // Auto capture payment

            Order order = razorpay.orders.create(orderRequestJson);

            JSONObject response = new JSONObject();
            response.put("orderId", (Object) order.get("id"));
            response.put("amount", (Object) order.get("amount"));
            response.put("currency", (Object) order.get("currency"));
            response.put("key", razorpayKeyId);

            return ResponseEntity.ok(response.toString());
        } catch (RazorpayException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating order: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000", 
        allowedHeaders = "*", 
        methods = {RequestMethod.POST, RequestMethod.OPTIONS},
        allowCredentials = "true")
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest verificationRequest) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_payment_id", verificationRequest.getRazorpay_payment_id());
            attributes.put("razorpay_order_id", verificationRequest.getRazorpay_order_id());
            attributes.put("razorpay_signature", verificationRequest.getRazorpay_signature());
            
            JSONObject paymentData = new JSONObject();
            paymentData.put("payment_entity", attributes);
            
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            boolean isValid = Utils.verifyPaymentSignature(paymentData, razorpayKeySecret);
            
            if (isValid) {
                return ResponseEntity.ok("Payment verified successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid signature");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error verifying payment: " + e.getMessage());
        }
    }

    // Request DTOs
    public static class OrderRequest {
        private long amount;
        private String currency;

        // Getters and Setters
        public long getAmount() { return amount; }
        public void setAmount(long amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class PaymentVerificationRequest {
        private String razorpay_payment_id;
        private String razorpay_order_id;
        private String razorpay_signature;

        // Getters and Setters
        public String getRazorpay_payment_id() { return razorpay_payment_id; }
        public void setRazorpay_payment_id(String razorpay_payment_id) { this.razorpay_payment_id = razorpay_payment_id; }
        public String getRazorpay_order_id() { return razorpay_order_id; }
        public void setRazorpay_order_id(String razorpay_order_id) { this.razorpay_order_id = razorpay_order_id; }
        public String getRazorpay_signature() { return razorpay_signature; }
        public void setRazorpay_signature(String razorpay_signature) { this.razorpay_signature = razorpay_signature; }
    }
}
