package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class ReportRevenueRow {

    private LocalDate date;
    private BigDecimal revenue;
    private BigDecimal cost;
    private BigDecimal profit;

    // Constructor đầy đủ (dùng cho DAO)
    public ReportRevenueRow(LocalDate date,
                            BigDecimal revenue,
                            BigDecimal cost,
                            BigDecimal profit) {
        this.date = date;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.cost = cost != null ? cost : BigDecimal.ZERO;
        this.profit = profit != null ? profit : BigDecimal.ZERO;
    }

    // Constructor rỗng (bắt buộc cho framework / future use)
    public ReportRevenueRow() {
        this.revenue = BigDecimal.ZERO;
        this.cost = BigDecimal.ZERO;
        this.profit = BigDecimal.ZERO;
    }

    // ===== GETTERS / SETTERS =====

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost != null ? cost : BigDecimal.ZERO;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit != null ? profit : BigDecimal.ZERO;
    }

    // ===== OPTIONAL: debug / logging =====
    @Override
    public String toString() {
        return "ReportRevenueRow{" +
                "date=" + date +
                ", revenue=" + revenue +
                ", cost=" + cost +
                ", profit=" + profit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportRevenueRow)) return false;
        ReportRevenueRow that = (ReportRevenueRow) o;
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
