package dto;
// package your.package.dto;

import java.time.LocalDate;

public class ReportInventoryExpiryRow {
    private String productCode;
    private String productName;
    private LocalDate expiryDate;
    private int remainingQuantity;
    private String bucket; // HẾT HẠN / ≤ 7 NGÀY / ≤ 30 NGÀY / BÌNH THƯỜNG / KHÔNG HSD

    public ReportInventoryExpiryRow() {
    }

    public ReportInventoryExpiryRow(String productCode, String productName, LocalDate expiryDate, int remainingQuantity, String bucket) {

        this.productCode = productCode;
        this.productName = productName;
        this.expiryDate = expiryDate;
        this.remainingQuantity = remainingQuantity;
        this.bucket = bucket;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
