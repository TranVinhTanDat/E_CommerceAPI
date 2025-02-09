package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.Cart;
import com.example.shoppecommerce.Entity.CartItem;
import com.example.shoppecommerce.Entity.CartItemRequest;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Service.CartService;
import com.example.shoppecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/view")
    public ResponseEntity<?> viewCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Cart cart = cartService.getCartByUserId(user.getId());
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cart.getItems());
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        cartService.clearCart(username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long itemId) {
        User user = userService.findByUsername(userDetails.getUsername());
        boolean success = cartService.removeItemFromCart(user.getId(), itemId);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Item removed successfully");
    }

    @PostMapping("/update/{itemId}")
    public ResponseEntity<?> updateCartItemQuantity(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long itemId, @RequestBody CartItemRequest cartItemRequest) {
        User user = userService.findByUsername(userDetails.getUsername());
        boolean success = cartService.updateCartItemQuantity(user.getId(), itemId, cartItemRequest.getQuantity());
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Cart item quantity updated successfully");
    }
}
