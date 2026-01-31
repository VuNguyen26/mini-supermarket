package dto;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryLot {

    // (DUY) Enum status chuẩn cho module tồn kho
    public enum Status {
        AVAILABLE,
        EXPIRED,
        DEPLETED
    }

    // Core fields (DUY)
    private int lotId;
    private int productId;
    private int grdId;
    private String lotCode;
    private LocalDate receivedDate;
    private LocalDate expiry;            // expiry date
    private int qtyIn;
    private int qtyOut;
    private int qtyRemaining;
    private LocalDateTime outOfStockAt;
    private Status status;
    private LocalDateTime createdAt;

    public InventoryLot() {}

    public InventoryLot(int lotId, int productId, int grdId, String lotCode,
                        LocalDate receivedDate, LocalDate expiry,
                        int qtyIn, int qtyOut, int qtyRemaining,
                        LocalDateTime outOfStockAt, Status status, LocalDateTime createdAt) {
        this.lotId = lotId;
        this.productId = productId;
        this.grdId = grdId;
        this.lotCode = lotCode;
        this.receivedDate = receivedDate;
        this.expiry = expiry;
        this.qtyIn = qtyIn;
        this.qtyOut = qtyOut;
        this.qtyRemaining = qtyRemaining;
        this.outOfStockAt = outOfStockAt;
        this.status = status;
        this.createdAt = createdAt;
    }

    // =========================
    // ====== DUY getters ======
    // =========================

    public int getLotId() { return lotId; }
    public void setLotId(int lotId) { this.lotId = lotId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getGrdId() { return grdId; }
    public void setGrdId(int grdId) { this.grdId = grdId; }

    public String getLotCode() { return lotCode; }
    public void setLotCode(String lotCode) { this.lotCode = lotCode; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public LocalDate getExpiry() { return expiry; }
    public void setExpiry(LocalDate expiry) { this.expiry = expiry; }

    public int getQtyIn() { return qtyIn; }
    public void setQtyIn(int qtyIn) { this.qtyIn = qtyIn; }

    public int getQtyOut() { return qtyOut; }
    public void setQtyOut(int qtyOut) { this.qtyOut = qtyOut; }

    public int getQtyRemaining() { return qtyRemaining; }
    public void setQtyRemaining(int qtyRemaining) { this.qtyRemaining = qtyRemaining; }

    public LocalDateTime getOutOfStockAt() { return outOfStockAt; }
    public void setOutOfStockAt(LocalDateTime outOfStockAt) { this.outOfStockAt = outOfStockAt; }

    public Status getStatusEnum() { return status; }
    public void setStatusEnum(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ==================================================
    // ====== COMPAT for HEAD/POS (java.sql.Date + String)
    // ==================================================

    // HEAD used long lotId
    public long getLotIdLong() { return (long) lotId; }
    public void setLotId(long lotId) { this.lotId = (int) lotId; }

    // HEAD used java.sql.Date for receivedDate
    public Date getReceivedDateSql() {
        return (receivedDate == null) ? null : Date.valueOf(receivedDate);
    }
    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = (receivedDate == null) ? null : receivedDate.toLocalDate();
    }

    // HEAD used expiryDate java.sql.Date
    public Date getExpiryDate() {
        return (expiry == null) ? null : Date.valueOf(expiry);
    }
    public void setExpiryDate(Date expiryDate) {
        this.expiry = (expiryDate == null) ? null : expiryDate.toLocalDate();
    }

    // HEAD had getStatus()/setStatus(String)
    // Keep method name getStatus() returning String to avoid breaking old code
    public String getStatus() {
        return status == null ? null : status.name();
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            this.status = null;
            return;
        }
        try {
            this.status = Status.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            // fallback: unknown value => null (or AVAILABLE if you prefer)
            this.status = null;
        }
    }

    // If new code wants enum via the old method name
    public void setStatus(Status status) {
        this.status = status;
    }
}
