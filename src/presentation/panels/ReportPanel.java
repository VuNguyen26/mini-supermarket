package presentation.panels;

import bus.ReportService;
import dal.dao.SalesInvoiceDAO;
import dto.InventoryLot;
import dto.SalesInvoice;
import util.PermissionCodes;
import util.RolePermission;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportPanel extends JPanel {
    private final ReportService reportService = new ReportService();

    private JTabbedPane tabbedPane;
    private JPanel revenuePanel;
    private JPanel topProductsPanel;
    private JPanel inventoryPanel;

    private JTextField startDateField;
    private JTextField endDateField;
    private JButton generateReportBtn;

    public ReportPanel() {
        initComponents();
        setupLayout();
        setupPermissions();
        loadInitialData();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Revenue Report Tab
        revenuePanel = createRevenuePanel();
        tabbedPane.addTab("Doanh thu", revenuePanel);

        // Top Products Tab
        topProductsPanel = createTopProductsPanel();
        tabbedPane.addTab("Top sản phẩm", topProductsPanel);

        // Inventory by Expiry Tab
        inventoryPanel = createInventoryPanel();
        tabbedPane.addTab("Tồn kho theo HSD", inventoryPanel);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupPermissions() {
        // Check if user has report view permission
        if (!RolePermission.has(PermissionCodes.REPORT_VIEW)) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xem báo cáo",
                                        "Không có quyền", JOptionPane.WARNING_MESSAGE);
            setEnabled(false);
        }
    }

    private void loadInitialData() {
        // Load current month data by default
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        startDateField.setText(monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        endDateField.setText(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        generateRevenueReport();
        generateTopProductsReport();
        generateInventoryReport();
    }

    private JPanel createRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Date range selector
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Từ ngày:"));
        startDateField = new JTextField(20);
        datePanel.add(startDateField);

        datePanel.add(new JLabel("Đến ngày:"));
        endDateField = new JTextField(20);
        datePanel.add(endDateField);

        generateReportBtn = new JButton("Tạo báo cáo");
        generateReportBtn.addActionListener(e -> generateRevenueReport());
        datePanel.add(generateReportBtn);

        panel.add(datePanel, BorderLayout.NORTH);

        // Revenue display
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel totalRevenueLabel = new JLabel("Tổng doanh thu: 0 VND");
        totalRevenueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        contentPanel.add(totalRevenueLabel);

        JLabel invoiceCountLabel = new JLabel("Số hóa đơn: 0");
        invoiceCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        contentPanel.add(invoiceCountLabel);

        // Store labels for updating
        panel.putClientProperty("totalRevenueLabel", totalRevenueLabel);
        panel.putClientProperty("invoiceCountLabel", invoiceCountLabel);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Sản phẩm", "Số lượng bán"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Store table for updating
        panel.putClientProperty("table", table);
        panel.putClientProperty("model", model);

        return panel;
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Mã lô", "Sản phẩm", "Số lượng", "Hạn sử dụng", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Store table for updating
        panel.putClientProperty("table", table);
        panel.putClientProperty("model", model);

        return panel;
    }

    private void generateRevenueReport() {
        try {
            LocalDateTime startDate = LocalDateTime.parse(startDateField.getText(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime endDate = LocalDateTime.parse(endDateField.getText(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            BigDecimal revenue = reportService.getRevenueReport(startDate, endDate);

            // Get invoice count
            List<SalesInvoice> invoices = new SalesInvoiceDAO().getInvoicesByDateRange(startDate, endDate);
            int invoiceCount = invoices.size();

            // Update labels
            JLabel totalRevenueLabel = (JLabel) revenuePanel.getClientProperty("totalRevenueLabel");
            totalRevenueLabel.setText("Tổng doanh thu: " + revenue + " VND");

            JLabel invoiceCountLabel = (JLabel) revenuePanel.getClientProperty("invoiceCountLabel");
            invoiceCountLabel.setText("Số hóa đơn: " + invoiceCount);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo báo cáo doanh thu: " + e.getMessage(),
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateTopProductsReport() {
        try {
            LocalDateTime startDate = LocalDateTime.parse(startDateField.getText(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime endDate = LocalDateTime.parse(endDateField.getText(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            List<ReportRow> topProducts = reportService.getTopSellingProducts(startDate, endDate, 10);

            DefaultTableModel model = (DefaultTableModel) topProductsPanel.getClientProperty("model");
            model.setRowCount(0);

            for (ReportRow row : topProducts) {
                model.addRow(new Object[]{row.getLabel(), row.getValue()});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo báo cáo top sản phẩm: " + e.getMessage(),
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateInventoryReport() {
        try {
            List<InventoryLot> lots = reportService.getInventoryByExpiryDate();

            DefaultTableModel model = (DefaultTableModel) inventoryPanel.getClientProperty("model");
            model.setRowCount(0);

            for (InventoryLot lot : lots) {
                model.addRow(new Object[]{
                    lot.getLotCode(),
                    lot.getProductName(),
                    lot.getQuantity(),
                    lot.getExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    lot.getStatus()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo báo cáo tồn kho: " + e.getMessage(),
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
