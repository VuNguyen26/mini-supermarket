package dto;

import java.sql.Timestamp;

public class SalesInvoice {
    private int invId;
    private int customerId;
    private int createdBy;
    private Timestamp createdAt;
    private double subTotal;
    private double discount;
    private double grandTotal;
    private String paymentMethod;

    public SalesInvoice() {
    }

    public SalesInvoice(int customerId, int createdBy, double subTotal, double discount, double grandTotal,
            String paymentMethod) {
        this.customerId = customerId;
        this.createdBy = createdBy;
        this.subTotal = subTotal;
        this.discount = discount;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
    }

    public int getInvId() {
        return invId;
    }

    public void setInvId(int invId) {
        this.invId = invId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}