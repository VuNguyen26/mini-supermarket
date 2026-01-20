package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Promotion {

    private int promoId;
    private String promoCode;
    private String promoName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private PromotionType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private String status;
    private int createdBy;
    private LocalDateTime createdAt;

    public Promotion() {}

    public Promotion(int promoId, String promoCode, String promoName,
                     LocalDateTime startAt, LocalDateTime endAt,
                     PromotionType type, BigDecimal value, BigDecimal minOrderAmount,
                     String status, int createdBy, LocalDateTime createdAt) {
        this.promoId = promoId;
        this.promoCode = promoCode;
        this.promoName = promoName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.type = type;
        this.value = value;
        this.minOrderAmount = minOrderAmount;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public int getPromoId() {
        return promoId;
    }

    public void setPromoId(int promoId) {
        this.promoId = promoId;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public PromotionType getType() {
        return type;
    }

    public void setType(PromotionType type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}