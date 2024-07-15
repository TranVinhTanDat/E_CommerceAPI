package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.Order;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Service.OrderService;
import com.example.shoppecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;  // Sử dụng UserService để lấy thông tin User

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.badRequest().body(null);  // Xử lý trường hợp không tìm thấy người dùng
        }
        Order order = orderService.placeOrder(user.getId());  // Chuyển ID dạng Long
        return ResponseEntity.ok(order);
    }
}
