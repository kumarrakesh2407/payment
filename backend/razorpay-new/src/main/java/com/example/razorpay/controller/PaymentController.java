package com.example.razorpay.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    @Value("${razorpay.key.id:your_key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:your_key_secret}")
    private String razorpayKeySecret;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            
            JSONObject orderRequestJson = new JSONObject();
            orderRequestJson.put("amount", orderRequest.getAmount() * 100); // Convert to paise
            orderRequestJson.put("currency", orderRequest.getCurrency());
            orderRequestJson.put("receipt", "order_" + System.currentTimeMillis());
            orderRequestJson.put("payment_capture", 1);

            Order order = razorpay.orders.create(orderRequestJson);
            
            // Create a response map with the order details and the Razorpay key
            Map<String, Object> response = new HashMap<>();
            response.put("key", razorpayKeyId);
            response.put("orderId", order.get("id").toString());
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating order: " + e.getMessage());
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> paymentData) {
        try {
            String orderId = paymentData.get("razorpay_order_id");
            String paymentId = paymentData.get("razorpay_payment_id");
            String signature = paymentData.get("razorpay_signature");
            
            if (orderId == null || paymentId == null || signature == null) {
                return ResponseEntity.badRequest().body("Missing payment verification data");
            }
            
            // Create a signature verification data string
            String data = orderId + "|" + paymentId;
            
            // Verify the signature
            boolean isValidSignature = Utils.verifySignature(data, signature, razorpayKeySecret);
            
            if (isValidSignature) {
                // Payment is successful and verified
                // Here you can update your database, send confirmation emails, etc.
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Payment verified successfully");
                response.put("orderId", orderId);
                response.put("paymentId", paymentId);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Invalid payment signature");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error verifying payment: " + e.getMessage());
        }
    }

    public static class OrderRequest {
        private int amount;
        private String currency;

        // Getters and Setters
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}
