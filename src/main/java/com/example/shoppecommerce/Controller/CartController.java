package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.CartItemRequest;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Service.CartService;
import com.example.shoppecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal UserDetails userDetails, @RequestBody CartItemRequest cartItemRequest) {
        User authenticatedUser = userService.findByUsername(userDetails.getUsername());
        Long userId = authenticatedUser.getId();
        cartService.addProductToCart(userId, cartItemRequest);
        return ResponseEntity.ok("Product added to cart");
    }
}
