package util;

/**
 * Permission code constants - tránh gõ sai chuỗi.
 * Chỉ cần dùng PermissionCodes.PRODUCT_VIEW thay vì "PRODUCT_VIEW".
 */
public final class  PermissionCodes {

    private PermissionCodes() {}

    // Dashboard
    public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";

    // Category
    public static final String CATEGORY_VIEW = "CATEGORY_VIEW";
    public static final String CATEGORY_CREATE = "CATEGORY_CREATE";
    public static final String CATEGORY_UPDATE = "CATEGORY_UPDATE";
    public static final String CATEGORY_DELETE = "CATEGORY_DELETE";

    // Product
    public static final String PRODUCT_VIEW = "PRODUCT_VIEW";
    public static final String PRODUCT_CREATE = "PRODUCT_CREATE";
    public static final String PRODUCT_UPDATE = "PRODUCT_UPDATE";
    public static final String PRODUCT_DELETE = "PRODUCT_DELETE";

    // Supplier
    public static final String SUPPLIER_VIEW = "SUPPLIER_VIEW";
    public static final String SUPPLIER_MANAGE = "SUPPLIER_MANAGE";

    // Customer
    public static final String CUSTOMER_VIEW = "CUSTOMER_VIEW";
    public static final String CUSTOMER_MANAGE = "CUSTOMER_MANAGE";

    // POS / Invoice
    public static final String POS_SELL = "POS_SELL";
    public static final String INVOICE_VIEW = "INVOICE_VIEW";

    // Goods receipt
    public static final String RECEIPT_CREATE = "RECEIPT_CREATE";

    // Inventory lot / Stock adjustment
    public static final String LOT_VIEW = "LOT_VIEW";
    public static final String ADJUSTMENT_CREATE = "ADJUSTMENT_CREATE";
    public static final String ADJUSTMENT_APPROVE = "ADJUSTMENT_APPROVE";

    // Promotion / Payment / Loyalty
    public static final String PROMOTION_MANAGE = "PROMOTION_MANAGE";
    public static final String PAYMENT_VIEW = "PAYMENT_VIEW";
    public static final String LOYALTY_VIEW = "LOYALTY_VIEW";

    // Report / Audit
    public static final String REPORT_VIEW = "REPORT_VIEW";
    public static final String AUDIT_VIEW = "AUDIT_VIEW";

    // User/Role management
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String ROLE_PERMISSION_MANAGE = "ROLE_PERMISSION_MANAGE";
}
