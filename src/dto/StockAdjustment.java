package dto;

import java.time.LocalDateTime;

public class StockAdjustment {

    private int saId;
    private String saCode;
    private int createdBy;
    private LocalDateTime createdAt;
    private StockAdjustmentReason reason;
    private StockAdjustmentStatus status;
    private String note;

    public StockAdjustment() {}

    public StockAdjustment(int saId, String saCode, int createdBy,
                           LocalDateTime createdAt,
                           StockAdjustmentReason reason,
                           StockAdjustmentStatus status,
                           String note) {
        this.saId = saId;
        this.saCode = saCode;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.reason = reason;
        this.status = status;
        this.note = note;
    }

    public int getSaId() {
        return saId;
    }

    public void setSaId(int saId) {
        this.saId = saId;
    }

    public String getSaCode() {
        return saCode;
    }

    public void setSaCode(String saCode) {
        this.saCode = saCode;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public StockAdjustmentReason getReason() {
        return reason;
    }

    public void setReason(StockAdjustmentReason reason) {
        this.reason = reason;
    }

    public StockAdjustmentStatus getStatus() {
        return status;
    }

    public void setStatus(StockAdjustmentStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}