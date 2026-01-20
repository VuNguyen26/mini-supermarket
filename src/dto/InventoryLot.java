package dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryLot {
	public enum Status {
		AVAILABLE,
		EXPIRED,
		DEPLETED
	}

	private int lotId;
	private int productId;
	private int grdId;
	private String lotCode;
	private LocalDate receivedDate;
	private LocalDate expiry;
	private int qtyIn;
	private int qtyOut;
	private int qtyRemaining;
	private LocalDateTime outOfStockAt;
	private Status status;
	private LocalDateTime createdAt;

	public InventoryLot() {}

	public InventoryLot(int lotId, int productId, int grdId, String lotCode, LocalDate receivedDate, LocalDate expiry,
			int qtyIn, int qtyOut, int qtyRemaining, LocalDateTime outOfStockAt, Status status, LocalDateTime createdAt) {
		this.lotId = lotId;
		this.productId = productId;
		this.grdId = grdId;
		this.lotCode = lotCode;
		this.receivedDate = receivedDate;
		this.expiry = expiry;
		this.qtyIn = qtyIn;
		this.qtyOut = qtyOut;
		this.qtyRemaining = qtyRemaining;
		this.outOfStockAt = outOfStockAt;
		this.status = status;
		this.createdAt = createdAt;
	}
	public int getLotId() { return lotId; }
	public void setLotId(int lotId) { this.lotId = lotId; }

	public int getProductId() { return productId; }
	public void setProductId(int productId) { this.productId = productId; }

	public int getGrdId() { return grdId; }
	public void setGrdId(int grdId) { this.grdId = grdId; }

	public String getLotCode() { return lotCode; }
	public void setLotCode(String lotCode) { this.lotCode = lotCode; }

	public LocalDate getReceivedDate() { return receivedDate; }
	public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

	public LocalDate getExpiry() { return expiry; }
	public void setExpiry(LocalDate expiry) { this.expiry = expiry; }

	public int getQtyIn() { return qtyIn; }
	public void setQtyIn(int qtyIn) { this.qtyIn = qtyIn; }

	public int getQtyOut() { return qtyOut; }
	public void setQtyOut(int qtyOut) { this.qtyOut = qtyOut; }

	public int getQtyRemaining() { return qtyRemaining; }
	public void setQtyRemaining(int qtyRemaining) { this.qtyRemaining = qtyRemaining; }

	public LocalDateTime getOutOfStockAt() { return outOfStockAt; }
	public void setOutOfStockAt(LocalDateTime outOfStockAt) { this.outOfStockAt = outOfStockAt; }

	public Status getStatus() { return status; }
	public void setStatus(Status status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
