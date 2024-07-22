package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.Order;
import com.example.shoppecommerce.Entity.Payment;
import com.example.shoppecommerce.Repository.OrderRepository;
import com.example.shoppecommerce.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment processPayment(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotal());
        payment.setPaymentMethod(paymentMethod);

        return paymentRepository.save(payment);
    }

    public boolean verifyPayment(Long orderId, String paymentMethod) {
        boolean isPaymentSuccessful = true; // Simulate payment verification

        if (isPaymentSuccessful) {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus("Pending");
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    public void markOrderAsProcessing(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("Processing");
        orderRepository.save(order);
    }

    public void confirmBankTransfer(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("Payment Confirmed");
        orderRepository.save(order);
    }
}