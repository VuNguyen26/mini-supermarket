package presentation.panels;

import bus.ReportService;
import dto.ReportRevenueRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Locale;

public class RevenueReportPanel extends JPanel {

    private final ReportService service;

    private final RevenueTableModel tableModel = new RevenueTableModel();
    private final JTable table = new JTable(tableModel);

    private final JLabel lblTotalValue = new JLabel("0");

    private final JSpinner fromDate = createDateSpinner();
    private final JSpinner toDate = createDateSpinner();

    private final NumberFormat moneyFormat =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public RevenueReportPanel(ReportService service) {
        this.service = service;

        setLayout(new BorderLayout(0, 24));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        setOpaque(false); // nền trắng / không màu theo FlatLaf

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }


    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("BÁO CÁO DOANH THU");
        title.putClientProperty("FlatLaf.style",
                "font:bold +6; foreground:$Component.accentColor");

        JLabel subtitle = new JLabel("Thống kê doanh thu theo khoảng thời gian");


        subtitle.putClientProperty("FlatLaf.style",
                "foreground:$Label.disabledForeground");

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(title);
        box.add(Box.createVerticalStrut(6));
        box.add(subtitle);

        panel.add(box, BorderLayout.WEST);
        return panel;
    }

    // ================= CENTER =================

    private JComponent buildCenter() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        actionBar.setOpaque(false);

        JButton btnView = new JButton("Xem báo cáo");
        stylePrimaryButton(btnView);
        btnView.addActionListener(e -> loadData());

        actionBar.add(new JLabel("Từ ngày"));
        actionBar.add(fromDate);
        actionBar.add(new JLabel("Đến ngày"));
        actionBar.add(toDate);
        actionBar.add(Box.createHorizontalStrut(20));
        actionBar.add(btnView);

        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.putClientProperty("FlatLaf.style",
                "arc:12;" +
                        "borderWidth:1;"
        );

        panel.add(actionBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ================= FOOTER =================

    private JComponent buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        panel.setOpaque(false);

        JLabel lblText = new JLabel("Tổng doanh thu:");
        lblText.putClientProperty("FlatLaf.style", "font:14");

        lblTotalValue.putClientProperty("FlatLaf.style",
                "font:bold +4; foreground:$Component.accentColor");

        panel.add(lblText);
        panel.add(lblTotalValue);
        return panel;
    }

    // ================= TABLE =================

    private void styleTable() {
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.putClientProperty("FlatLaf.style",
                "font:bold 13;" +
                        "background:lighten($Component.accentColor,85%);" +
                        "foreground:#0D47A1"
        );

        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof BigDecimal bd) {
                    setText(moneyFormat.format(bd));
                } else super.setValue(value);
            }
        };

        table.getColumnModel().getColumn(1).setCellRenderer(moneyRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(moneyRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(new ProfitRenderer());
    }

    private class ProfitRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);

            if (value instanceof BigDecimal bd) {
                boolean positive = bd.signum() >= 0;
                setText((positive ? "+ " : "- ") + moneyFormat.format(bd.abs()));
                putClientProperty("FlatLaf.style",
                        positive
                                ? "foreground:$Component.successColor"
                                : "foreground:$Component.errorColor");
            }
            return this;
        }
    }

    // ================= ACTION =================

    private void loadData() {
        try {
            LocalDate from = toLocalDate(fromDate);
            LocalDate to = toLocalDate(toDate);

            List<ReportRevenueRow> rows = service.getRevenueList(from, to);
            tableModel.setData(rows);

            BigDecimal total = rows.stream()
                    .map(ReportRevenueRow::getRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            lblTotalValue.setText(moneyFormat.format(total));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải báo cáo doanh thu",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= UTIL =================

    private static JSpinner createDateSpinner() {
        JSpinner sp = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );
        sp.setEditor(new JSpinner.DateEditor(sp, "dd-MM-yyyy"));
        return sp;
    }

    private static LocalDate toLocalDate(JSpinner sp) {
        return ((Date) sp.getValue())
                .toInstant()
                .atZone(TimeZone.getDefault().toZoneId())
                .toLocalDate();
    }

    // ================= BUTTON =================

    private static void stylePrimaryButton(JButton btn) {
        Dimension size = new Dimension(120, 30);
        btn.setPreferredSize(size);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));

        btn.setBackground(new Color(25,118,210)); // xanh, nào có mã màu chuẩn sửa lại sau
        btn.setForeground(Color.WHITE);

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);

    }
}
