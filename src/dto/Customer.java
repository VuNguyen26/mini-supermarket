package dto;

import java.time.LocalDateTime;

/**
 * Customer DTO - Khớp với bảng customer trong database
 * customer_id, customer_name, phone, address, points, created_at
 */
public class Customer {
    private Integer customerId;
    private String customerName;  // Tên field trong DB
    private String phone;
    private String address;
    private Integer points;  // Điểm tích lũy
    private LocalDateTime createdAt;

    public Customer() {
        this.points = 0;
    }

    public Customer(Integer customerId, String customerName, String phone, String address, Integer points, LocalDateTime createdAt) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.points = points;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return null; // Placeholder if needed
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        // Placeholder if needed
    }

    @Override
    public String toString() {
        return customerName + " (" + phone + ") - " + points + " điểm";
    }
}
