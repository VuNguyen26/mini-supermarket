package presentation.panels;

import dal.DBConnection;
import dal.dao.ReportDAO;
import dto.ReportRevenueRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private static final int LOW_THRESHOLD = 5;
    private static final int RECENT_LIMIT = 10;

    private final ReportDAO reportDAO = new ReportDAO();

    // ===== TOP KPI (4 cards) =====
    private final JLabel lbTotalRevenue = new JLabel("0 đ");
    private final JLabel lbOrdersToday  = new JLabel("0");
    private final JLabel lbTotalProducts= new JLabel("0");
    private final JLabel lbCustomers    = new JLabel("0");

    // ===== MIDDLE LEFT: Recent Orders =====
    private final DefaultTableModel recentOrderModel = new DefaultTableModel(
            new String[]{"Mã", "Giờ", "Ngày", "Thanh toán", "Tổng"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tblRecentOrders = new JTable(recentOrderModel);

    // ===== MIDDLE RIGHT: Stock Alert (scroll list) =====
    private final DefaultListModel<StockAlertItem> stockModel = new DefaultListModel<>();
    private final JList<StockAlertItem> lstStockAlerts = new JList<>(stockModel);

    // ===== BOTTOM KPI (3 cards) =====
    private final JLabel lbImportReceipts = new JLabel("0");
    private final JLabel lbInventoryValue = new JLabel("0 đ");
    private final JLabel lbAvgCartValue   = new JLabel("0 đ");

    public DashboardPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(), BorderLayout.CENTER);

        reload();
    }

    // =========================
    // UI
    // =========================
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Tổng quan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(15, 23, 42));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JComponent buildGrid() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        JPanel row1 = new JPanel(new GridLayout(1, 4, 12, 12));
        row1.setOpaque(false);
        row1.add(kpiCardSmall("Tổng doanh thu", lbTotalRevenue, new Color(34, 197, 94)));
        row1.add(kpiCardSmall("Đơn hàng hôm nay", lbOrdersToday, new Color(59, 130, 246)));
        row1.add(kpiCardSmall("Tổng sản phẩm", lbTotalProducts, new Color(124, 58, 237)));
        row1.add(kpiCardSmall("Khách hàng", lbCustomers, new Color(245, 158, 11)));

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 0;
        root.add(row1, gbc);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 12));
        row2.setOpaque(false);
        row2.add(buildRecentOrdersCard());
        row2.add(buildStockAlertCard());

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weighty = 1;
        root.add(row2, gbc);

        JPanel row3 = new JPanel(new GridLayout(1, 3, 12, 12));
        row3.setOpaque(false);
        row3.add(kpiCardBottom("Tổng số phiếu nhập", lbImportReceipts));
        row3.add(kpiCardBottom("Tổng giá trị hàng", lbInventoryValue));
        row3.add(kpiCardBottom("Trung bình giá trị đơn hôm nay", lbAvgCartValue));

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weighty = 0;
        root.add(row3, gbc);

        return root;
    }

    private JComponent buildRecentOrdersCard() {
        JPanel inner = new JPanel(new BorderLayout(10, 10));
        inner.setOpaque(false);

        JLabel t = new JLabel("Đơn hàng gần đây");
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(new Color(15, 23, 42));

        tblRecentOrders.setRowHeight(26);
        tblRecentOrders.setShowVerticalLines(false);
        tblRecentOrders.setGridColor(new Color(229, 231, 235));
        JScrollPane sp = new JScrollPane(tblRecentOrders);
        sp.setBorder(null);

        inner.add(t, BorderLayout.NORTH);
        inner.add(sp, BorderLayout.CENTER);

        return wrapCard(inner, 14);
    }

    private JComponent buildStockAlertCard() {
        JPanel inner = new JPanel(new BorderLayout(10, 10));
        inner.setOpaque(false);

        JLabel t = new JLabel("Cảnh báo hết hàng");
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(new Color(15, 23, 42));

        lstStockAlerts.setCellRenderer(new StockAlertRenderer());
        JScrollPane sp = new JScrollPane(lstStockAlerts);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);

        inner.add(t, BorderLayout.NORTH);
        inner.add(sp, BorderLayout.CENTER);

        return wrapCard(inner, 14);
    }

    private JPanel kpiCardSmall(String title, JLabel value, Color accent) {
        JPanel inner = new JPanel(new BorderLayout(6, 6));
        inner.setOpaque(false);

        JPanel icon = new JPanel();
        icon.setPreferredSize(new Dimension(14, 14));
        icon.setBackground(accent);
        icon.setOpaque(true);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(100, 116, 139));

        value.setFont(new Font("Segoe UI", Font.BOLD, 18));
        value.setForeground(new Color(15, 23, 42));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(icon);
        top.add(Box.createHorizontalStrut(8));
        top.add(t);

        inner.add(top, BorderLayout.NORTH);
        inner.add(value, BorderLayout.CENTER);

        return wrapCard(inner, 12);
    }

    private JPanel kpiCardBottom(String title, JLabel value) {
        JPanel inner = new JPanel(new BorderLayout(6, 6));
        inner.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(100, 116, 139));

        value.setFont(new Font("Segoe UI", Font.BOLD, 16));
        value.setForeground(new Color(15, 23, 42));

        inner.add(t, BorderLayout.NORTH);
        inner.add(value, BorderLayout.CENTER);

        return wrapCard(inner, 12);
    }

    private JPanel wrapCard(JComponent inner, int pad) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(pad, pad, pad, pad)
        ));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // =========================
    // Reload (REAL DATA)
    // =========================
    private void reload() {
        SwingWorker<DashboardData, Void> w = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData d = new DashboardData();

                LocalDate today = LocalDate.now();

                // 1) Doanh thu hôm nay
                List<ReportRevenueRow> rows = reportDAO.getRevenueReport(today, today);
                BigDecimal rev = BigDecimal.ZERO;
                for (ReportRevenueRow r : rows) {
                    rev = rev.add(getRevenue(r));
                }
                d.totalRevenueToday = rev;

                // 2) Orders today + recent orders
                d.ordersToday = countOrdersToday();
                d.recentOrders = getRecentOrders();

                // 3) total products
                d.totalProducts = scalarLong("SELECT COUNT(*) FROM product");

                // 4) customers (fallback)
                d.customers = tryScalarLong(new String[]{
                        "SELECT COUNT(*) FROM customer",
                        "SELECT COUNT(*) FROM customers",
                        "SELECT COUNT(*) FROM client",
                        "SELECT COUNT(*) FROM clients"
                }, 0L);

                // 6) stock alert list
                d.stockAlerts = getStockAlerts();

                // 7) import receipts count (fallback)
                d.importReceipts = tryScalarLong(new String[]{
                        "SELECT COUNT(*) FROM import_receipt",
                        "SELECT COUNT(*) FROM goods_receipt",
                        "SELECT COUNT(*) FROM purchase_receipt",
                        "SELECT COUNT(*) FROM stock_in"
                }, 0L);

                // 8) inventory value
                d.inventoryValue = tryScalarBD(new String[]{
                        "SELECT COALESCE(SUM(il.qty_remaining * p.import_price), 0) " +
                                "FROM inventory_lot il " +
                                "JOIN product p ON il.product_id = p.product_id " +
                                "WHERE il.qty_remaining > 0"
                }, BigDecimal.ZERO);

                // 9) avg cart today
                if (d.ordersToday > 0) {
                    d.avgOrderValueToday = d.totalRevenueToday
                            .divide(BigDecimal.valueOf(d.ordersToday), 0, RoundingMode.HALF_UP);
                } else {
                    d.avgOrderValueToday = BigDecimal.ZERO;
                }

                return d;
            }

            @Override
            protected void done() {
                try {
                    DashboardData d = get();

                    lbTotalRevenue.setText(fmt(d.totalRevenueToday));
                    lbOrdersToday.setText(String.valueOf(d.ordersToday));
                    lbTotalProducts.setText(String.valueOf(d.totalProducts));
                    lbCustomers.setText(String.valueOf(d.customers));

                    renderRecentOrders(d.recentOrders);
                    renderStockAlerts(d.stockAlerts);

                    lbImportReceipts.setText(String.valueOf(d.importReceipts));
                    lbInventoryValue.setText(fmt(d.inventoryValue));
                    lbAvgCartValue.setText(fmt(d.avgOrderValueToday));

                } catch (Exception ex) {
                    recentOrderModel.setRowCount(0);
                    recentOrderModel.addRow(new Object[]{"(Lỗi tải dữ liệu)", "", "", "", "0 đ"});
                    stockModel.clear();
                    stockModel.addElement(new StockAlertItem("-", "Không tải được cảnh báo tồn kho", 0, LOW_THRESHOLD));
                }
            }
        };

        w.execute();
    }

    // =========================
    // Fix for private revenue field
    // =========================
    private BigDecimal getRevenue(ReportRevenueRow row) {
        if (row == null) return BigDecimal.ZERO;

        // 1) try getter getRevenue()
        try {
            Method m = row.getClass().getMethod("getRevenue");
            Object v = m.invoke(row);
            if (v instanceof BigDecimal bd) return bd;
        } catch (Exception ignored) {}

        // 2) try public field "revenue"
        try {
            Field f = row.getClass().getDeclaredField("revenue");
            f.setAccessible(true);
            Object v = f.get(row);
            if (v instanceof BigDecimal bd) return bd;
        } catch (Exception ignored) {}

        return BigDecimal.ZERO;
    }

    // =========================
    // Recent orders
    // =========================
    private static class RecentOrder {
        String code;
        LocalDateTime createdAt;
        String payment;
        BigDecimal total;
    }

    private List<RecentOrder> getRecentOrders() throws SQLException {
        String sqlWithPay =
                "SELECT si.inv_id AS code, si.created_at, si.payment_method AS payment, si.grand_total AS total " +
                        "FROM sales_invoice si " +
                        "ORDER BY si.created_at DESC " +
                        "LIMIT ?";

        String sqlNoPay =
                "SELECT si.inv_id AS code, si.created_at, '' AS payment, si.grand_total AS total " +
                        "FROM sales_invoice si " +
                        "ORDER BY si.created_at DESC " +
                        "LIMIT ?";

        try {
            return queryRecentOrders(sqlWithPay);
        } catch (SQLException ex) {
            return queryRecentOrders(sqlNoPay);
        }
    }

    private List<RecentOrder> queryRecentOrders(String sql) throws SQLException {
        List<RecentOrder> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, RECENT_LIMIT);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecentOrder o = new RecentOrder();
                    o.code = rs.getString("code");

                    Timestamp ts = rs.getTimestamp("created_at");
                    o.createdAt = (ts != null) ? ts.toLocalDateTime() : null;

                    o.payment = rs.getString("payment");
                    o.total = rs.getBigDecimal("total");
                    list.add(o);
                }
            }
        }
        return list;
    }

    private void renderRecentOrders(List<RecentOrder> orders) {
        recentOrderModel.setRowCount(0);

        if (orders == null || orders.isEmpty()) {
            recentOrderModel.addRow(new Object[]{"(Không có dữ liệu)", "", "", "", "0 đ"});
            return;
        }

        DateTimeFormatter fmtTime = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (RecentOrder o : orders) {
            String time = (o.createdAt != null) ? o.createdAt.format(fmtTime) : "";
            String date = (o.createdAt != null) ? o.createdAt.format(fmtDate) : "";
            recentOrderModel.addRow(new Object[]{
                    safe(o.code),
                    time,
                    date,
                    safe(o.payment),
                    fmt(o.total)
            });
        }
    }

    private long countOrdersToday() throws SQLException {
        LocalDate today = LocalDate.now();
        String sql =
                "SELECT COUNT(*) FROM sales_invoice " +
                        "WHERE created_at >= ? AND created_at < ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(today.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(today.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    // =========================
    // Stock alerts
    // =========================
    private enum StockLevel {
        OUT(0), LOW(1), OK(2);
        final int rank;
        StockLevel(int rank) { this.rank = rank; }
    }

    private static class StockAlertItem {
        final String code;
        final String name;
        final long qty;
        final long threshold;
        final StockLevel level;

        StockAlertItem(String code, String name, long qty, long threshold) {
            this.code = (code == null || code.isBlank()) ? "-" : code;
            this.name = (name == null || name.isBlank()) ? "(Không tên)" : name;
            this.qty = qty;
            this.threshold = threshold;

            if (qty <= 0) this.level = StockLevel.OUT;
            else if (qty <= threshold) this.level = StockLevel.LOW;
            else this.level = StockLevel.OK;
        }

        String displayText() {
            return String.format("[%s] %s — Tồn: %d", code, name, qty);
        }
    }

    private static class StockAlertRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lb.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            if (value instanceof StockAlertItem item) {
                lb.setText(item.displayText());

                if (!isSelected) {
                    switch (item.level) {
                        case OUT -> { lb.setBackground(new Color(254, 226, 226)); lb.setForeground(new Color(185, 28, 28)); }
                        case LOW -> { lb.setBackground(new Color(254, 249, 195)); lb.setForeground(new Color(161, 98, 7)); }
                        case OK  -> { lb.setBackground(new Color(220, 252, 231)); lb.setForeground(new Color(22, 101, 52)); }
                    }
                }
            }
            return lb;
        }
    }

    private List<StockAlertItem> getStockAlerts() throws SQLException {
        String sql =
                "SELECT p.barcode AS product_code, p.product_name, COALESCE(SUM(il.qty_remaining), 0) AS qty " +
                        "FROM product p " +
                        "LEFT JOIN inventory_lot il ON il.product_id = p.product_id " +
                        "GROUP BY p.product_id, p.barcode, p.product_name";

        List<StockAlertItem> items = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String code = rs.getString("product_code");
                String name = rs.getString("product_name");
                long qty = rs.getLong("qty");
                items.add(new StockAlertItem(code, name, qty, LOW_THRESHOLD));
            }
        }

        items.sort(Comparator
                .comparingInt((StockAlertItem it) -> it.level.rank)
                .thenComparingLong(it -> it.qty)
        );

        return items;
    }

    private void renderStockAlerts(List<StockAlertItem> items) {
        stockModel.clear();
        if (items == null || items.isEmpty()) {
            stockModel.addElement(new StockAlertItem("-", "Tất cả sản phẩm đều đủ", 999999, LOW_THRESHOLD));
            return;
        }
        for (StockAlertItem it : items) stockModel.addElement(it);
    }

    // =========================
    // DB scalar helpers
    // =========================
    private long scalarLong(String sql) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    private long tryScalarLong(String[] candidates, long fallback) throws SQLException {
        for (String sql : candidates) {
            try {
                return scalarLong(sql);
            } catch (SQLException ignored) {}
        }
        return fallback;
    }

    private BigDecimal scalarBD(String sql) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal v = rs.getBigDecimal(1);
                return v == null ? BigDecimal.ZERO : v;
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal tryScalarBD(String[] candidates, BigDecimal fallback) throws SQLException {
        for (String sql : candidates) {
            try {
                return scalarBD(sql);
            } catch (SQLException ignored) {}
        }
        return fallback;
    }

    // =========================
    // Utils
    // =========================
    private String fmt(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return String.format("%,.0f đ", v);
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    // =========================
    // Data holder
    // =========================
    private static class DashboardData {
        BigDecimal totalRevenueToday = BigDecimal.ZERO;
        long ordersToday = 0;
        long totalProducts = 0;
        long customers = 0;

        List<RecentOrder> recentOrders = List.of();
        List<StockAlertItem> stockAlerts = List.of();

        long importReceipts = 0;
        BigDecimal inventoryValue = BigDecimal.ZERO;
        BigDecimal avgOrderValueToday = BigDecimal.ZERO;
    }
}