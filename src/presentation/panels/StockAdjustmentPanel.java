package presentation.panels;

import bus.AuthService.AuthUser;
import bus.StockAdjustmentService;
import dto.StockAdjustment;
import dto.StockAdjustmentDetail;
import dto.StockAdjustmentStatus;
import presentation.dialogs.StockAdjustmentDetailDialog;
import presentation.dialogs.StockAdjustmentDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class StockAdjustmentPanel extends JPanel {

    private final AuthUser currentUser;
    private final StockAdjustmentService saService = new StockAdjustmentService();

    private JTable tblAdjustment;
    private JTable tblDetail;

    private DefaultTableModel adjustmentModel;
    private DefaultTableModel detailModel;

    private StockAdjustment selectedAdjustment;

    public StockAdjustmentPanel(AuthUser currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadAdjustments();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    /* ================= HEADER ================= */

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Danh sách kiểm kho");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(title, BorderLayout.WEST);

        JButton btnAdd = new JButton("+ Thêm phiếu");
        btnAdd.addActionListener(e -> {
            StockAdjustmentDialog dialog =
                    new StockAdjustmentDialog(
                            SwingUtilities.getWindowAncestor(this),
                            currentUser
                    );
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadAdjustments();
            }
        });

        panel.add(btnAdd, BorderLayout.EAST);
        return panel;
    }

    /* ================= MAIN ================= */

    private JComponent buildMainContent() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildAdjustmentTable(),
                buildDetailSection()
        );
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0.5);
        return splitPane;
    }

    /* ================= TABLE: ADJUSTMENT ================= */

    private JComponent buildAdjustmentTable() {
        adjustmentModel = new DefaultTableModel(
                new Object[]{"ID", "Mã phiếu", "Lý do", "Trạng thái", "Ngày tạo", "Ghi chú", "Tương tác"},
                0
        ) {
            public boolean isCellEditable(int r, int c) {
                return c == 6;
            }
        };

        tblAdjustment = new JTable(adjustmentModel);
        tblAdjustment.setRowHeight(36);
        tblAdjustment.setShowGrid(true);
        tblAdjustment.setGridColor(new Color(220, 220, 220));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        tblAdjustment.getColumnModel().getColumn(0).setCellRenderer(center); 
        tblAdjustment.getColumnModel().getColumn(1).setCellRenderer(center);
        tblAdjustment.getColumnModel().getColumn(2).setCellRenderer(center); 
        tblAdjustment.getColumnModel().getColumn(3).setCellRenderer(center); 
        tblAdjustment.getColumnModel().getColumn(4).setCellRenderer(center);
        tblAdjustment.getColumnModel().getColumn(5).setCellRenderer(left);
        tblAdjustment.getColumnModel().getColumn(6).setCellRenderer(center);
        tblAdjustment.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        tblAdjustment.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());


        // Click → load chi tiết
        tblAdjustment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = tblAdjustment.getSelectedRow();
                    if (row >= 0) {
                        selectedAdjustment =
                                saService.getById(
                                        (int) adjustmentModel.getValueAt(row, 0)
                                );
                        loadDetails(selectedAdjustment);
                    }
                }
            }
        });

        return new JScrollPane(tblAdjustment);
    }

    /* ================= DETAIL SECTION ================= */

    private JComponent buildDetailSection() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JLabel title = new JLabel("Chi tiết phiếu kiểm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        detailModel = new DefaultTableModel(
                new Object[]{"ID", "Sản phẩm", "Lô", "SL hệ thống", "SL kiểm", "Chênh lệch", "Ghi chú"},
                0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblDetail = new JTable(detailModel);
        tblDetail.setRowHeight(32);
        tblDetail.setShowGrid(true);
        tblDetail.setGridColor(new Color(220, 220, 220));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        tblDetail.getColumnModel().getColumn(0).setCellRenderer(center); 
        tblDetail.getColumnModel().getColumn(1).setCellRenderer(center);
        tblDetail.getColumnModel().getColumn(2).setCellRenderer(center); 
        tblDetail.getColumnModel().getColumn(3).setCellRenderer(center); 
        tblDetail.getColumnModel().getColumn(4).setCellRenderer(center);
        tblDetail.getColumnModel().getColumn(5).setCellRenderer(center);
        tblDetail.getColumnModel().getColumn(6).setCellRenderer(left);

        panel.add(new JScrollPane(tblDetail), BorderLayout.CENTER);
        panel.add(buildDetailButtons(), BorderLayout.SOUTH);

        return panel;
    }

    /* ================= DETAIL BUTTONS ================= */

    private JComponent buildDetailButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAdd = new JButton("Thêm");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");


        // ===== THÊM =====
        btnAdd.addActionListener(e -> {
            if (!canEditDetail()) return;

            StockAdjustmentDetailDialog dialog =
                    new StockAdjustmentDetailDialog(
                            SwingUtilities.getWindowAncestor(this),
                            selectedAdjustment.getSaId()
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadDetails(selectedAdjustment);
            }
        });

        // ===== SỬA =====
        btnEdit.addActionListener(e -> {
            if (!canEditDetail()) return;

            int row = tblDetail.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa");
                return;
            }

            int sadId = (int) detailModel.getValueAt(row, 0);

            StockAdjustmentDetail detail =
                    saService.getDetailById(sadId); // 🔥 QUAN TRỌNG

            StockAdjustmentDetailDialog dialog =
                    new StockAdjustmentDetailDialog(
                            SwingUtilities.getWindowAncestor(this),
                            detail
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadDetails(selectedAdjustment);
            }
        });

        // ===== XÓA =====
        btnDelete.addActionListener(e -> {
            if (!canEditDetail()) return;

            int row = tblDetail.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng để xóa");
                return;
            }

            int sadId = (int) detailModel.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa dòng này?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                saService.deleteDetail(sadId);
                loadDetails(selectedAdjustment);
            }
        });

        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnDelete);
        return panel;
    }

    private boolean canEditDetail() {
        if (selectedAdjustment == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn phiếu kiểm kho");
            return false;
        }
        if (selectedAdjustment.getStatus() != StockAdjustmentStatus.DRAFT) {
            JOptionPane.showMessageDialog(this, "Phiếu không ở trạng thái DRAFT");
            return false;
        }
        return true;
    }

    /* ================= LOAD DATA ================= */

    private void loadAdjustments() {
        adjustmentModel.setRowCount(0);
        List<StockAdjustment> list = saService.getAll();
        for (StockAdjustment sa : list) {
            adjustmentModel.addRow(new Object[]{
                    sa.getSaId(),
                    sa.getSaCode(),
                    sa.getReason(),
                    sa.getStatus(),
                    sa.getCreatedAt(),
                    sa.getNote(),
                    sa
            });
        }
        detailModel.setRowCount(0);
        selectedAdjustment = null;
    }

    private void loadDetails(StockAdjustment sa) {
        detailModel.setRowCount(0);
        List<StockAdjustmentDetail> list =
                saService.getByStockAdjustment(sa.getSaId());

        for (StockAdjustmentDetail d : list) {
            detailModel.addRow(new Object[]{
                    d.getSadId(),
                    d.getProductName(),
                    d.getLotCode(),
                    d.getSystemQty(),
                    d.getCountedQty(),
                    d.getDiffQty(),
                    d.getNote(),
            });
        }
    }

    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        private final JButton editBtn = new JButton("Sửa");

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
            setOpaque(true);
            editBtn.setFocusable(false);
            add(editBtn);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            removeAll();

            StockAdjustment sa = (StockAdjustment) value;

            // ===== Đồng bộ màu với JTable =====
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 1, table.getGridColor()
            ));

            if (sa.getStatus() == dto.StockAdjustmentStatus.DRAFT) {
                add(editBtn);
            }

            return this;
        }
    }


    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        private final JButton editBtn = new JButton("Sửa");
        private StockAdjustment currentSA;

        public ActionCellEditor() {
            panel.setOpaque(true);
            editBtn.setFocusable(false);
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                openEditDialog();
            });

            panel.add(editBtn);
        }

        private void openEditDialog() {

            // 🔥 LOAD LẠI DATA MỚI NHẤT TỪ DB
            StockAdjustment freshSA =
                    saService.getById(currentSA.getSaId());

            StockAdjustmentDialog dialog =
                    new StockAdjustmentDialog(
                            SwingUtilities.getWindowAncestor(panel),
                            currentUser,
                            freshSA
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadAdjustments();
                tblAdjustment.revalidate();
                tblAdjustment.repaint();
            }
        }

        @Override
        public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected,
            int row, int column) {

            currentSA = (StockAdjustment) value;
            panel.removeAll();

            // ===== Đồng bộ màu =====
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }

            panel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 1, table.getGridColor()
            ));

            if (currentSA.getStatus() == dto.StockAdjustmentStatus.DRAFT) {
                panel.add(editBtn);
            }

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentSA;
        }
    }

}
