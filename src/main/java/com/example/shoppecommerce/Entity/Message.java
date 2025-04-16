package com.example.shoppecommerce.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp = LocalDateTime.now();
    private boolean isRead = false; // Thêm trường isRead, mặc định là false (chưa đọc)
}