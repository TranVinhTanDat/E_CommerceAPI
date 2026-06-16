package com.example.shoppecommerce.Config;

import com.example.shoppecommerce.Entity.Category;
import com.example.shoppecommerce.Entity.Product;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.CategoryRepository;
import com.example.shoppecommerce.Repository.ProductRepository;
import com.example.shoppecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Optional;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        initAdminUser();
        initSampleData();
    }

    private void initAdminUser() {
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

    private void initSampleData() {
        // Chỉ seed khi chưa có sản phẩm nào (tránh trùng lặp khi restart)
        if (productRepository.count() > 0) {
            System.out.println("Products already exist, skipping sample data seeding.");
            return;
        }

        Category nhapKhau = saveCategory("Trái cây nhập khẩu");
        Category trongNuoc = saveCategory("Trái cây trong nước");
        Category theoMua  = saveCategory("Trái cây theo mùa");
        Category rauCu     = saveCategory("Rau củ hữu cơ");

        // image trỏ tới ảnh có sẵn trong public/img của frontend
        saveProduct("Táo Mỹ Envy", "Táo Envy nhập khẩu Mỹ, giòn ngọt, mọng nước.", 89000, "/img/fruite-item-1.jpg", 120, nhapKhau);
        saveProduct("Nho xanh không hạt", "Nho xanh Úc không hạt, vị ngọt thanh mát.", 129000, "/img/fruite-item-2.jpg", 80, nhapKhau);
        saveProduct("Cam Navel Úc", "Cam Navel mọng nước, giàu vitamin C.", 75000, "/img/fruite-item-3.jpg", 100, nhapKhau);
        saveProduct("Lê Hàn Quốc", "Lê Hàn Quốc giòn ngọt, vỏ mỏng.", 99000, "/img/fruite-item-4.jpg", 60, nhapKhau);

        saveProduct("Xoài cát Hòa Lộc", "Xoài cát Hòa Lộc đặc sản, thơm ngọt đậm đà.", 65000, "/img/fruite-item-5.jpg", 90, trongNuoc);
        saveProduct("Thanh long ruột đỏ", "Thanh long ruột đỏ Bình Thuận, ngọt mát.", 45000, "/img/fruite-item-6.jpg", 110, trongNuoc);
        saveProduct("Bưởi da xanh", "Bưởi da xanh Bến Tre, múi to, ngọt thanh.", 55000, "/img/best-product-1.jpg", 70, trongNuoc);
        saveProduct("Sầu riêng Ri6", "Sầu riêng Ri6 cơm vàng, béo ngậy.", 159000, "/img/best-product-2.jpg", 40, trongNuoc);

        saveProduct("Vải thiều Lục Ngạn", "Vải thiều Lục Ngạn theo mùa, ngọt sắc.", 60000, "/img/best-product-3.jpg", 85, theoMua);
        saveProduct("Măng cụt", "Măng cụt tươi, vị chua ngọt đặc trưng.", 95000, "/img/best-product-4.jpg", 50, theoMua);

        saveProduct("Cà chua bi hữu cơ", "Cà chua bi hữu cơ, sạch, an toàn.", 35000, "/img/best-product-5.jpg", 130, rauCu);
        saveProduct("Bơ sáp Đắk Lắk", "Bơ sáp Đắk Lắk dẻo béo, hạt nhỏ.", 70000, "/img/best-product-6.jpg", 75, rauCu);

        System.out.println("Sample data seeded: " + categoryRepository.count() + " categories, "
                + productRepository.count() + " products.");
    }

    private Category saveCategory(String name) {
        Category c = new Category();
        c.setName(name);
        return categoryRepository.save(c);
    }

    private void saveProduct(String name, String desc, long price, String image, int qty, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(BigDecimal.valueOf(price));
        p.setImage(image);
        p.setQuantity(qty);
        p.setCategory(category);
        productRepository.save(p);
    }
}
