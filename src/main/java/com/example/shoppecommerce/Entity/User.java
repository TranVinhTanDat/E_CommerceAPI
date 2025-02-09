package com.example.shoppecommerce.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String avatar;
    private String role;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;  // Trường createdAt

    // Các mối quan hệ và phương thức khác
    @PrePersist
    public void prePersist() {
        if (this.avatar == null || this.avatar.isEmpty()) {
            this.avatar = "https://t4.ftcdn.net/jpg/02/15/84/43/360_F_215844325_ttX9YiIIyeaR7Ne6EaLLjMAmy4GvPC69.jpg";
        }
        if (this.createdAt == null) {
            this.createdAt = new Date();  // Gán thời gian tạo cho người dùng mới
        }
    }
}
