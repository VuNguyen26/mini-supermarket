package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product DTO - Khớp với bảng product trong database
 * product_id, barcode, product_name, category_id, brand_id, unit,
 * import_price, sale_price, stock_qty, min_stock, status, created_at, updated_at
 */
public class Product {
    private Integer productId;
    private String barcode;
    private String productName;
    private Integer categoryId;
    private String categoryName; // For display only (JOIN)
    private Integer brandId;
    private String brandName; // For display only (JOIN)
    private String unit;
    private BigDecimal importPrice;
    private BigDecimal salePrice;
    private Integer stockQty;
    private Integer minStock;
    private String status; // ACTIVE, INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {
        this.unit = "pcs";
        this.status = "ACTIVE";
        this.importPrice = BigDecimal.ZERO;
        this.salePrice = BigDecimal.ZERO;
        this.stockQty = 0;
        this.minStock = 0;
    }

    public Product(Integer productId, String barcode, String productName, Integer categoryId, Integer brandId) {
        this();
        this.productId = productId;
        this.barcode = barcode;
        this.productName = productName;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }

    // Getters and Setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isLowStock() {
        return stockQty != null && minStock != null && stockQty <= minStock;
    }

    @Override
    public String toString() {
        return productName + " (" + barcode + ")";
    }
}
