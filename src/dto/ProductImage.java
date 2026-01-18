package dto;

import java.time.LocalDateTime;

public class ProductImage {
    private Integer imageId;
    private Integer productId;
    private String imagePath;
    private Boolean isPrimary;
    private LocalDateTime createdAt;

    public ProductImage() {}

    public ProductImage(Integer imageId, Integer productId, String imagePath, Boolean isPrimary) {
        this.imageId = imageId;
        this.productId = productId;
        this.imagePath = imagePath;
        this.isPrimary = isPrimary;
    }

    // Getters and Setters
    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
