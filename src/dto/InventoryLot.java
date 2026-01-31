package dto;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryLot {

    // Enum status chuẩn cho module tồn kho / UI
    public enum Status {
        AVAILABLE,
        EXPIRED,
        DEPLETED
    }

    // =========================
    // ===== Core fields (DUY/HEAD schema) =====
    // =========================
    private int lotId;
    private int productId;
    private int grdId;

    private String lotCode;

    private LocalDate receivedDate;
    private LocalDate expiry;

    private int qtyIn;
    private int qtyOut;
    private int qtyRemaining;

    private LocalDateTime outOfStockAt;
    private Status status;
    private LocalDateTime createdAt;

    public InventoryLot() {}

    public InventoryLot(
            int lotId,
            int productId,
            int grdId,
            String lotCode,
            LocalDate receivedDate,
            LocalDate expiry,
            int qtyIn,
            int qtyOut,
            int qtyRemaining,
            LocalDateTime outOfStockAt,
            Status status,
            LocalDateTime createdAt
    ) {
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
    // ===== DUY getters/setters =====
    // =========================
    public int getLotId() { return lotId; }
    public void setLotId(int lotId) { this.lotId = lotId; }

    // HEAD/POS sometimes uses long lotId
    public void setLotId(long lotId) { this.lotId = (int) lotId; }
    public long getLotIdLong() { return (long) lotId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    // Some old code used long productId
    public void setProductId(long productId) { this.productId = (int) productId; }
    public long getProductIdLong() { return (long) productId; }

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

    // New code prefers enum
    public Status getStatusEnum() { return status; }
    public void setStatusEnum(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ==================================================
    // ===== COMPAT for HEAD/POS (java.sql.Date + String) =====
    // ==================================================

    // Some old code uses java.sql.Date
    public Date getReceivedDateSql() {
        return (receivedDate == null) ? null : Date.valueOf(receivedDate);
    }
    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = (receivedDate == null) ? null : receivedDate.toLocalDate();
    }

    // Old code uses getExpiryDate()/setExpiryDate(Date)
    public Date getExpiryDate() {
        return (expiry == null) ? null : Date.valueOf(expiry);
    }
    public void setExpiryDate(Date expiryDate) {
        this.expiry = (expiryDate == null) ? null : expiryDate.toLocalDate();
    }

    /**
     * Backward compatible status getter/setter using String.
     * NOTE: UI code uses lot.getStatus().name() -> so provide BOTH:
     * - getStatusEnum() for enum
     * - getStatus() returning enum for legacy UI calls
     */
    public Status getStatus() { // so code like lot.getStatus().name() works
        return status;
    }

    public void setStatus(Status status) { // allow setStatus(enum)
        this.status = status;
    }

    public String getStatusString() { // if some code expects String
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
            this.status = null;
        }
    }

    // ==================================================
    // ===== COMPAT for lesondowski schema (alias mapping) =====
    // ==================================================
    // quantity            -> qtyIn
    // remainingQuantity   -> qtyRemaining
    // importDate          -> receivedDate
    // expiryDate          -> expiry
    // importPrice         -> (NOT in this schema) -> keep field + getter/setter optional

    public int getQuantity() { return qtyIn; }
    public void setQuantity(int quantity) { this.qtyIn = quantity; }

    public int getRemainingQuantity() { return qtyRemaining; }
    public void setRemainingQuantity(int remainingQuantity) { this.qtyRemaining = remainingQuantity; }

    public LocalDate getImportDate() { return receivedDate; }
    public void setImportDate(LocalDate importDate) { this.receivedDate = importDate; }

    public LocalDate getExpiryDateLocal() { return expiry; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiry = expiryDate; }

    // importPrice doesn't exist in DUY schema -> keep as optional transient field
    private Double importPrice;
    public double getImportPrice() { return importPrice == null ? 0.0 : importPrice; }
    public void setImportPrice(double importPrice) { this.importPrice = importPrice; }
}
