package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.*;
import com.example.shoppecommerce.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Transactional
    public void addProductToCart(Long userId, CartItemRequest cartItemRequest) {
        // Find user by userId
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // // Tìm sản phẩm theo ID
        Product product = productRepository.findById(cartItemRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

        // Tìm giỏ hàng của người dùng, nếu không có thì tạo mới
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart); //  // Lưu giỏ hàng mới
        });

        // Kiểm tra xem sản phẩm đã tồn tại trong giỏ hàng hay chưa
        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(cartItemRequest.getProductId()))
                .findFirst();

        if (existingCartItem.isPresent()) {
            // Cập nhật số lượng nếu sản phẩm đã tồn tại trong giỏ hàng
            existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + cartItemRequest.getQuantity());
        } else {
            // If the product is not in the cart, add a new item (Thêm sản phẩm mới vào giỏ hàng)
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemRequest.getQuantity());
            cart.getItems().add(cartItem);
        }

        // Save the cart and items
        cartRepository.save(cart);
    }


    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public boolean removeItemFromCart(Long userId, Long itemId) {
        Optional<CartItem> item = cartItemRepository.findById(itemId);
        if (item.isPresent() && item.get().getCart().getUser().getId().equals(userId)) {
            cartItemRepository.delete(item.get());
            return true;
        }
        return false;
    }

}
