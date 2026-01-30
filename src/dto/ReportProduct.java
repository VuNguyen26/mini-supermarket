package dto;


import java.math.BigDecimal;


public class ReportProduct {
    private String productId;
    private String productName;
    private long totalQuantity;
    private BigDecimal totalRevenue;

    public ReportProduct(String productId, String productName, long totalQuantity, BigDecimal totalRevenue){
        this.productId = productId;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }
    public String getProductId(){
        return productId;
    }
    public void setProductId(String productId){
        this.productId = productId;
    }

    public String getProductName(){
        return productName;
    }
    public void setProductName(String productName){
        this.productName = productName;
    }
    public Long getTotalQuantity(){
        return totalQuantity;
    }
    public void setTotalQuantity(Long totalQuantity){
        this.totalQuantity = totalQuantity;
    }
    public BigDecimal getTotalRevenue(){
        return totalRevenue;
    }
    public void setTotalRevenue(BigDecimal totalRevenue){
        this.totalRevenue = totalRevenue;
    }
}

