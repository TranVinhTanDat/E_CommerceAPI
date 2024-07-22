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

        // Tìm sản phẩm theo ID
        Product product = productRepository.findById(cartItemRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra số lượng sản phẩm tồn kho
        if (product.getQuantity() < cartItemRequest.getQuantity()) {
            throw new RuntimeException("Not enough stock for product: " + product.getName());
        }

        // Tìm giỏ hàng của người dùng, nếu không có thì tạo mới
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        });

        // Kiểm tra xem sản phẩm đã tồn tại trong giỏ hàng hay chưa
        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(cartItemRequest.getProductId()))
                .findFirst();

        if (existingCartItem.isPresent()) {
            // Cập nhật số lượng nếu sản phẩm đã tồn tại trong giỏ hàng
            int newQuantity = existingCartItem.get().getQuantity() + cartItemRequest.getQuantity();
            if (product.getQuantity() < newQuantity) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            existingCartItem.get().setQuantity(newQuantity);
        } else {
            // Nếu sản phẩm chưa tồn tại trong giỏ hàng, thêm sản phẩm mới vào giỏ hàng
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemRequest.getQuantity());
            cart.getItems().add(cartItem);
        }

        // Cập nhật số lượng tồn kho của sản phẩm
        product.setQuantity(product.getQuantity() - cartItemRequest.getQuantity());
        productRepository.save(product);

        // Lưu giỏ hàng và các mặt hàng
        cartRepository.save(cart);
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public boolean removeItemFromCart(Long userId, Long itemId) {
        Optional<CartItem> item = cartItemRepository.findById(itemId);
        if (item.isPresent() && item.get().getCart().getUser().getId().equals(userId)) {
            Product product = item.get().getProduct();
            product.setQuantity(product.getQuantity() + item.get().getQuantity());
            productRepository.save(product);

            cartItemRepository.delete(item.get());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateCartItemQuantity(Long userId, Long itemId, int quantity) {
        Optional<CartItem> item = cartItemRepository.findById(itemId);
        if (item.isPresent() && item.get().getCart().getUser().getId().equals(userId)) {
            Product product = item.get().getProduct();
            int quantityDifference = quantity - item.get().getQuantity();
            if (product.getQuantity() < quantityDifference) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            item.get().setQuantity(quantity);
            product.setQuantity(product.getQuantity() - quantityDifference);
            productRepository.save(product);

            cartItemRepository.save(item.get());
            return true;
        }
        return false;
    }

    @Transactional
    public void clearCart(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            Cart cart = cartRepository.findByUserId(user.get().getId()).orElse(null);
            if (cart != null) {
                cartItemRepository.deleteByCartId(cart.getId());
            }
        }
    }
}
