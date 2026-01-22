package dto;


import java.math.BigDecimal;


public class ReportProduct {
    private int productId;
    private String productName;
    private long totalQuantity;
    private BigDecimal totalRevenue;

    public ReportProduct(int productId, String productName, long totalQuantity, BigDecimal totalRevenue){
        this.productId = productId;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }

}