package presentation.panels;

import bus.AuditLogService;
import dto.AuditLog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuditLogPanel extends JPanel {

    private final AuditLogService service;

    private final DefaultTableModel model;
    private final JTable table;

    private static final Color MAIN_BLUE = new Color(33, 150, 243);
    private static final Color LIGHT_BLUE_BORDER = new Color(180, 210, 245);

    public AuditLogPanel(AuditLogService service) {
        this.service = service;

        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ===== HEADER =====
        JLabel title = new JLabel("AUDIT LOG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(MAIN_BLUE);

        JLabel subtitle = new JLabel("Lịch sử thao tác người dùng trong hệ thống");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitle);

        add(headerPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(
                new Object[]{"Người dùng", "Hành động", "Loại", "ID", "Chi tiết", "Thời gian"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(new RoundedBorder(LIGHT_BLUE_BORDER, 16));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        add(tableWrapper, BorderLayout.CENTER);

        // ❌ KHÔNG gọi load() ở đây nữa
        // load();
    }

    /** ✅ Gọi hàm này khi user mở tab "Audit log" */
    public void reload() {
        load();
    }

    private void styleTable() {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(220, 235, 255));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(MAIN_BLUE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void load() {
        try {
            List<AuditLog> logs = service.getAll();
            model.setRowCount(0);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (AuditLog a : logs) {
                model.addRow(new Object[]{
                        a.getUsername(),
                        a.getAction(),
                        a.getEntityName(),
                        a.getEntityId() != null ? a.getEntityId() : "-",
                        a.getDescription(),
                        a.getCreatedAt() != null ? a.getCreatedAt().format(fmt) : ""
                });
            }
        } catch (Exception e) {
            // ✅ In lỗi thật ra console để biết thiếu bảng/cột/SQL/DB...
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải audit log:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ===== Rounded Border Class =====
    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 8, 8, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(8, 8, 8, 8);
            return insets;
        }
    }
}
