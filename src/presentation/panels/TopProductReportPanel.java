package presentation.panels;

import bus.ReportService;
import dto.ReportProduct;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;

public class TopProductReportPanel extends JPanel {

    private final ReportService service;
    private final DefaultTableModel model;
    private final JTable table;

    private final JSpinner fromDate;
    private final JSpinner toDate;

    public TopProductReportPanel(ReportService service) {
        this.service = service;

        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        setBackground(Color.WHITE);
        setOpaque(true);

        fromDate = createDateSpinner();
        toDate = createDateSpinner();

        JPanel actionBar = buildActionBar();

        model = new DefaultTableModel(
                new Object[]{"ID", "Tên sản phẩm", "Số lượng", "Doanh thu"}, 0
        );
        table = new JTable(model);

        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.putClientProperty("FlatLaf.style",
                "arc:12;" +
                        "borderWidth:1;" +
                        "borderColor:lighten($Component.accentColor,40%)"
        );

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        topPanel.add(buildTitle(), BorderLayout.NORTH);
        topPanel.add(actionBar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    private JComponent buildTitle() {
        JLabel title = new JLabel("TOP SẢN PHẨM BÁN CHẠY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(25, 118, 210)); // xanh chủ đạo
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.add(title);

        return panel;
    }


    // ================= ACTION BAR =================

    private JPanel buildActionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);

        JButton btnView = new JButton("Xem báo cáo");
        stylePrimaryButton(btnView);
        btnView.addActionListener(e -> load());

        panel.add(new JLabel("Từ ngày"));
        panel.add(fromDate);
        panel.add(new JLabel("Đến ngày"));
        panel.add(toDate);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnView);

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

        // căn giữa
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // ================= ACTION =================

    private void load() {
        try {
            LocalDate from = convert(fromDate);
            LocalDate to = convert(toDate);

            List<ReportProduct> list = service.getTopProducts(from, to, 10);
            model.setRowCount(0);

            for (ReportProduct r : list) {
                model.addRow(new Object[]{
                        r.getProductId(),
                        r.getProductName(),
                        r.getTotalQuantity(),
                        r.getTotalRevenue()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải top sản phẩm",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private JSpinner createDateSpinner() {
        JSpinner sp = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );
        sp.setEditor(new JSpinner.DateEditor(sp, "dd-MM-yyyy"));
        return sp;
    }

    private LocalDate convert(JSpinner sp) {
        return ((Date) sp.getValue())
                .toInstant()
                .atZone(TimeZone.getDefault().toZoneId())
                .toLocalDate();
    }


    private static void stylePrimaryButton(JButton btn) {
        Dimension size = new Dimension(120, 30);
        btn.setPreferredSize(size);

        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        btn.setBackground(new Color(25, 118, 210));
        btn.setForeground(Color.WHITE);

        btn.putClientProperty("FlatLaf.style",
                "arc:10; borderWidth:0");

        btn.setFocusPainted(false);
        btn.setOpaque(true);
    }
}
