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

    // 1) BÁO CÁO DOANH THU THEO NGÀY
    // cost = SUM(quantity * import_price)
    public List<ReportRevenueRow> getRevenueReport(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<ReportRevenueRow> list = new ArrayList<>();

        String sql =
                "SELECT DATE(si.created_at) AS report_date, " +
                        "       SUM(si.grand_total) AS revenue, " +
                        "       SUM(sid.qty * p.import_price) AS cost, " +
                        "       SUM(si.grand_total) - SUM(sid.qty * p.import_price) AS profit " +
                        "FROM sales_invoice si " +
                        "JOIN sales_invoice_detail sid ON si.inv_id = sid.inv_id " +
                        "JOIN product p ON sid.product_id = p.product_id " +
                        "WHERE si.created_at >= ? AND si.created_at < ? " +
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

    // 2) TOP SẢN PHẨM BÁN CHẠY
    // Dùng barcode như product_code (vì project bạn đang có barcode)
    public List<ReportProduct> getTopProducts(LocalDate fromDate, LocalDate toDate, int limit) throws SQLException {
        List<ReportProduct> list = new ArrayList<>();

        String sql =
                "SELECT p.barcode AS product_code, p.product_name, " +
                        "       SUM(sid.qty) AS total_qty, " +
                        "       SUM(sid.line_total) AS total_revenue " +
                        "FROM sales_invoice_detail sid " +
                        "JOIN sales_invoice si ON sid.inv_id = si.inv_id " +
                        "JOIN product p ON sid.product_id = p.product_id " +
                        "WHERE si.created_at >= ? AND si.created_at < ? " +
                        "GROUP BY p.product_id, p.barcode, p.product_name " +
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
                            rs.getLong("total_qty"),
                            rs.getBigDecimal("total_revenue")
                    );
                    list.add(rp);
                }
            }
        }

        return list;
    }

    // 3) TỒN KHO THEO HẠN SỬ DỤNG (inventory_lot theo schema DUY/HEAD)
    public List<ReportInventoryExpiryRow> getInventoryByExpiry(LocalDate today) throws SQLException {
        List<ReportInventoryExpiryRow> list = new ArrayList<>();

        String sql =
                "SELECT p.barcode AS product_code, p.product_name, " +
                        "       il.expiry_date, il.qty_remaining " +
                        "FROM inventory_lot il " +
                        "JOIN product p ON il.product_id = p.product_id " +
                        "WHERE il.qty_remaining > 0 " +
                        "ORDER BY il.expiry_date";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date exp = rs.getDate("expiry_date");
                LocalDate expiryDate = (exp != null ? exp.toLocalDate() : null);

                ReportInventoryExpiryRow row = new ReportInventoryExpiryRow(
                        rs.getString("product_code"),
                        rs.getString("product_name"),
                        expiryDate,
                        rs.getInt("qty_remaining"),
                        buildExpiryBucket(today, expiryDate)
                );

                list.add(row);
            }
        }

        return list;
    }

    // 4) PHÂN LOẠI HSD
    private String buildExpiryBucket(LocalDate today, LocalDate expiryDate) {
        if (expiryDate == null) return "KHÔNG HSD";

        long days = ChronoUnit.DAYS.between(today, expiryDate);

        if (days < 0) return "HẾT HẠN";
        if (days <= 7) return "≤ 7 NGÀY";
        if (days <= 30) return "≤ 30 NGÀY";
        return "BÌNH THƯỜNG";
    }

    /**
     * 5) LẤY DANH SÁCH LÔ HÀNG SẮP HẾT HSD / ĐÃ HẾT HSD
     * days = -1 => lấy lô đã hết hạn (expiry_date < CURDATE())
     * days >= 0 => lấy lô expiry trong [CURDATE(), CURDATE()+days]
     *
     * Map theo InventoryLot schema DUY/HEAD:
     * - received_date
     * - expiry_date
     * - qty_in
     * - qty_out
     * - qty_remaining
     * - status
     */
    public List<InventoryLot> getExpiryStockLots(int days) throws SQLException {
        List<InventoryLot> list = new ArrayList<>();

        String sql = """
            SELECT
                il.lot_id,
                il.product_id,
                il.lot_code,
                il.received_date,
                il.expiry_date,
                il.qty_in,
                il.qty_out,
                il.qty_remaining,
                il.status
            FROM inventory_lot il
            WHERE il.qty_remaining > 0
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
                    InventoryLot lot = new InventoryLot();

                    // compat setters exist (int/long)
                    lot.setLotId(rs.getLong("lot_id"));
                    lot.setProductId(rs.getInt("product_id"));
                    lot.setLotCode(rs.getString("lot_code"));

                    Date recv = rs.getDate("received_date");
                    if (recv != null) lot.setReceivedDate(recv.toLocalDate());

                    Date exp = rs.getDate("expiry_date");
                    if (exp != null) lot.setExpiry(exp.toLocalDate());

                    lot.setQtyIn(rs.getInt("qty_in"));
                    lot.setQtyOut(rs.getInt("qty_out"));
                    lot.setQtyRemaining(rs.getInt("qty_remaining"));

                    String st = rs.getString("status");
                    if (st != null) lot.setStatus(st);

                    list.add(lot);
                }
            }
        }

        return list;
    }
}
