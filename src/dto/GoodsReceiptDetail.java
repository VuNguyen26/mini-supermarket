package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoodsReceiptDetail {
	private int grdId;
	private int grId;
	private int productId;
	private int qty;
	private BigDecimal unitPrice;
	private BigDecimal lineTotal;
	private String lotCode;
	private LocalDate mfgDate;
	private LocalDate expiry;
	private LocalDateTime receivedAt;

	public GoodsReceiptDetail() {}

	public GoodsReceiptDetail(int grdId, int grId, int productId, int qty, BigDecimal unitPrice, BigDecimal lineTotal,
			String lotCode, LocalDate mfgDate, LocalDate expiry, LocalDateTime receivedAt) {
		this.grdId = grdId;
		this.grId = grId;
		this.productId = productId;
		this.qty = qty;
		this.unitPrice = unitPrice;
		this.lineTotal = lineTotal;
		this.lotCode = lotCode;
		this.mfgDate = mfgDate;
		this.expiry = expiry;
		this.receivedAt = receivedAt;
	}
	public int getGrdId() { return grdId; }
	public void setGrdId(int grdId) { this.grdId = grdId; }

	public int getGrId() { return grId; }
	public void setGrId(int grId) { this.grId = grId; }

	public int getProductId() { return productId; }
	public void setProductId(int productId) { this.productId = productId; }

	public int getQty() { return qty; }
	public void setQty(int qty) { this.qty = qty; }

	public BigDecimal getUnitPrice() { return unitPrice; }
	public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

	public BigDecimal getLineTotal() { return lineTotal; }
	public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

	public String getLotCode() { return lotCode; }
	public void setLotCode(String lotCode) { this.lotCode = lotCode; }

	public LocalDate getMfgDate() { return mfgDate; }
	public void setMfgDate(LocalDate mfgDate) { this.mfgDate = mfgDate; }

	public LocalDate getExpiry() { return expiry; }
	public void setExpiry(LocalDate expiry) { this.expiry = expiry; }

	public LocalDateTime getReceivedAt() { return receivedAt; }
	public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
}
