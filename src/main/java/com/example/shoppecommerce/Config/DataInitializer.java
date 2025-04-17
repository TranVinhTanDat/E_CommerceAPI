package com.example.shoppecommerce.Config;

import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdminUser() {
        // Kiểm tra xem có tài khoản admin nào trong database chưa
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin.isPresent()) {
            System.out.println("Admin user already exists, skipping creation.");
            return; // Nếu đã có admin, không tạo mới
        }

        // Tạo tài khoản admin mặc định
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123")); // Mật khẩu: admin123
        admin.setEmail("admin@shoppecommerce.com");
        admin.setRole("ADMIN");
        admin.setAvatar("https://t4.ftcdn.net/jpg/02/15/84/43/360_F_215844325_ttX9YiIIyeaR7Ne6EaLLjMAmy4GvPC69.jpg");

        userRepository.save(admin);
        System.out.println("Admin user created with username: admin, password: admin123");
    }
}