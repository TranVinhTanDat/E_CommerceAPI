package com.example.shoppecommerce.Repository;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(o.id, u.username, a.addressLine1, a.addressLine2, a.phone, o.status, o.total, p.paymentMethod) " +
            "FROM Order o JOIN o.user u JOIN Address a ON a.user.id = u.id JOIN Payment p ON p.order.id = o.id " +
            "ORDER BY o.createdAt DESC")
    List<OrderDTO> findAllOrderDTOs();

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(o.id, u.username, a.addressLine1, a.addressLine2, a.phone, o.status, o.total, p.paymentMethod) " +
            "FROM Order o JOIN o.user u JOIN Address a ON a.user.id = u.id JOIN Payment p ON p.order.id = o.id " +
            "WHERE DATE(o.createdAt) = :date " +
            "ORDER BY o.createdAt DESC")
    List<OrderDTO> findOrdersByDate(@Param("date") java.sql.Date date);
}
