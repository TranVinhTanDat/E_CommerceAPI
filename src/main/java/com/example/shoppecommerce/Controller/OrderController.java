package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.DTO.OrderDetailsDTO;
import com.example.shoppecommerce.Entity.Order;
import com.example.shoppecommerce.Entity.OrderStatus;
import com.example.shoppecommerce.Entity.StatsResponse;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Service.OrderService;
import com.example.shoppecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public ResponseEntity<?> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long addressId) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            Order order = orderService.placeOrder(user.getId(), addressId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "status", required = false) String status) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            List<OrderDTO> orders;
            if (status != null) {
                orders = orderService.getUserOrdersByStatusAsDTO(user.getId(), status);
            } else {
                orders = orderService.getUserOrdersAsDTO(user.getId());
            }

            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/details/{orderId}")
    public ResponseEntity<OrderDetailsDTO> getOrderDetails(@PathVariable Long orderId) {
        System.out.println("Received request for order details: " + orderId);
        try {
            OrderDetailsDTO orderDetails = orderService.getOrderDetailsAsDTO(orderId);
            System.out.println("Order details retrieved: " + orderDetails);
            return ResponseEntity.ok(orderDetails);
        } catch (RuntimeException e) {
            System.out.println("Error fetching order details: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/mark-as-processing/{orderId}")
    public ResponseEntity<Void> markOrderAsProcessing(@PathVariable Long orderId) {
        orderService.markOrderAsProcessing(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-bank-transfer/{orderId}")
    public ResponseEntity<String> confirmBankTransfer(@PathVariable Long orderId) {
        try {
            orderService.confirmBankTransfer(orderId);
            return ResponseEntity.ok("Order status updated to PROCESSING");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/mark-as-shipped/{orderId}")
    public ResponseEntity<String> markOrderAsShipped(@PathVariable Long orderId) {
        try {
            orderService.markOrderAsProcessing(orderId);
            return ResponseEntity.ok("Order status updated to SHIPPED");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/mark-as-delivered/{orderId}")
    public ResponseEntity<String> markOrderAsDelivered(@PathVariable Long orderId) {
        try {
            orderService.markOrderAsShipped(orderId);
            return ResponseEntity.ok("Order status updated to DELIVERED");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(@RequestParam(value = "date", required = false) String dateStr) {
        try {
            List<OrderDTO> orders;
            if (dateStr != null) {
                java.sql.Date date = java.sql.Date.valueOf(dateStr.trim());
                orders = orderService.getOrdersByDate(date);
            } else {
                orders = orderService.getAllOrderDTOs();
            }
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid date format: " + dateStr);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching orders: " + e.getMessage());
        }
    }

    @PostMapping("/update-status/{orderId}")
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long orderId, @RequestParam String newStatus) {
        try {
            OrderStatus statusEnum = OrderStatus.valueOf(newStatus.toUpperCase());
            orderService.updateOrderStatus(orderId, statusEnum);
            return ResponseEntity.ok("Order status updated to " + newStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid order status: " + newStatus);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/finalize/{orderId}")
    public ResponseEntity<Void> finalizeOrder(@PathVariable Long orderId) {
        orderService.finalizeOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all-shipper")
    public ResponseEntity<?> getAllOrdersShipper(@RequestParam(value = "date", required = false) String dateStr) {
        try {
            List<OrderDTO> orders;
            if (dateStr != null) {
                java.sql.Date date = java.sql.Date.valueOf(dateStr);
                orders = orderService.getOrdersByDate(date);
                if (orders.isEmpty()) {
                    System.out.println("No orders found for date: " + date);
                } else {
                    System.out.println("Orders found: " + orders.size());
                }
            } else {
                orders = orderService.getAllOrderDTOs();
            }
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid date format: " + dateStr);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching orders: " + e.getMessage());
        }
    }


    // API thống kê
    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics() {
        try {
            // Lấy dữ liệu thống kê
            long totalOrders = orderService.getTotalOrders();
            BigDecimal totalRevenue = orderService.getTotalRevenue();
            long newUsers = orderService.getNewUsersInMonth();
            long totalProducts = orderService.getTotalProducts();
            List<Object[]> salesOverTime = orderService.getSalesByMonth();

            // Đóng gói thành dữ liệu trả về
            return ResponseEntity.ok(new StatsResponse(totalOrders, totalRevenue, newUsers, totalProducts, salesOverTime));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching statistics");
        }
    }

    // Bổ sung endpoint để lấy số lượng đơn hàng mới hôm nay
    @GetMapping("/new-count")
    public ResponseEntity<Long> getNewOrdersCount() {
        try {
            long newOrdersCount = orderService.getNewOrdersCount();
            return ResponseEntity.ok(newOrdersCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }

    // Bổ sung endpoint để lấy danh sách đơn hàng mới hôm nay
    @GetMapping("/new")
    public ResponseEntity<List<Order>> getNewOrders() {
        try {
            List<Order> newOrders = orderService.getNewOrders();
            return ResponseEntity.ok(newOrders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

