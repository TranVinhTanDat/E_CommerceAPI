package com.example.shoppecommerce.Repository;

import com.example.shoppecommerce.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // Thêm phương thức tìm kiếm theo email
}
