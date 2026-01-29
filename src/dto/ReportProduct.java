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
    public Integer getProductId(){
        return productId;
    }
    public void setProductId(Integer productId){
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

