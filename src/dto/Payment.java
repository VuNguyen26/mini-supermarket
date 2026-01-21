package dto;

import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private int invId;
    private String method;
    private double amount;
    private Timestamp paidAt;
    private String status;
    private String note;

    public Payment() {
    }

    public Payment(int invId, String method, double amount, String note) {
        this.invId = invId;
        this.method = method;
        this.amount = amount;
        this.note = note;
        this.status = "PAID";
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getInvId() {
        return invId;
    }

    public void setInvId(int invId) {
        this.invId = invId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}