package com.example.shoppecommerce.Repository;

import com.example.shoppecommerce.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
