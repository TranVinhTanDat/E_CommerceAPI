package com.example.shoppecommerce.DTO;

import com.example.shoppecommerce.Entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
    private Long orderId;
    private String userName;
    private String addressLine1;
    private String addressLine2;
    private String phone;
    private String status;
    private BigDecimal total;
    private String paymentMethod;
    private List<OrderItemDTO> items; // Thêm field này

    public OrderDTO(Long orderId, String userName, String addressLine1, String addressLine2,
                    String phone, OrderStatus status, BigDecimal total, String paymentMethod) {
        this.orderId = orderId;
        this.userName = userName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.phone = phone;
        this.status = status != null ? status.name() : null;
        this.total = total;
        this.paymentMethod = paymentMethod;
    }
}