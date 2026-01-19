package presentation.panels;

import bus.AuthService.AuthUser;
import bus.StockAdjustmentService;
import dto.StockAdjustment;
import presentation.dialogs.StockAdjustmentDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.util.List;

public class StockAdjustmentPanel extends JPanel {

    private final AuthUser currentUser;
    private final StockAdjustmentService service = new StockAdjustmentService();
    private JTable table;
    private DefaultTableModel model;

    public StockAdjustmentPanel(AuthUser currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== Header (Title + Add button) =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel("Danh sách kiểm kho");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(15, 23, 42));
        headerPanel.add(title, BorderLayout.WEST);

        // Add button
        JButton addButton = new JButton("+ Thêm");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        addButton.setForeground(Color.WHITE);
        addButton.setBackground(new Color(37, 99, 235)); // blue-600
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect nhẹ
        addButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                addButton.setBackground(new Color(29, 78, 216)); // blue-700
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                addButton.setBackground(new Color(37, 99, 235));
            }
        });

        // Gọi dialog thêm người dùng
        addButton.addActionListener(e -> {
            StockAdjustmentDialog dialog =
                    new StockAdjustmentDialog(
                            SwingUtilities.getWindowAncestor(this),
                            currentUser
                    );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadData();
            }
        });

        headerPanel.add(addButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ===== Table =====
        model = new DefaultTableModel(
                new Object[]{"ID", "Mã phiếu", "Lý do", "Trạng thái", "Ngày tạo", "Ghi chú", "Tương tác"},
                0
        );

        table = new JTable(model);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(226, 232, 240));
        header.setForeground(new Color(15, 23, 42));

        table.setRowHeight(40);

        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        table.getColumnModel().getColumn(0).setCellRenderer(center); 
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center); 
        table.getColumnModel().getColumn(3).setCellRenderer(center); 
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(left);
        table.getColumnModel().getColumn(6).setCellRenderer(center);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData() {
        model.setRowCount(0); // clear table

        List<StockAdjustment> list = service.getAll();

        for (StockAdjustment sa : list) {
            model.addRow(new Object[]{
                    sa.getSaId(),
                    sa.getSaCode(),
                    sa.getReason(),   // enum OK
                    sa.getStatus(),   // enum OK
                    sa.getCreatedAt(),
                    sa.getNote()
            });
        }
    }
}
