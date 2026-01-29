package dto;

import java.time.LocalDate;

public class InventoryLot {

    private long lotId;
    private long productId;
    private int quantity;
    private int remainingQuantity;
    private LocalDate importDate;
    private LocalDate expiryDate;
    private double importPrice;

    public InventoryLot() {
    }

    public InventoryLot(long lotId,
                        long productId,
                        int quantity,
                        int remainingQuantity,
                        LocalDate importDate,
                        LocalDate expiryDate,
                        double importPrice) {
        this.lotId = lotId;
        this.productId = productId;
        this.quantity = quantity;
        this.remainingQuantity = remainingQuantity;
        this.importDate = importDate;
        this.expiryDate = expiryDate;
        this.importPrice = importPrice;
    }

    public long getLotId() {
        return lotId;
    }

    public void setLotId(long lotId) {
        this.lotId = lotId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(double importPrice) {
        this.importPrice = importPrice;
    }
}
