package presentation.panels;

import dal.dao.AuditLogDAO;
import dto.AuditLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuditLogPanel extends JPanel {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtKeyword;
    private JComboBox<String> cbAction;

    public AuditLogPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        initToolbar();
        initTable();
        loadData();
    }

    // ================= TOOLBAR =================
    private void initToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

        txtKeyword = new JTextField(18);
        cbAction = new JComboBox<>(new String[]{
                "Tất cả",
                "CREATE",
                "UPDATE",
                "DELETE",
                "LOGIN",
                "LOGOUT"
        });

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.addActionListener(e -> loadData());

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtKeyword.setText("");
            cbAction.setSelectedIndex(0);
            loadData();
        });

        toolbar.add(new JLabel("Từ khóa"));
        toolbar.add(txtKeyword);
        toolbar.add(new JLabel("Hành động"));
        toolbar.add(cbAction);
        toolbar.add(btnSearch);
        toolbar.add(btnRefresh);

        add(toolbar, BorderLayout.NORTH);
    }

    // ================= TABLE =================
    private void initTable() {
        model = new DefaultTableModel(
                new Object[]{
                        "Thời gian",
                        "Người dùng",
                        "Hành động",
                        "Đối tượng",
                        "Chi tiết"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // ================= LOAD DATA =================
    private void loadData() {
        try {
            String keyword = txtKeyword.getText().trim();
            String action = cbAction.getSelectedIndex() == 0
                    ? null
                    : cbAction.getSelectedItem().toString();

            List<AuditLog> logs = auditLogDAO.search(keyword, action);

            model.setRowCount(0);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (AuditLog log : logs) {
                model.addRow(new Object[]{
                        log.getCreatedAt().format(fmt),
                        log.getUsername(),
                        log.getAction(),
                        log.getEntityName(),
                        log.getDescription()
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải nhật ký hệ thống",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
