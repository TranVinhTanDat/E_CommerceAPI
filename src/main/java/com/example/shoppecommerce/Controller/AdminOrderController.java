package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.DTO.OrderDetailsDTO;
import com.example.shoppecommerce.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestParam(value = "date", required = false) String dateStr) {
        System.out.println("Yêu cầu lấy tất cả đơn hàng, date: " + dateStr);
        List<OrderDTO> orders;
        if (dateStr != null) {
            try {
                java.sql.Date date = java.sql.Date.valueOf(dateStr.trim());
                orders = orderService.getOrdersByDate(date);
            } catch (IllegalArgumentException e) {
                System.out.println("Lỗi định dạng ngày: " + dateStr);
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            orders = orderService.getAllOrderDTOs();
        }
        System.out.println("Số lượng đơn hàng trả về: " + orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/details/{orderId}")
    public ResponseEntity<OrderDetailsDTO> getOrderDetails(@PathVariable Long orderId) {
        System.out.println("Yêu cầu chi tiết đơn hàng cho admin: " + orderId);
        OrderDetailsDTO orderDetails = orderService.getOrderDetailsAsDTO(orderId);
        if (orderDetails == null) {
            System.out.println("Không tìm thấy đơn hàng: " + orderId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderDetails);
    }
}