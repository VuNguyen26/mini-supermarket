package presentation.panels;

import dto.ReportRevenueRow;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RevenueTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Ngày", "Doanh thu", "Chi phí", "Lợi nhuận"
    };

    private List<ReportRevenueRow> data = new ArrayList<>();

    public void setData(List<ReportRevenueRow> data) {
        this.data = data != null ? data : new ArrayList<>();
        fireTableDataChanged();
    }

    public ReportRevenueRow getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= data.size()) return null;
        return data.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> java.time.LocalDate.class;
            case 1, 2, 3 -> java.math.BigDecimal.class;
            default -> Object.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReportRevenueRow row = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getDate();
            case 1 -> row.getRevenue();
            case 2 -> row.getCost();
            case 3 -> row.getProfit();
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // bảng chỉ xem, không cho sửa
    }
}
