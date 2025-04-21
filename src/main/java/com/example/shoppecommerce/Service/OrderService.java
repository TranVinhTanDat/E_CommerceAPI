package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.DTO.OrderDetailsDTO;
import com.example.shoppecommerce.DTO.OrderItemDTO;
import com.example.shoppecommerce.Entity.*;
import com.example.shoppecommerce.Repository.*;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AddressRepository addressRepository; // Thêm dependency này


    public List<OrderDTO> getAllOrderDTOs() {
        return orderRepository.findAllOrderDTOs();
    }

    @Transactional
    public Order placeOrder(Long userId, Long addressId) {
        logger.info("📦 Bắt đầu đặt hàng cho user ID: {}", userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found"));

        // Kiểm tra địa chỉ giao hàng
        Address shippingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Shipping address not found"));
        if (!shippingAddress.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to use this address");
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            logger.error("❌ Giỏ hàng trống, không thể đặt hàng!");
            throw new RuntimeException("No items in the cart");
        }

        logger.info("📦 Giỏ hàng (cart_id={}) có {} mặt hàng", cart.getId(), cartItems.size());

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress); // Gán địa chỉ giao hàng
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(BigDecimal.ZERO);

        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() < cartItem.getQuantity()) {
                logger.error("❌ Không đủ hàng cho sản phẩm: {}", product.getName());
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
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotal(total);
        orderRepository.save(order);

        // Xóa giỏ hàng
        logger.info("🗑️ Đang xóa {} mặt hàng trong giỏ hàng (cart_id={})!", cartItems.size(), cart.getId());
        cartItemRepository.deleteByCartId(cart.getId());
        entityManager.flush();
        entityManager.clear();

        // Kiểm tra lại giỏ hàng
        List<CartItem> remainingItems = cartItemRepository.findByCartId(cart.getId());
        if (!remainingItems.isEmpty()) {
            logger.error("❌ Vẫn còn {} mặt hàng trong giỏ hàng (cart_id={}) sau khi xóa", remainingItems.size(), cart.getId());
            for (CartItem item : remainingItems) {
                logger.error("Mặt hàng còn lại: id={}, productId={}, quantity={}",
                        item.getId(),
                        item.getProduct() != null ? item.getProduct().getId() : null,
                        item.getQuantity());
            }
            throw new RuntimeException("Failed to clear cart items");
        }

        logger.info("✅ Đã xóa toàn bộ giỏ hàng (cart_id={}) thành công!", cart.getId());

        try {
            emailService.sendOrderConfirmationEmail(user.getEmail(), order.getId().toString(), order.getTotal());
            logger.info("✅ Email xác nhận đơn hàng đã được gửi!");
        } catch (MessagingException e) {
            logger.error("❌ Lỗi khi gửi email xác nhận!", e);
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
                    return true;
                }
            }
        }
        return false;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.getItems().size();
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
    public void finalizeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if ("Temporary".equals(order.getStatus())) {
            order.setStatus(OrderStatus.PENDING);
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

        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new RuntimeException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        logger.info("Order ID {} cập nhật trạng thái: {}", orderId, newStatus);
    }

    private boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case PENDING: return next == OrderStatus.PROCESSING || next == OrderStatus.CANCELED;
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

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        return orderRepository.sumTotalAmount();
    }

    public long getNewUsersInMonth() {
        return userRepository.countNewUsersInMonth();
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public List<Object[]> getSalesByMonth() {
        return orderRepository.getSalesByMonth();
    }

    public long getNewOrdersCount() {
        return orderRepository.countNewOrdersByDate(List.of(OrderStatus.PENDING, OrderStatus.PROCESSING));
    }

    public List<Order> getNewOrders() {
        List<Order> newOrders = orderRepository.findByStatusInAndDate(List.of(OrderStatus.PENDING, OrderStatus.PROCESSING));
        newOrders.forEach(order -> {
            order.setUser(null);
            order.getItems().forEach(item -> {
                item.setOrder(null);
                item.getProduct().setCategory(null);
            });
        });
        return newOrders;
    }

    @Transactional
    public OrderDetailsDTO getOrderDetailsAsDTO(Long orderId) {
        System.out.println("Fetching order with ID: " + orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        System.out.println("Order found: ID=" + order.getId() + ", Total=" + order.getTotal() + ", Status=" + order.getStatus());
        order.getItems().size(); // Tải danh sách items (lazy loading)
        System.out.println("Order items count: " + order.getItems().size());

        OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO();
        orderDetailsDTO.setId(order.getId());
        System.out.println("Setting username: " + order.getUser().getUsername());
        orderDetailsDTO.setUserName(order.getUser().getUsername());
        orderDetailsDTO.setUserEmail(order.getUser().getEmail());
        orderDetailsDTO.setTotal(order.getTotal());
        orderDetailsDTO.setStatus(order.getStatus().name());
        orderDetailsDTO.setCreatedAt(order.getCreatedAt());
        orderDetailsDTO.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {
            System.out.println("Mapping order item: ID=" + item.getId() + ", ProductID=" + item.getProduct().getId());
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            itemDTO.setImage(item.getProduct().getImage());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            return itemDTO;
        }).collect(Collectors.toList());

        orderDetailsDTO.setItems(itemDTOs);
        System.out.println("Final OrderDetailsDTO: ID=" + orderDetailsDTO.getId() + ", Total=" + orderDetailsDTO.getTotal());
        return orderDetailsDTO;
    }
}