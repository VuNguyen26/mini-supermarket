package dto;

import java.sql.Date;

public class InventoryLot {
    private long lotId;
    private int productId;
    private String lotCode;
    private Date receivedDate;
    private Date expiryDate;
    private int qtyIn;
    private int qtyRemaining;
    private String status;

    public InventoryLot() {
    }

    public long getLotId() {
        return lotId;
    }

    public void setLotId(long lotId) {
        this.lotId = lotId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getLotCode() {
        return lotCode;
    }

    public void setLotCode(String lotCode) {
        this.lotCode = lotCode;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQtyIn() {
        return qtyIn;
    }

    public void setQtyIn(int qtyIn) {
        this.qtyIn = qtyIn;
    }

    public int getQtyRemaining() {
        return qtyRemaining;
    }

    public void setQtyRemaining(int qtyRemaining) {
        this.qtyRemaining = qtyRemaining;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}