package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.*;
import com.example.shoppecommerce.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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
        order.setStatus("Processing");

        // Đặt giá trị mặc định cho thuộc tính total
        order.setTotal(BigDecimal.ZERO);

        // Save the order first to generate an ID
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
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

}
