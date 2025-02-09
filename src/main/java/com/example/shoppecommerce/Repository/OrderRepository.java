package com.example.shoppecommerce.Repository;

import com.example.shoppecommerce.DTO.OrderDTO;
import com.example.shoppecommerce.Entity.Order;
import com.example.shoppecommerce.Entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(" +
            "o.id, u.username, a.addressLine1, a.addressLine2, a.phone, " +
            "o.status, o.total, 'CASH') " + // ✅ Tránh lỗi nếu paymentMethod null
            "FROM Order o " +
            "JOIN o.user u " +
            "JOIN Address a ON a.user.id = u.id " +
            "ORDER BY o.createdAt DESC")
    List<OrderDTO> findAllOrderDTOs();

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(" +
            "o.id, u.username, a.addressLine1, a.addressLine2, a.phone, " +
            "o.status, o.total, 'CASH') " +
            "FROM Order o " +
            "JOIN o.user u " +
            "JOIN Address a ON a.user.id = u.id " +
            "WHERE DATE(o.createdAt) = :date " +
            "ORDER BY o.createdAt DESC")
    List<OrderDTO> findOrdersByDate(@Param("date") Date date);

    @Query("SELECT SUM(o.total) FROM Order o")
    BigDecimal sumTotalAmount();  // Tổng doanh thu

    @Query("SELECT COUNT(o) FROM Order o")
    long count(); // Tổng số đơn hàng

    @Query("SELECT FUNCTION('MONTH', o.createdAt), SUM(o.total) FROM Order o GROUP BY FUNCTION('MONTH', o.createdAt)")
    List<Object[]> getSalesByMonth(); // Doanh thu theo tháng
}
