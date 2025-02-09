package com.example.shoppecommerce.Entity;

public enum OrderStatus {
    TEMPORARY,    // Đơn hàng tạm, chưa thanh toán
    PENDING,      // Đã đặt hàng, chờ xử lý
    PROCESSING,   // Đang xử lý, chuẩn bị giao hàng
    SHIPPED,      // Đang vận chuyển
    DELIVERED,    // Đã giao hàng thành công
    CANCELED,     // Đơn hàng bị hủy
    REFUNDED      // Đơn hàng được hoàn tiền
}
