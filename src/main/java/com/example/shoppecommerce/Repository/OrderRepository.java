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

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(" +
            "o.id, u.username, a.addressLine1, a.addressLine2, a.phone, " +
            "o.status, o.total, 'CASH', o.createdAt, o.updatedAt) " +
            "FROM Order o " +
            "JOIN o.user u " +
            "LEFT JOIN o.shippingAddress a " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<OrderDTO> findByUserId(@Param("userId") Long userId);

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(" +
            "o.id, u.username, a.addressLine1, a.addressLine2, a.phone, " +
            "o.status, o.total, 'CASH', o.createdAt, o.updatedAt) " +
            "FROM Order o " +
            "JOIN o.user u " +
            "LEFT JOIN o.shippingAddress a " +
            "WHERE o.user.id = :userId AND o.status = :status " +
            "ORDER BY o.createdAt DESC")
    List<OrderDTO> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(" +
            "o.id, u.username, a.addressLine1, a.addressLine2, a.phone, " +
            "o.status, o.total, 'CASH', o.createdAt, o.updatedAt) " +
            "FROM Order o " +
            "JOIN o.user u " +
            "LEFT JOIN o.shippingAddress a " +
            "ORDER BY o.createdAt DESC")
    List<OrderDTO> findAllOrderDTOs();

    @Query("SELECT new com.example.shoppecommerce.DTO.OrderDTO(" +
            "o.id, u.username, a.addressLine1, a.addressLine2, a.phone, " +
            "o.status, o.total, 'CASH', o.createdAt, o.updatedAt) " +
            "FROM Order o " +
            "JOIN o.user u " +
            "LEFT JOIN o.shippingAddress a " +
            "WHERE FUNCTION('DATE', o.createdAt) = :date " +
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

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses AND FUNCTION('DATE', o.createdAt) = CURRENT_DATE")
    long countNewOrdersByDate(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses AND FUNCTION('DATE', o.createdAt) = CURRENT_DATE")
    List<Order> findByStatusInAndDate(@Param("statuses") List<OrderStatus> statuses);
}
