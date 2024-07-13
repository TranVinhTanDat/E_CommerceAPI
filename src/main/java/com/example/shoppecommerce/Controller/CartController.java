package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.Cart;
import com.example.shoppecommerce.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = cartService.getUserIdFromUsername(userDetails.getUsername());
        Cart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addItemToCart(HttpServletRequest request,
                                              @RequestParam Long productId, @RequestParam int quantity) {
        Long userId = (Long) request.getAttribute("userId");
        Cart cart = cartService.addItemToCart(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }
}
