package dto;

public class LotOption {

    private Long lotId;
    private String lotCode;
    private int stockQty;

    public LotOption(Long lotId, String lotCode, int stockQty) {
        this.lotId = lotId;
        this.lotCode = lotCode;
        this.stockQty = stockQty;
    }

    public Long getLotId() {
        return lotId;
    }

    public String getLotCode() {
        return lotCode;
    }

    public int getStockQty() {
        return stockQty;
    }

    @Override
    public String toString() {
        return lotCode + " (Tồn: " + stockQty + ")";
    }
}