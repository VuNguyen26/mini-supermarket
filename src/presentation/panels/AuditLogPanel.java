package presentation.panels;

import bus.AuditLogService;
import dto.AuditLog;
import util.PermissionCodes;
import util.RolePermission;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuditLogPanel extends JPanel {
    private final AuditLogService auditLogService = new AuditLogService();

    private JTable auditTable;
    private DefaultTableModel tableModel;
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<String> actionFilter;
    private JButton filterBtn;
    private JButton refreshBtn;

    public AuditLogPanel() {
        initComponents();
        setupLayout();
        setupPermissions();
        loadAuditLogs();
    }

    private void initComponents() {
        // Table
        String[] columnNames = {"Thời gian", "Người dùng", "Hành động", "Bảng", "Mã bản ghi", "IP", "Chi tiết"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        auditTable = new JTable(tableModel);
        auditTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        auditTable.getTableHeader().setReorderingAllowed(false);

        // Filters
        startDateField = new JTextField(15);
        endDateField = new JTextField(15);
        actionFilter = new JComboBox<>(new String[]{"Tất cả", "INSERT", "UPDATE", "DELETE", "LOGIN", "LOGOUT"});
        filterBtn = new JButton("Lọc");
        refreshBtn = new JButton("Làm mới");

        // Set default date range (last 7 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        startDateField.setText(weekAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        endDateField.setText(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // Event listeners
        filterBtn.addActionListener(e -> filterAuditLogs());
        refreshBtn.addActionListener(e -> loadAuditLogs());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        filterPanel.add(new JLabel("Từ ngày:"));
        filterPanel.add(startDateField);
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(endDateField);
        filterPanel.add(new JLabel("Hành động:"));
        filterPanel.add(actionFilter);
        filterPanel.add(filterBtn);
        filterPanel.add(refreshBtn);

        add(filterPanel, BorderLayout.NORTH);

        // Table panel
        JScrollPane scrollPane = new JScrollPane(auditTable);
        scrollPane.setBorder(new EmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Details panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        detailsPanel.setPreferredSize(new Dimension(0, 150));

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setLineWrap(true);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Chi tiết thay đổi"));

        detailsPanel.add(detailsScroll, BorderLayout.CENTER);

        // Show details when row selected
        auditTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = auditTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String oldValues = (String) tableModel.getValueAt(selectedRow, 6);
                    String newValues = (String) tableModel.getValueAt(selectedRow, 7);
                    detailsArea.setText("Trước:\n" + (oldValues != null ? oldValues : "N/A") +
                                      "\n\nSau:\n" + (newValues != null ? newValues : "N/A"));
                }
            }
        });

        add(detailsPanel, BorderLayout.SOUTH);
    }

    private void setupPermissions() {
        // Check if user has audit view permission
        if (!RolePermission.has(PermissionCodes.AUDIT_VIEW)) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xem lịch sử thao tác",
                                        "Không có quyền", JOptionPane.WARNING_MESSAGE);
            setEnabled(false);
        }
    }

    private void loadAuditLogs() {
        try {
            List<AuditLog> logs = auditLogService.getAllAuditLogs();
            updateTable(logs);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải lịch sử thao tác: " + e.getMessage(),
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterAuditLogs() {
        try {
            LocalDateTime startDate = LocalDateTime.parse(startDateField.getText(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime endDate = LocalDateTime.parse(endDateField.getText(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);

            String selectedAction = (String) actionFilter.getSelectedItem();
            if (!"Tất cả".equals(selectedAction)) {
                logs = logs.stream()
                        .filter(log -> selectedAction.equals(log.getAction()))
                        .toList();
            }

            updateTable(logs);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lọc lịch sử thao tác: " + e.getMessage(),
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<AuditLog> logs) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (AuditLog log : logs) {
            tableModel.addRow(new Object[]{
                log.getTimestamp().format(formatter),
                log.getUserName(),
                log.getAction(),
                log.getTableName(),
                log.getRecordId(),
                log.getIpAddress(),
                log.getOldValues(),
                log.getNewValues()
            });
        }
    }
}
