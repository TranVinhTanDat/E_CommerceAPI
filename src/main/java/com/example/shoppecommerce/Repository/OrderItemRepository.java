package com.example.shoppecommerce.Repository;

import com.example.shoppecommerce.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
