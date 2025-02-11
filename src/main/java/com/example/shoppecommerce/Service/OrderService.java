package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.Entity.*;
import com.example.shoppecommerce.Repository.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public List<OrderDTO> getAllOrderDTOs() {
        return orderRepository.findAllOrderDTOs();
    }

    @Transactional
    public Order placeOrder(Long userId) {
        logger.info("📦 Bắt đầu đặt hàng cho user ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> {
            logger.error("❌ Không tìm thấy giỏ hàng!");
            return new RuntimeException("Cart not found");
        });

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        // Kiểm tra giỏ hàng trống
        if (cartItems.isEmpty()) {
            logger.warn("⚠️ Không có sản phẩm nào trong giỏ hàng!");
            throw new RuntimeException("No items in the cart to place the order");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(BigDecimal.ZERO);

        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            // Cập nhật số lượng sản phẩm
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            total = total.add(orderItem.getPrice());
            orderItems.add(orderItem);
        }

        // Cập nhật thông tin đơn hàng
        order.setItems(orderItems);
        order.setTotal(total);
        orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt thành công
        cartItemRepository.deleteAll(cartItems);
        logger.info("🗑️ Giỏ hàng đã được xóa sau khi đặt hàng thành công!");

        // Gửi email thông báo cho người dùng
        try {
            emailService.sendOrderConfirmationEmail(user.getEmail(), order.getId().toString(), order.getTotal());
            logger.info("✅ Email thông báo đơn hàng đã được gửi thành công cho user ID: {}", userId);
        } catch (MessagingException e) {
            logger.error("❌ Lỗi khi gửi email thông báo cho user ID: {}", userId, e);
        }

        return order;
    }


    public List<Order> getUserOrdersByStatus(Long userId, String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByUserIdAndStatus(userId, orderStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }


    public boolean hasUserPurchasedProduct(Long userId, Long productId) {
        List<Order> deliveredOrders = orderRepository.findByUserIdAndStatus(userId, OrderStatus.DELIVERED);
        for (Order order : deliveredOrders) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct().getId().equals(productId)) {
                    return true; // ✅ User đã mua sản phẩm và đã giao hàng
                }
            }
        }
        return false; // ❌ User chưa mua sản phẩm hoặc chưa nhận hàng
    }


    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        for (Order order : orders) {
            order.getItems().size(); // Ensure items are loaded
        }
        return orders;
    }


    public Order getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.getItems().size(); // Ensure items are loaded
        return order;
    }

    public void markOrderAsShipped(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.SHIPPED) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Order is not in SHIPPED state, cannot mark as DELIVERED.");
        }
    }

    public void markOrderAsProcessing(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.PROCESSING) {
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Order is not in PROCESSING state, cannot mark as SHIPPED.");
        }
    }

    public void confirmBankTransfer(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Order is not in PENDING state, cannot confirm payment.");
        }
    }


    @Transactional
    public Order placeTemporaryOrder(Long userId) {
        logger.info("Placing temporary order for user ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> {
            logger.error("Cart not found for user ID: {}", userId);
            return new RuntimeException("Cart not found");
        });

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            logger.warn("No items in the cart to place the order for user ID: {}", userId);
            throw new RuntimeException("No items in the cart to place the order");
        }

        Order order = new Order();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.error("User not found for ID: {}", userId);
            throw new RuntimeException("User not found");
        }
        order.setUser(user);
        order.setStatus(OrderStatus.TEMPORARY);  // Set status to Temporary

        order.setTotal(BigDecimal.ZERO);

        // Save the order first to generate an ID
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() < cartItem.getQuantity()) {
                logger.error("Not enough stock for product: {}", product.getName());
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            total = total.add(orderItem.getPrice());

            orderItemRepository.save(orderItem);
            logger.info("Added product ID: {} to temporary order with quantity: {}", product.getId(), cartItem.getQuantity());
        }

        order.setTotal(total);
        orderRepository.save(order); // Update the order with the total amount
        logger.info("Temporary order placed successfully for user ID: {} with total: {}", userId, total);

        return order;
    }


    @Transactional
    public void finalizeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if ("Temporary".equals(order.getStatus())) {
            order.setStatus(OrderStatus.PENDING); // Update status to Pending
            orderRepository.save(order);
            logger.info("Order finalized and set to pending for order ID: {}", orderId);
        } else {
            logger.error("Cannot finalize order with status: {}", order.getStatus());
            throw new RuntimeException("Order status is not temporary");
        }
    }



    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        logger.info("Order ID {} updated from {} to {}", orderId, currentStatus, newStatus);
    }

    private boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case PENDING: return next == OrderStatus.PROCESSING || next == OrderStatus.CANCELED; // ✅ Cho phép hủy
            case PROCESSING: return next == OrderStatus.SHIPPED;
            case SHIPPED: return next == OrderStatus.DELIVERED;
            case DELIVERED: return next == OrderStatus.REFUNDED;
            default: return false;
        }
    }


    public List<OrderDTO> getOrdersByDate(java.sql.Date date) {
        List<OrderDTO> orders = orderRepository.findOrdersByDate(date);
        if (orders.isEmpty()) {
            System.out.println("No orders found for date: " + date);
        } else {
            System.out.println("Orders found: " + orders.size());
        }
        return orders;
    }



    // Thống kê tổng số đơn hàng trong tháng
    public long getTotalOrders() {
        return orderRepository.count();
    }

    // Thống kê tổng doanh thu trong tháng
    public BigDecimal getTotalRevenue() {
        return orderRepository.sumTotalAmount(); // Giả sử bạn có phương thức sumTotalAmount() trong OrderRepository
    }

    // Thống kê số người dùng mới trong tháng
    public long getNewUsersInMonth() {
        return userRepository.countNewUsersInMonth(); // Giả sử bạn có phương thức countNewUsersInMonth() trong UserRepository
    }

    // Thống kê tổng số sản phẩm
    public long getTotalProducts() {
        return productRepository.count();
    }

    // Thống kê doanh thu theo tháng
    public List<Object[]> getSalesByMonth() {
        return orderRepository.getSalesByMonth(); // Giả sử bạn có một truy vấn cho việc này
    }
}
