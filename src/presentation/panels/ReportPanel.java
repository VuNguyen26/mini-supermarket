package presentation.panels;

import bus.ReportService;
import dto.ReportProduct;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.SwingConstants;


public class ReportPanel extends JPanel {

    private final ReportService reportService = new ReportService();

    private JTable table;
    private DefaultTableModel model;
    private JPanel chartPanel;
    private JSpinner fromDate;
    private JSpinner toDate;
    private JComboBox<String> typeBox;

    public ReportPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        initToolbar();
        initTable();
    }
    private void centerTableContent(JTable table) {

        /* căn giữa nội dung bảng*/
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    private void initToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Color.WHITE);
        toolbar.setOpaque(true);
        fromDate = new JSpinner(new SpinnerDateModel(new java.util.Date(), null, null, Calendar.DAY_OF_MONTH));
        toDate = new JSpinner(new SpinnerDateModel(new java.util.Date(), null, null, Calendar.DAY_OF_MONTH));

        fromDate.setEditor(new JSpinner.DateEditor(fromDate, "dd-MM-yyyy"));
        toDate.setEditor(new JSpinner.DateEditor(toDate, "dd-MM-yyyy"));

        typeBox = new JComboBox<>(new String[]{
                "Top bán chạy",
                "Top doanh thu",
                "Biểu đồ cột" // tạo biểu đồ
        });

        JButton btnLoad = new JButton("Xem báo cáo");
        btnLoad.addActionListener(e -> loadData());


        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220,220,220)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        toolbar.setBackground(new Color(245, 245, 245));

        toolbar.add(new JLabel("Từ ngày"));
        toolbar.add(fromDate);
        toolbar.add(new JLabel("Đến ngày"));
        toolbar.add(toDate);
        toolbar.add(typeBox);
        toolbar.add(btnLoad);

        add(toolbar, BorderLayout.NORTH);
    }

    private void initTable() {
        // ===== TABLE =====
        model = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên sản phẩm", "Số lượng", "Doanh thu", "Tổng"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFillsViewportHeight(true);

        centerTableContent(table); // Can giua
        DefaultTableCellRenderer headerRenderer =
                (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Danh sách báo cáo"));
        tableScroll.setBackground(Color.WHITE);
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220,220,220)),
                        BorderFactory.createEmptyBorder(24, 24, 24, 24)
                )
        );


        // ===== CHART PANEL (placeholder) =====
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                BorderFactory.createEmptyBorder(24,24,24,24)
                )
        );


        // tạm thời để label (sẽ vẽ sau)
        chartPanel.add(
                new JLabel("Biểu đồ sẽ hiển thị ở đây", SwingConstants.CENTER),
                BorderLayout.CENTER
        );

        // ===== SPLIT PANE =====
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                tableScroll,
                chartPanel
        );

        splitPane.setResizeWeight(0.6); // 60% bảng, 40% biểu đồ
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    private LocalDate convert(JSpinner spinner) {
        return ((java.util.Date) spinner.getValue())
                .toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }
    private void showBarChart(LocalDate from, LocalDate to) {
        List<ReportProduct> list =
                reportService.getTopProducts(from, to, "qty", 10);

        chartPanel.removeAll();
        chartPanel.add(new BarChartPanel(list), BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void loadTableReport(LocalDate from, LocalDate to, int typeIndex) {
        /* load danh sach báo cáo
        *
        * */

        String type = typeIndex == 0 ? "qty" : "revenue";

        List<ReportProduct> list =
                reportService.getTopProducts(from, to, type, 10);

        model.setRowCount(0);

        for (ReportProduct rp : list) {
            model.addRow(new Object[]{
                    rp.getProductId(),
                    rp.getProductName(),
                    rp.getTotalQuantity(),
                    rp.getTotalRevenue()
            });
        }
    }

    private void loadData() {
        try {
            LocalDate from = convert(fromDate);
            LocalDate to = convert(toDate);

            int typeIndex = typeBox.getSelectedIndex();

            if (typeIndex <= 1) {
                loadTableReport(from, to, typeIndex);
            } else if (typeIndex == 2) {
                showBarChart(from, to);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải báo cáo",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}


class BarChartPanel extends JPanel {

    private final List<ReportProduct> data;

    BarChartPanel(List<ReportProduct> data) {
        this.data = data;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        int w = getWidth();
        int h = getHeight();
        int barWidth = w / data.size();

        // Long -> int (đúng kiểu)
        int max = data.stream()
                .mapToInt(rp -> rp.getTotalQuantity().intValue())
                .max()
                .orElse(1);

        for (int i = 0; i < data.size(); i++) {
            int value = data.get(i).getTotalQuantity().intValue();

            int barHeight = (int) ((double) value / max * (h - 60));

            g.setColor(new Color(100, 149, 237));
            g.fillRect(
                    i * barWidth + 20,
                    h - barHeight - 30,
                    barWidth - 40,
                    barHeight
            );

            g.setColor(Color.BLACK);
            g.drawString(
                    data.get(i).getProductName(),
                    i * barWidth + 20,
                    h - 10
            );
        }
    }
}


