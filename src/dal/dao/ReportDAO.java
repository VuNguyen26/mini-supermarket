package dal.dao;

import dal.DBConnection;
import dto.ReportProduct;
import dto.ReportRevenueRow;
import dto.ReportInventoryExpiryRow;
import dto.InventoryLot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;

public class ReportDAO {

    // 1. BÁO CÁO DOANH THU THEO NGÀY
    public List<ReportRevenueRow> getRevenueReport(
            LocalDate fromDate,
            LocalDate toDate
    ) throws SQLException {

        List<ReportRevenueRow> list = new ArrayList<>();

        String sql =
                "SELECT DATE(si.created_at) AS report_date, " +
                        "       SUM(sid.total_price) AS revenue, " +
                        "       SUM(sid.quantity * p.cost_price) AS cost, " +
                        "       SUM(sid.total_price) - SUM(sid.quantity * p.cost_price) AS profit " +
                        "FROM sales_invoice si " +
                        "JOIN sales_invoice_detail sid ON si.invoice_id = sid.invoice_id " +
                        "JOIN product p ON sid.product_id = p.product_id " +
                        "WHERE si.status = 'COMPLETED' " +
                        "  AND si.created_at BETWEEN ? AND ? " +
                        "GROUP BY DATE(si.created_at) " +
                        "ORDER BY report_date";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fromDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRevenueRow row = new ReportRevenueRow(
                            rs.getDate("report_date").toLocalDate(),
                            rs.getBigDecimal("revenue"),
                            rs.getBigDecimal("cost"),
                            rs.getBigDecimal("profit")
                    );
                    list.add(row);
                }
            }
        }

        return list;
    }

    // 2. TOP SẢN PHẨM BÁN CHẠY
    public List<ReportProduct> getTopProducts(
            LocalDate fromDate,
            LocalDate toDate,
            int limit
    ) throws SQLException {

        List<ReportProduct> list = new ArrayList<>();

        String sql =
                "SELECT p.product_code, p.product_name, " +
                        "       SUM(sid.quantity) AS total_qty, " +
                        "       SUM(sid.total_price) AS total_revenue " +
                        "FROM sales_invoice_detail sid " +
                        "JOIN sales_invoice si ON sid.invoice_id = si.invoice_id " +
                        "JOIN product p ON sid.product_id = p.product_id " +
                        "WHERE si.status = 'COMPLETED' " +
                        "  AND si.created_at BETWEEN ? AND ? " +
                        "GROUP BY p.product_id, p.product_code, p.product_name " +
                        "ORDER BY total_revenue DESC " +
                        "LIMIT ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fromDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    ReportProduct rp = new ReportProduct(
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getLong("total_qty"),          // 👈 Long, KHÔNG int
                            rs.getBigDecimal("total_revenue")
                    );

                    list.add(rp);
                }
            }
        }

        return list;
    }

    // 3. TỒN KHO THEO HẠN SỬ DỤNG

    public List<ReportInventoryExpiryRow> getInventoryByExpiry(
            LocalDate today
    ) throws SQLException {

        List<ReportInventoryExpiryRow> list = new ArrayList<>();

        String sql =
                "SELECT p.product_code, p.product_name, " +
                        "       il.expiry_date, il.remaining_quantity " +
                        "FROM inventory_lot il " +
                        "JOIN product p ON il.product_id = p.product_id " +
                        "WHERE il.remaining_quantity > 0 " +
                        "ORDER BY il.expiry_date";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Date exp = rs.getDate("expiry_date");
                LocalDate expiryDate = exp != null ? exp.toLocalDate() : null;

                ReportInventoryExpiryRow row = new ReportInventoryExpiryRow(
                        rs.getString("product_code"),
                        rs.getString("product_name"),
                        expiryDate,
                        rs.getInt("remaining_quantity"),
                        buildExpiryBucket(today, expiryDate)
                );

                list.add(row);
            }
        }

        return list;
    }

    // 4. PHÂN LOẠI HSD
    private String buildExpiryBucket(LocalDate today, LocalDate expiryDate) {
        if (expiryDate == null) {
            return "KHÔNG HSD";
        }

        long days = ChronoUnit.DAYS.between(today, expiryDate);

        if (days < 0) {
            return "HẾT HẠN";
        }
        if (days <= 7) {
            return "≤ 7 NGÀY";
        }
        if (days <= 30) {
            return "≤ 30 NGÀY";
        }
        return "BÌNH THƯỜNG";
    }

    public List<InventoryLot> getExpiryStockLots(int days) throws SQLException {

        List<InventoryLot> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        String sql = """
            SELECT
                il.lot_id,
                il.product_id,
                il.quantity,
                il.remaining_quantity,
                il.import_date,
                il.expiry_date,
                il.import_price
            FROM inventory_lot il
            WHERE il.remaining_quantity > 0
              AND (
                    (? = -1 AND il.expiry_date < CURDATE())
                 OR (? <> -1 AND il.expiry_date BETWEEN CURDATE()
                        AND DATE_ADD(CURDATE(), INTERVAL ? DAY))
              )
            ORDER BY il.expiry_date ASC
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, days);
            ps.setInt(2, days);
            ps.setInt(3, days);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    InventoryLot lot = new InventoryLot(
                            rs.getLong("lot_id"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity"),
                            rs.getInt("remaining_quantity"),
                            rs.getDate("import_date").toLocalDate(),
                            rs.getDate("expiry_date") != null
                                    ? rs.getDate("expiry_date").toLocalDate()
                                    : null,
                            rs.getDouble("import_price")
                    );

                    list.add(lot);
                }
            }
        }
        return list;
    }


}
