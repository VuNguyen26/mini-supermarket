package dto;

public class StockAdjustmentDetail {

    private int sadId;
    private int saId;
    private int productId;
    private String productName;
    private Long lotId;        // bigint + nullable → dùng Long
    private int systemQty;
    private int countedQty;
    private int diffQty;
    private String note;

    public StockAdjustmentDetail() {}

    public StockAdjustmentDetail(int sadId, int saId, int productId, String productName, Long lotId,
                                 int systemQty, int countedQty, int diffQty, String note) {
        this.productName = productName;
        this.sadId = sadId;
        this.saId = saId;
        this.productId = productId;
        this.lotId = lotId;
        this.systemQty = systemQty;
        this.countedQty = countedQty;
        this.diffQty = diffQty;
        this.note = note;
    }

    public int getSadId() {
        return sadId;
    }

    public void setSadId(int sadId) {
        this.sadId = sadId;
    }

    public int getSaId() {
        return saId;
    }

    public void setSaId(int saId) {
        this.saId = saId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName(){
        return this.productName;
    }

    public void setProductName(String productName){
        this.productName = productName;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public int getSystemQty() {
        return systemQty;
    }

    public void setSystemQty(int systemQty) {
        this.systemQty = systemQty;
    }

    public int getCountedQty() {
        return countedQty;
    }

    public void setCountedQty(int countedQty) {
        this.countedQty = countedQty;
    }

    public int getDiffQty() {
        return diffQty;
    }

    public void setDiffQty(int diffQty) {
        this.diffQty = diffQty;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
