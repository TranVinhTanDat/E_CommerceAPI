package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.DTO.OrderDetailsDTO;
import com.example.shoppecommerce.Entity.OrderStatus;
import com.example.shoppecommerce.Service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    @Autowired
    private OrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestParam(value = "date", required = false) String dateStr) {
        logger.info("Yêu cầu lấy tất cả đơn hàng, date: {}", dateStr);
        List<OrderDTO> orders;
        if (dateStr != null) {
            try {
                java.sql.Date date = java.sql.Date.valueOf(dateStr.trim());
                orders = orderService.getOrdersByDate(date);
            } catch (IllegalArgumentException e) {
                logger.error("Lỗi định dạng ngày: {}", dateStr, e);
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            orders = orderService.getAllOrderDTOs();
        }
        logger.info("Số lượng đơn hàng trả về: {}", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/details/{orderId}")
    public ResponseEntity<OrderDetailsDTO> getOrderDetails(@PathVariable Long orderId) {
        logger.info("Yêu cầu chi tiết đơn hàng cho admin: {}", orderId);
        try {
            OrderDetailsDTO orderDetails = orderService.getOrderDetailsAsDTO(orderId);
            if (orderDetails == null) {
                logger.warn("Không tìm thấy đơn hàng: {}", orderId);
                return ResponseEntity.notFound().build();
            }
            logger.info("Trả về chi tiết đơn hàng: {}", orderDetails.getId());
            return ResponseEntity.ok(orderDetails);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy chi tiết đơn hàng {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/new")
    public ResponseEntity<List<OrderDTO>> getNewOrders() {
        logger.info("Yêu cầu lấy danh sách đơn hàng mới...");
        try {
            List<OrderDTO> newOrders = orderService.getNewOrdersAsDTO();
            logger.info("Tìm thấy {} đơn hàng mới", newOrders.size());
            return ResponseEntity.ok(newOrders);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách đơn hàng mới: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    @GetMapping("/new-count")
    public ResponseEntity<Long> getNewOrdersCount() {
        logger.info("Yêu cầu đếm số lượng đơn hàng mới...");
        try {
            long count = orderService.getNewOrdersCount();
            logger.info("Số lượng đơn hàng mới: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Lỗi khi đếm số lượng đơn hàng mới: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(0L);
        }
    }
}