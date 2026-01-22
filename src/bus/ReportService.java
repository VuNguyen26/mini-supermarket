package bus;

import dal.DBConnection;
import dto.InventoryLot;
import dto.SalesInvoice;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();
    private final InventoryLotDAO inventoryLotDAO = new InventoryLotDAO();

    // Báo cáo doanh thu theo khoảng thời gian
    public BigDecimal getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(final_amount), 0) as total_revenue
            FROM sales_invoice
            WHERE invoice_date BETWEEN ? AND ?
            AND status = 'COMPLETED'
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total_revenue");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    // Báo cáo doanh thu theo tháng
    public List<ReportRow> getMonthlyRevenueReport(int year) throws SQLException {
        List<ReportRow> report = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

            BigDecimal revenue = getRevenueReport(startDate, endDate);
            report.add(new ReportRow("Tháng " + month, revenue.toString()));
        }
        return report;
    }

    // Top sản phẩm bán chạy
    public List<ReportRow> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit) throws SQLException {
        // Query thực tế từ database
        String sql = """
            SELECT p.product_name, SUM(sid.quantity) as total_quantity
            FROM sales_invoice_detail sid
            JOIN sales_invoice si ON sid.invoice_id = si.invoice_id
            JOIN product p ON sid.product_id = p.product_id
            WHERE si.invoice_date BETWEEN ? AND ?
            AND si.status = 'COMPLETED'
            GROUP BY sid.product_id, p.product_name
            ORDER BY total_quantity DESC
            LIMIT ?
            """;

        List<ReportRow> topProducts = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topProducts.add(new ReportRow(
                        rs.getString("product_name"),
                        rs.getString("total_quantity")
                    ));
                }
            }
        }
        return topProducts;
    }

    // Tồn kho theo hạn sử dụng
    public List<InventoryLot> getInventoryByExpiryDate() throws SQLException {
        return inventoryLotDAO.getAllInventoryLots();
    }

    // Sản phẩm sắp hết hạn (trong N ngày)
    public List<InventoryLot> getExpiringProducts(int daysAhead) throws SQLException {
        return inventoryLotDAO.getExpiringLots(daysAhead);
    }

    // Thống kê tổng quan
    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {
            // Tổng doanh thu hôm nay
            LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime tomorrow = today.plusDays(1);
            BigDecimal todayRevenue = getRevenueReport(today, tomorrow.minusSeconds(1));
            stats.put("todayRevenue", todayRevenue);

            // Tổng doanh thu tháng này
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
            BigDecimal monthRevenue = getRevenueReport(monthStart, monthEnd);
            stats.put("monthRevenue", monthRevenue);

            // Số hóa đơn hôm nay
            String invoiceSql = """
                SELECT COUNT(*) as invoice_count
                FROM sales_invoice
                WHERE DATE(invoice_date) = CURDATE()
                AND status = 'COMPLETED'
                """;

            try (PreparedStatement invoiceStmt = conn.prepareStatement(invoiceSql);
                 ResultSet invoiceRs = invoiceStmt.executeQuery()) {
                if (invoiceRs.next()) {
                    stats.put("todayInvoices", invoiceRs.getInt("invoice_count"));
                }
            }

            // Sản phẩm sắp hết hạn (7 ngày)
            List<InventoryLot> expiringProducts = getExpiringProducts(7);
            stats.put("expiringProducts", expiringProducts.size());
        }

        return stats;
    }
}
