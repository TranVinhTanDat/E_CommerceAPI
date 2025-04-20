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

    @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product p JOIN FETCH p.category WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product p JOIN FETCH p.category WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(" +
            "o.id, u.username, a.addressLine1, a.addressLine2, a.phone, " +
            "o.status, o.total, 'CASH') " +
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
    BigDecimal sumTotalAmount();

    @Query("SELECT COUNT(o) FROM Order o")
    long count();

    @Query("SELECT FUNCTION('MONTH', o.createdAt), SUM(o.total) FROM Order o GROUP BY FUNCTION('MONTH', o.createdAt)")
    List<Object[]> getSalesByMonth();

    long countByStatusIn(List<OrderStatus> statuses);

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    // Bổ sung để hỗ trợ đơn hàng mới hôm nay
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses AND DATE(o.createdAt) = CURRENT_DATE")
    long countNewOrdersByDate(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses AND DATE(o.createdAt) = CURRENT_DATE")
    List<Order> findByStatusInAndDate(@Param("statuses") List<OrderStatus> statuses);
}