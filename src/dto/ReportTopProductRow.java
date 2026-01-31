package dto;

import java.math.BigDecimal;

public class ReportTopProductRow {
    private String productCode;
    private String productName;
    private int totalQuantity;
    private BigDecimal totalRevenue;

    public ReportTopProductRow() {
    }

    public ReportTopProductRow(String productCode, String productName, int totalQuantity, BigDecimal totalRevenue) {
        this.productCode = productCode;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
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

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
