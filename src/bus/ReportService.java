package bus;

import dal.dao.ReportDAO;
import dto.ReportProduct;
import dto.ReportRow;
import dto.ReportRevenueRow;
import dto.InventoryLot;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý các nghiệp vụ báo cáo
 * Không chứa SQL
 * Chỉ gọi DAO và xử lý logic
 */
public class ReportService {

    private final ReportDAO reportDAO = new ReportDAO();

    /**
     * Báo cáo doanh thu theo khoảng thời gian
     */
    public BigDecimal getRevenueReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = """
        SELECT COALESCE(SUM(sid.total_price), 0) AS total_revenue
        FROM sales_invoice si
        JOIN sales_invoice_detail sid
             ON si.invoice_id = sid.invoice_id
        WHERE si.status = 'COMPLETED'
          AND si.created_at >= ?
          AND si.created_at < ?
        """;
        try (var conn = dal.DBConnection.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total_revenue");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * Báo cáo doanh thu theo tháng trong năm
     */
    public List<ReportRow> getMonthlyRevenueReport(int year) throws SQLException {
        List<ReportRow> report = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            LocalDate  start = LocalDate.of(year, month, 1);
            LocalDate  end = start.withDayOfMonth(start.lengthOfMonth());

            BigDecimal revenue = getRevenueReport(start, end);
            report.add(new ReportRow("Tháng " + month, revenue.toString()));
        }

        return report;
    }

    /**
     * Top sản phẩm bán chạy / doanh thu cao
     */
    public List<ReportProduct> getTopProducts(
            LocalDate fromDate,
            LocalDate toDate,
            int limit) {

        try {
            return reportDAO.getTopProducts(fromDate, toDate, limit);
        } catch (SQLException e) {
            e.printStackTrace(); // quan trọng khi debug
            throw new RuntimeException("Không thể tải top sản phẩm", e);
        }
    }
    // ===== DANH SÁCH DOANH THU =====
    public List<ReportRevenueRow> getRevenueList(
            LocalDate from,
            LocalDate to
    ) {
        try {
            return reportDAO.getRevenueReport(from, to);
        } catch (Exception e) {
            throw new RuntimeException("Không thể tải danh sách doanh thu", e);
        }
    }


    public List<InventoryLot> getExpiryStock(int days) {
        try {
            return reportDAO.getExpiryStockLots(days);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải báo cáo tồn kho theo HSD", e
            );
        }
    }
}
