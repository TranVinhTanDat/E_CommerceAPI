package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.Entity.*;
import com.example.shoppecommerce.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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

    public List<OrderDTO> getAllOrderDTOs() {
        return orderRepository.findAllOrderDTOs();
    }

    @Transactional
    public Order placeOrder(Long userId) {
        logger.info("Placing order for user ID: {}", userId);

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
        order.setStatus("Pending");  // Set status to Pending

        // Đặt giá trị mặc định cho thuộc tính total
        order.setTotal(BigDecimal.ZERO);

        // Save the order first to generate an ID
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() < cartItem.getQuantity()) {
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
            logger.info("Added product ID: {} to order with quantity: {}", product.getId(), cartItem.getQuantity());
        }

        order.setTotal(total);
        orderRepository.save(order); // Update the order with the total amount
        logger.info("Order placed successfully for user ID: {} with total: {}", userId, total);

        cartItemRepository.deleteAll(cartItems);
        logger.info("Cart cleared for user ID: {}", userId);

        return order;
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
        order.setStatus("Temporary");  // Set status to Temporary

        order.setTotal(BigDecimal.ZERO);

        // Save the order first to generate an ID
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() < cartItem.getQuantity()) {
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
            order.setStatus("Pending"); // Update status to Pending
            orderRepository.save(order);
            logger.info("Order finalized and set to pending for order ID: {}", orderId);
        } else {
            logger.error("Cannot finalize order with status: {}", order.getStatus());
            throw new RuntimeException("Order status is not temporary");
        }
    }

    public void rejectBankTransfer(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if ("Payment Confirmed".equals(order.getStatus())) {
            order.setStatus("Payment Rejected");
            orderRepository.save(order);
            logger.info("Payment rejected for order ID: {}", orderId);
        } else {
            logger.error("Cannot reject payment for order with status: {}", order.getStatus());
            throw new RuntimeException("Order status is not Payment Confirmed");
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


}
