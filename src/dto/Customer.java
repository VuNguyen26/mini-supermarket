package dto;

import java.time.LocalDateTime;

/**
 * Customer DTO - Khớp với bảng customer trong database
 * customer_id, customer_name, phone, address, points, created_at
 */
public class Customer {
    private Integer customerId;
    private String customerName;
    private String phone;
    private String address;
    private Integer points;
    private LocalDateTime createdAt;

    public Customer() {
        this.points = 0;
        this.address = "";
    }

    // Full constructor (HEAD)
    public Customer(Integer customerId, String customerName, String phone, String address, Integer points, LocalDateTime createdAt) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address != null ? address : "";
        this.points = points != null ? points : 0;
        this.createdAt = createdAt;
    }

    // Constructor compatible with origin/huynh: default customer / "Khách lẻ"
    public Customer(int customerId, String customerName, String phone) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = "";
        this.points = 0;
        this.createdAt = null;
    }

    // Constructor compatible with origin/huynh
    public Customer(int customerId, String customerName, String phone, String address, int points) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address != null ? address : "";
        this.points = points;
        this.createdAt = null;
    }

    // Extra: 5-params Integer constructor (useful for DAO)
    public Customer(Integer customerId, String customerName, String phone, String address, Integer points) {
        this(customerId, customerName, phone, address, points, null);
    }

    // ========== Getters/Setters ==========
    public Integer getCustomerId() {
        return customerId;
    }

    // Alias getter (safe int) for code that expects primitive
    public int getCustomerIdValue() {
        return customerId != null ? customerId : 0;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    // Alias setter for primitive int
    public void setCustomerId(int customerId) {
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
        this.address = address != null ? address : "";
    }

    public Integer getPoints() {
        return points;
    }

    // Alias getter for primitive int
    public int getPointsValue() {
        return points != null ? points : 0;
    }

    public void setPoints(Integer points) {
        this.points = points != null ? points : 0;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        // Keep behavior from huynh: id=0 shows "Khách lẻ"
        if (getCustomerIdValue() == 0) return "Khách lẻ";
        String p = (phone != null) ? phone : "";
        return customerName + " - " + p;
    }
}
