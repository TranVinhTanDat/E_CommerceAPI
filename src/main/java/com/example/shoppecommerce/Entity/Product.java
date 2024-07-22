package com.example.shoppecommerce.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String image;
    private int quantity; // Thêm dòng này

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Các mối quan hệ và phương thức khác
}
