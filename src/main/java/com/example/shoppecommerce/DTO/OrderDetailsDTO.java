package com.example.shoppecommerce.DTO;

import com.example.shoppecommerce.Entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderDetailsDTO {
    private Long id;
    private String userName;
    private String userEmail;
    private BigDecimal total;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private List<OrderItemDTO> items;

    private String addressLine1;
    private String addressLine2;
    private String phone;
}