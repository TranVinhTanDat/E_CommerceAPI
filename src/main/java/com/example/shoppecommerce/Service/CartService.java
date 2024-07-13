package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.Cart;
import com.example.shoppecommerce.Entity.CartItem;
import com.example.shoppecommerce.Entity.Product;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.CartItemRepository;
import com.example.shoppecommerce.Repository.CartRepository;
import com.example.shoppecommerce.Repository.ProductRepository;
import com.example.shoppecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart addItemToCart(Long userId, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart = cartRepository.save(cart);
        }

        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));

        cart.getItems().add(cartItem);

        return cartRepository.save(cart);
    }

    public Long getUserIdFromUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
