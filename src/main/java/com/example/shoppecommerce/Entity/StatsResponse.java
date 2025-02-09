package com.example.shoppecommerce.Entity;

import java.math.BigDecimal;
import java.util.List;

public class StatsResponse {
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long newUsers;
    private long totalProducts;
    private List<Object[]> salesOverTime;

    public StatsResponse(long totalOrders, BigDecimal totalRevenue, long newUsers, long totalProducts, List<Object[]> salesOverTime) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.newUsers = newUsers;
        this.totalProducts = totalProducts;
        this.salesOverTime = salesOverTime;
    }

    // Getters and Setters
    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(long newUsers) {
        this.newUsers = newUsers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public List<Object[]> getSalesOverTime() {
        return salesOverTime;
    }

    public void setSalesOverTime(List<Object[]> salesOverTime) {
        this.salesOverTime = salesOverTime;
    }
}
