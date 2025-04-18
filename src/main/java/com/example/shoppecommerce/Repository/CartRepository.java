package com.example.shoppecommerce.Repository;

import com.example.shoppecommerce.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);


}