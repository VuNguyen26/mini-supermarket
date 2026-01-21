package dto;

public class Customer {
    private int customerId;
    private String customerName;
    private String phone;
    private String address;
    private int points;

    public Customer() {
    }

    // Default customer is "Khach' le?"
    public Customer(int customerId, String customerName, String phone) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = ""; 
        this.points = 0;   
    }

    public Customer(int customerId, String customerName, String phone, String address, int points) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.points = points;
    }

    public int getCustomerId() {
        return customerId;
    }

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
        this.address = address;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        if (customerId == 0) return "Khách lẻ";
        return customerName + " - " + (phone != null ? phone : "");
    }
}