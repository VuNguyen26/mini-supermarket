package dto;

public class PromotionProduct {

    private int ppId;
    private int promoId;
    private int productId;
    private String productName;

    public PromotionProduct() {}

    public PromotionProduct(int ppId, int promoId, int productId, String productName) {
        this.ppId = ppId;
        this.promoId = promoId;
        this.productId = productId;
        this.productName = productName;
    }

    public int getPpId() {
        return ppId;
    }

    public void setPpId(int ppId) {
        this.ppId = ppId;
    }

    public int getPromoId() {
        return promoId;
    }

    public void setPromoId(int promoId) {
        this.promoId = promoId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProducName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}