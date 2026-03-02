package dto;

public class LoyaltyPointTxn {
	private long txnId;
	private int customerId;
	private Integer invId;
	private int createdBy;
	private String type;
	private int points;
	private double moneyAmount;
	private double earnRateMoney;
	private int earnRatePoints;
	private int redeemRatePoints;
	private double redeemRateMoney;
	private String note;

	public long getTxnId() {
		return txnId;
	}

	public void setTxnId(long txnId) {
		this.txnId = txnId;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public Integer getInvId() {
		return invId;
	}

	public void setInvId(Integer invId) {
		this.invId = invId;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public double getMoneyAmount() {
		return moneyAmount;
	}

	public void setMoneyAmount(double moneyAmount) {
		this.moneyAmount = moneyAmount;
	}

	public double getEarnRateMoney() {
		return earnRateMoney;
	}

	public void setEarnRateMoney(double earnRateMoney) {
		this.earnRateMoney = earnRateMoney;
	}

	public int getEarnRatePoints() {
		return earnRatePoints;
	}

	public void setEarnRatePoints(int earnRatePoints) {
		this.earnRatePoints = earnRatePoints;
	}

	public int getRedeemRatePoints() {
		return redeemRatePoints;
	}

	public void setRedeemRatePoints(int redeemRatePoints) {
		this.redeemRatePoints = redeemRatePoints;
	}

	public double getRedeemRateMoney() {
		return redeemRateMoney;
	}

	public void setRedeemRateMoney(double redeemRateMoney) {
		this.redeemRateMoney = redeemRateMoney;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
