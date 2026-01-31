package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GoodsReceipt {
	private int grId;
	private int supplierId;
	private int createdBy;
	private LocalDateTime createdAt;
	private String note;
	private BigDecimal totalAmount;

	public GoodsReceipt() {}

	public GoodsReceipt(int grId, int supplierId, int createdByUserId, LocalDateTime createdAt, BigDecimal totalAmount, String note) {
		this.grId = grId;
		this.supplierId = supplierId;
		this.createdBy = createdByUserId;
		this.createdAt = createdAt;
		this.totalAmount = totalAmount;
		this.note = note;
	}

	public int getGrId() { return grId; }
	public void setGrId(int grId) { this.grId = grId; }

	public int getSupplierId() { return supplierId; }
	public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

	public int getCreatedBy() { return createdBy; }
	public void setCreatedBy(int createdByUserId) { this.createdBy = createdByUserId; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createAt) { this.createdAt = createAt; }

	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
}

