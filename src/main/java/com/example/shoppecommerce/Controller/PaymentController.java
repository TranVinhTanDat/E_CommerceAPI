package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.Payment;
import com.example.shoppecommerce.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String paymentMethod = payload.get("paymentMethod").toString();
        Payment payment = paymentService.processPayment(orderId, paymentMethod);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyPayment(@RequestParam Long orderId, @RequestParam String paymentMethod) {
        boolean isVerified = paymentService.verifyPayment(orderId, paymentMethod);
        return ResponseEntity.ok(isVerified);
    }
}
