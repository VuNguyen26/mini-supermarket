package presentation.menu;

import util.PermissionCodes;
import util.RolePermission;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * AppMenuBar:
 * - Tạo menu theo module
 * - Ẩn/hiện menu item theo permission (RolePermission.has)
 * - Khi click menu -> gọi navigator.navigate("KEY")
 */
public class AppMenuBar extends JMenuBar {

    /**
     * MainFrame chỉ cần implement interface này là menu dùng được.
     * Bạn có thể implement trong MainFrame: key -> switchCard(panelKey)
     */
    public interface Navigator {
        void navigate(String key);
    }

    // Keys để MainFrame switch panel
    public static final String KEY_DASHBOARD = "DASHBOARD";
    public static final String KEY_CATEGORY = "CATEGORY";
    public static final String KEY_PRODUCT = "PRODUCT";
    public static final String KEY_SUPPLIER = "SUPPLIER";
    public static final String KEY_CUSTOMER = "CUSTOMER";
    public static final String KEY_POS = "POS";
    public static final String KEY_RECEIPT = "RECEIPT";
    public static final String KEY_INVENTORY_LOT = "INVENTORY_LOT";
    public static final String KEY_STOCK_ADJUST = "STOCK_ADJUST";
    public static final String KEY_PROMOTION = "PROMOTION";
    public static final String KEY_LOYALTY = "LOYALTY";
    public static final String KEY_PAYMENT = "PAYMENT";
    public static final String KEY_REPORT = "REPORT";
    public static final String KEY_AUDIT = "AUDIT";
    public static final String KEY_USER = "USER";
    public static final String KEY_ROLE_PERMISSION = "ROLE_PERMISSION";

    private final Navigator navigator;

    public AppMenuBar(Navigator navigator) {
        this.navigator = Objects.requireNonNull(navigator);
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        setBackground(Color.WHITE);
        buildMenus();
    }

    private void buildMenus() {
        removeAll();

        // ====== HOME ======
        JMenu home = new JMenu("Home");
        if (RolePermission.has(PermissionCodes.DASHBOARD_VIEW)) {
            home.add(item("Dashboard", KEY_DASHBOARD));
        }
        addIfNotEmpty(home);

        // ====== MASTER DATA ======
        JMenu master = new JMenu("Danh mục");

        if (RolePermission.has(PermissionCodes.CATEGORY_VIEW)) {
            master.add(item("Loại hàng", KEY_CATEGORY));
        }
        if (RolePermission.has(PermissionCodes.PRODUCT_VIEW)) {
            master.add(item("Sản phẩm", KEY_PRODUCT));
        }
        if (RolePermission.has(PermissionCodes.SUPPLIER_VIEW) || RolePermission.has(PermissionCodes.SUPPLIER_MANAGE)) {
            master.add(item("Nhà cung cấp", KEY_SUPPLIER));
        }
        if (RolePermission.has(PermissionCodes.CUSTOMER_VIEW) || RolePermission.has(PermissionCodes.CUSTOMER_MANAGE)) {
            master.add(item("Khách hàng", KEY_CUSTOMER));
        }

        addIfNotEmpty(master);

        // ====== SALES ======
        JMenu sales = new JMenu("Bán hàng");
        if (RolePermission.has(PermissionCodes.POS_SELL)) {
            sales.add(item("POS (Bán hàng)", KEY_POS));
        }
        if (RolePermission.has(PermissionCodes.INVOICE_VIEW)) {
            sales.add(item("Hóa đơn", KEY_REPORT)); // nếu bạn có panel hóa đơn riêng thì đổi KEY
        }
        addIfNotEmpty(sales);

        // ====== WAREHOUSE ======
        JMenu warehouse = new JMenu("Kho");
        if (RolePermission.has(PermissionCodes.RECEIPT_CREATE)) {
            warehouse.add(item("Phiếu nhập", KEY_RECEIPT));
        }
        if (RolePermission.has(PermissionCodes.LOT_VIEW)) {
            warehouse.add(item("Tồn theo lô", KEY_INVENTORY_LOT));
        }
        if (RolePermission.has(PermissionCodes.ADJUSTMENT_CREATE) || RolePermission.has(PermissionCodes.ADJUSTMENT_APPROVE)) {
            warehouse.add(item("Kiểm kho / Điều chỉnh", KEY_STOCK_ADJUST));
        }
        addIfNotEmpty(warehouse);

        // ====== PROMOTION / LOYALTY ======
        JMenu marketing = new JMenu("Khuyến mãi");
        if (RolePermission.has(PermissionCodes.PROMOTION_MANAGE)) {
            marketing.add(item("Chương trình KM", KEY_PROMOTION));
        }
        if (RolePermission.has(PermissionCodes.LOYALTY_VIEW)) {
            marketing.add(item("Điểm tích lũy", KEY_LOYALTY));
        }
        addIfNotEmpty(marketing);

        // ====== PAYMENT ======
        JMenu pay = new JMenu("Thanh toán");
        if (RolePermission.has(PermissionCodes.PAYMENT_VIEW)) {
            pay.add(item("Giao dịch thanh toán", KEY_PAYMENT));
        }
        addIfNotEmpty(pay);

        // ====== REPORT / AUDIT ======
        JMenu report = new JMenu("Báo cáo");
        if (RolePermission.has(PermissionCodes.REPORT_VIEW)) {
            report.add(item("Báo cáo", KEY_REPORT));
        }
        if (RolePermission.has(PermissionCodes.AUDIT_VIEW)) {
            report.add(item("Nhật ký hệ thống", KEY_AUDIT));
        }
        addIfNotEmpty(report);

        // ====== SYSTEM ======
        JMenu system = new JMenu("Hệ thống");
        if (RolePermission.has(PermissionCodes.USER_MANAGE)) {
            system.add(item("Người dùng", KEY_USER));
            system.addSeparator();
            system.add(item("Phân quyền theo Role", KEY_ROLE_PERMISSION)); // panel bạn sẽ làm
        }
        // Logout nên ai cũng có
        system.addSeparator();
        system.add(item("Đăng xuất", "LOGOUT"));

        addIfNotEmpty(system);

        revalidate();
        repaint();
    }

    private JMenuItem item(String text, String key) {
        JMenuItem it = new JMenuItem(text);
        it.addActionListener(e -> {
            if ("LOGOUT".equals(key)) {
                navigator.navigate("LOGOUT");
            } else {
                navigator.navigate(key);
            }
        });
        return it;
    }

    private void addIfNotEmpty(JMenu menu) {
        if (menu.getItemCount() > 0) {
            add(menu);
        }
    }
}
