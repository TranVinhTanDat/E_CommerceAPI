package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.Entity.Order;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Service.OrderService;
import com.example.shoppecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Order order = orderService.placeOrder(user.getId());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }
        List<Order> orders = orderService.getUserOrders(user.getId());
        orders.forEach(order -> {
            order.setUser(null);
            // Loại bỏ các liên kết vòng tròn nếu có
            order.getItems().forEach(item -> {
                item.setOrder(null);
                item.getProduct().setCategory(null);  // Nếu có liên kết vòng tròn
            });
        });
        return ResponseEntity.ok(orders);
    }




    @GetMapping("/details/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable Long orderId) {
        Order order = orderService.getOrderDetails(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }



    @PostMapping("/mark-as-processing/{orderId}")
    public ResponseEntity<Void> markOrderAsProcessing(@PathVariable Long orderId) {
        orderService.markOrderAsProcessing(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-bank-transfer/{orderId}")
    public ResponseEntity<Void> confirmBankTransfer(@PathVariable Long orderId) {
        orderService.confirmBankTransfer(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject-bank-transfer/{orderId}")
    public ResponseEntity<Void> rejectBankTransfer(@PathVariable Long orderId) {
        orderService.rejectBankTransfer(orderId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestParam(value = "date", required = false) String dateStr) {
        List<OrderDTO> orders;
        if (dateStr != null) {
            try {
                java.sql.Date date = java.sql.Date.valueOf(dateStr);
                orders = orderService.getOrdersByDate(date);
                if (orders.isEmpty()) {
                    System.out.println("No orders found for date: " + date);
                } else {
                    System.out.println("Orders found: " + orders.size());
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            orders = orderService.getAllOrderDTOs();
        }
        return ResponseEntity.ok(orders);
    }




    // Add these in OrderController

    @PostMapping("/place-temporary")
    public ResponseEntity<Order> placeTemporaryOrder(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Order order = orderService.placeTemporaryOrder(user.getId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/finalize/{orderId}")
    public ResponseEntity<Void> finalizeOrder(@PathVariable Long orderId) {
        orderService.finalizeOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all-shipper")
    public ResponseEntity<List<OrderDTO>> getAllOrdersShipper(@RequestParam(value = "date", required = false) String dateStr) {
        List<OrderDTO> orders;
        if (dateStr != null) {
            try {
                java.sql.Date date = java.sql.Date.valueOf(dateStr);
                orders = orderService.getOrdersByDate(date);
                if (orders.isEmpty()) {
                    System.out.println("No orders found for date: " + date);
                } else {
                    System.out.println("Orders found: " + orders.size());
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            orders = orderService.getAllOrderDTOs();
        }
        return ResponseEntity.ok(orders);
    }


}
