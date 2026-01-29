package bus;

import dal.dao.ReportDAO;
import dto.ReportProduct;
import dto.ReportRow;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public BigDecimal getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(final_amount), 0) AS total_revenue
            FROM sales_invoice
            WHERE invoice_date BETWEEN ? AND ?
              AND status = 'COMPLETED'
            """;

        try (var conn = dal.DBConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(startDate));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(endDate));

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
            LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);

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
            String type,
            int limit) {

        return reportDAO.getProductReport(fromDate, toDate, type, limit);
    }
}
