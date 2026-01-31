package presentation.panels;

import bus.ReportService;
import dto.InventoryLot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExpiryStockReportPanel extends JPanel {

    private final ReportService reportService;
    private final DefaultTableModel model;
    private final JComboBox<String> filter;
    private final JTable table;

    public ExpiryStockReportPanel(ReportService service) {
        this.reportService = service;

        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("BÁO CÁO TỒN KHO - HẠN SỬ DỤNG");
        title.putClientProperty("FlatLaf.style",
                "font:bold +6; foreground:$Component.accentColor");

        JLabel subtitle = new JLabel("Danh sách sản phẩm sắp hết hạn hoặc đã hết hạn");
        subtitle.putClientProperty("FlatLaf.style",
                "foreground:$Label.disabledForeground");

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        // ===== ACTION BAR =====
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        actionBar.setBackground(Color.WHITE);

        filter = new JComboBox<>(new String[]{"< 30 ngày", "< 60 ngày", "Đã hết hạn"});

        JButton btnLoad = new JButton("Xem báo cáo");
        stylePrimaryButton(btnLoad);
        btnLoad.addActionListener(e -> loadData());

        actionBar.add(new JLabel("Lọc theo:"));
        actionBar.add(filter);
        actionBar.add(btnLoad);

        // ===== TABLE =====
        model = new DefaultTableModel(
                new Object[]{"Mã SP", "Lô", "Số lượng còn", "HSD"}, 0
        );

        table = new JTable(model);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.putClientProperty("FlatLaf.style",
                "arc:12; borderColor:$Component.borderColor");

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(actionBar, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    // ================= STYLE TABLE =================
    private void styleTable() {
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.putClientProperty("FlatLaf.style",
                "font:bold 13;" +
                        "background:lighten($Component.accentColor,85%);" +
                        "foreground:#0D47A1;" +
                        "borderColor:lighten($Component.accentColor,70%)"
        );

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // ================= ACTION =================
    private void loadData() {
        model.setRowCount(0);

        int days = switch (filter.getSelectedIndex()) {
            case 0 -> 30;
            case 1 -> 60;
            default -> -1;
        };

        try {
            List<InventoryLot> lots = reportService.getExpiryStock(days);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (InventoryLot lot : lots) {
                model.addRow(new Object[]{
                        lot.getProductId(),
                        lot.getLotId(),
                        lot.getQtyRemaining(),
                        lot.getExpiry() != null
                                ? lot.getExpiry().format(fmt)
                                : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải dữ liệu tồn kho",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= BUTTON STYLE =================
    private static void stylePrimaryButton(JButton btn) {
        Dimension size = new Dimension(120, 30);
        btn.setPreferredSize(size);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));

        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(25, 118, 243));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
    }
}
