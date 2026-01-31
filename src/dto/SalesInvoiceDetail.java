package dto;

public class SalesInvoiceDetail {
    private int invDetailId;
    private int invId;
    private int productId;
    private long lotId;
    private int qty;
    private double unitPrice;
    private double lineTotal;
    private String productName;

    public SalesInvoiceDetail() {
    }

    public SalesInvoiceDetail(int productId, long lotId, int qty, double unitPrice) {
        this.productId = productId;
        this.lotId = lotId;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.lineTotal = qty * unitPrice;
    }

    public int getinvDetailId() {
        return invDetailId;
    }

    public void setinvDetailId(int invDetailId) {
        this.invDetailId = invDetailId;
    }

    public int getInvId() {
        return invId;
    }

    public void setInvId(int invId) {
        this.invId = invId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public long getLotId() {
        return lotId;
    }

    public void setLotId(long lotId) {
        this.lotId = lotId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
        this.lineTotal = this.qty * this.unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
}