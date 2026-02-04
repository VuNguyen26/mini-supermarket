package dto;

public class ProductOption {
    private int productId;
    private String productName;

    public ProductOption(int productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    public int getProductId() {
        return productId;
    }

    @Override
    public String toString() {
        return productName; // JComboBox sẽ hiển thị tên SP
    }
}