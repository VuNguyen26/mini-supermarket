package presentation.panels;

import bus.AuthService.AuthUser;
import bus.StockAdjustmentService;
import dto.StockAdjustment;
import dto.StockAdjustmentDetail;
import dto.StockAdjustmentStatus;
import presentation.dialogs.StockAdjustmentDetailDialog;
import presentation.dialogs.StockAdjustmentDialog;
import util.RolePermission;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StockAdjustmentPanel extends JPanel {

    private final AuthUser currentUser;
    private final StockAdjustmentService saService = new StockAdjustmentService();

    private JTable tblAdjustment;
    private JTable tblDetail;
    private JTextField txtSearch;

    private DefaultTableModel adjustmentModel;
    private DefaultTableModel detailModel;

    private StockAdjustment selectedAdjustment;

    private boolean canView;
    private boolean canCreate;
    private boolean canUpdate;
    private boolean canDelete;

    private JButton btnAddHeader;
    private JButton btnViewHeader;
    private JButton btnExcelHeader;

    private JButton btnAddDetail;
    private JButton btnEditDetail;
    private JButton btnDeleteDetail;

    public StockAdjustmentPanel(AuthUser currentUser) {
        this.currentUser = currentUser;
        initPermissions();
        initUI();
        applyPermissions();
        loadAdjustments("");

        initUI();
        applyPermissions();
        loadAdjustments("");
    }

    private void initPermissions() {
        canView = RolePermission.has("ADJUSTMENT_VIEW");
        canCreate = RolePermission.has("ADJUSTMENT_CREATE");
        canUpdate = RolePermission.has("ADJUSTMENT_UPDATE");
        canDelete = RolePermission.has("ADJUSTMENT_DELETE");

        if (!canUpdate) canUpdate = RolePermission.has("ADJUSTMENT_MANAGE");
        if (!canDelete) canDelete = RolePermission.has("ADJUSTMENT_MANAGE");
    }

    /**
     * Hàm này dùng reflection để đỡ phụ thuộc chính xác AuthUser đang có method gì.
     * Nó sẽ thử lần lượt:
     * - hasPermission(String)
     * - hasPermCode(String)
     * - hasRolePermission(String)
     * - getPermissions() rồi dò code trong collection
     */
    @SuppressWarnings("unchecked")
    private boolean hasPermission(String permCode) {
        if (currentUser == null || permCode == null || permCode.isBlank()) {
            return false;
        }

        try {
            Method m = currentUser.getClass().getMethod("hasPermission", String.class);
            Object rs = m.invoke(currentUser, permCode);
            if (rs instanceof Boolean) return (Boolean) rs;
        } catch (Exception ignored) {}

        try {
            Method m = currentUser.getClass().getMethod("hasPermCode", String.class);
            Object rs = m.invoke(currentUser, permCode);
            if (rs instanceof Boolean) return (Boolean) rs;
        } catch (Exception ignored) {}

        try {
            Method m = currentUser.getClass().getMethod("hasRolePermission", String.class);
            Object rs = m.invoke(currentUser, permCode);
            if (rs instanceof Boolean) return (Boolean) rs;
        } catch (Exception ignored) {}

        try {
            Method m = currentUser.getClass().getMethod("getPermissions");
            Object rs = m.invoke(currentUser);
            if (rs instanceof Collection<?>) {
                for (Object item : (Collection<Object>) rs) {
                    if (item == null) continue;

                    if (permCode.equalsIgnoreCase(String.valueOf(item))) {
                        return true;
                    }

                    try {
                        Method getCode = item.getClass().getMethod("getPermCode");
                        Object code = getCode.invoke(item);
                        if (code != null && permCode.equalsIgnoreCase(String.valueOf(code))) {
                            return true;
                        }
                    } catch (Exception ignored) {}

                    try {
                        Method getCode = item.getClass().getMethod("getCode");
                        Object code = getCode.invoke(item);
                        if (code != null && permCode.equalsIgnoreCase(String.valueOf(code))) {
                            return true;
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    private void refreshDashboard() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof presentation.MainFrame) {
            ((presentation.MainFrame) owner).refreshDashboard();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    private void applyPermissions() {
        if (btnAddHeader != null) btnAddHeader.setEnabled(canCreate);

        // Đã vào được panel thì vẫn cho xem chi tiết + xuất Excel
        if (btnViewHeader != null) btnViewHeader.setEnabled(true);
        if (btnExcelHeader != null) btnExcelHeader.setEnabled(true);

        if (btnAddDetail != null) btnAddDetail.setEnabled(canCreate);
        if (btnEditDetail != null) btnEditDetail.setEnabled(canUpdate);
        if (btnDeleteDetail != null) btnDeleteDetail.setEnabled(canDelete);
    }

    /* ================= HEADER ================= */

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 40));

        JLabel title = new JLabel("Danh sách kiểm kho");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setMinimumSize(new Dimension(150, 20));
        txtSearch.setPreferredSize(new Dimension(250, 28));
        txtSearch.setMaximumSize(new Dimension(270, 30));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        txtSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            loadAdjustments(keyword);
        });

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.add(title);
        leftPanel.add(txtSearch);

        panel.add(leftPanel, BorderLayout.WEST);

        btnAddHeader = new JButton("+ Thêm phiếu");
        btnViewHeader = new JButton("Xem chi tiết");
        btnExcelHeader = new JButton("Xuất Excel");

        btnAddHeader.setPreferredSize(new Dimension(135, 30));
        btnViewHeader.setPreferredSize(new Dimension(120, 30));
        btnExcelHeader.setPreferredSize(new Dimension(110, 30));

        btnAddHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnViewHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExcelHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnAddHeader.setBackground(new Color(40, 167, 69));
        btnAddHeader.setForeground(Color.WHITE);
        btnViewHeader.setBackground(new Color(0, 123, 255));
        btnViewHeader.setForeground(Color.WHITE);
        btnExcelHeader.setBackground(new Color(29, 111, 66));
        btnExcelHeader.setForeground(Color.WHITE);

        btnAddHeader.addActionListener(e -> {
            if (!canCreate) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền thêm phiếu kiểm kho.");
                return;
            }

            StockAdjustmentDialog dialog =
                    new StockAdjustmentDialog(
                            SwingUtilities.getWindowAncestor(this),
                            currentUser
                    );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                txtSearch.setText("");
                loadAdjustments("");
                refreshDashboard();
            }
        });

        btnViewHeader.addActionListener(e -> {
            if (selectedAdjustment == null) {
                JOptionPane.showMessageDialog(this, "Chưa chọn phiếu để xem");
                return;
            }

            StockAdjustmentDialog dialog =
                    new StockAdjustmentDialog(
                            SwingUtilities.getWindowAncestor(this),
                            selectedAdjustment
                    );
            dialog.setVisible(true);
        });

        btnExcelHeader.addActionListener(e -> {
            int row = tblAdjustment.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn 1 phiếu để xuất chi tiết");
                return;
            }

            int saId = (int) tblAdjustment.getValueAt(row, 0);
            List<StockAdjustmentDetail> list = saService.getByStockAdjustment(saId);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất Excel!");
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Lưu tại");
            chooser.setSelectedFile(new File("Stock_Adjustment.xlsx"));
            chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            int result = chooser.showSaveDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                if (file.exists()) {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "File đã tồn tại. Ghi đè?",
                            "Xác nhận",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm != JOptionPane.YES_OPTION) return;
                }

                if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                    file = new File(file.getAbsolutePath() + ".xlsx");
                }

                try {
                    Workbook workbook = new XSSFWorkbook();
                    Sheet sheet = workbook.createSheet("Chi tiết phiếu kiểm kho");

                    Row titleRow = sheet.createRow(0);
                    titleRow.createCell(0).setCellValue("Chi tiết phiếu kiểm mã " + selectedAdjustment.getSaCode());
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

                    Row headerRow = sheet.createRow(1);
                    headerRow.createCell(0).setCellValue("STT");
                    headerRow.createCell(1).setCellValue("ID");
                    headerRow.createCell(2).setCellValue("Sản phẩm");
                    headerRow.createCell(3).setCellValue("Lô");
                    headerRow.createCell(4).setCellValue("Đếm hệ thống");
                    headerRow.createCell(5).setCellValue("Đếm thực tế");
                    headerRow.createCell(6).setCellValue("Đếm chênh lệch");
                    headerRow.createCell(7).setCellValue("Ghi chú");

                    int stt = 1;
                    for (StockAdjustmentDetail d : list) {
                        Row dataRow = sheet.createRow(stt + 1);
                        dataRow.createCell(0).setCellValue(stt);
                        dataRow.createCell(1).setCellValue(d.getSadId());
                        dataRow.createCell(2).setCellValue(d.getProductName());
                        dataRow.createCell(3).setCellValue(d.getLotCode());
                        dataRow.createCell(4).setCellValue(d.getSystemQty());
                        dataRow.createCell(5).setCellValue(d.getCountedQty());
                        dataRow.createCell(6).setCellValue(d.getDiffQty());
                        dataRow.createCell(7).setCellValue(d.getNote());
                        stt++;
                    }

                    FileOutputStream out = new FileOutputStream(file);
                    workbook.write(out);
                    out.close();
                    workbook.close();

                    JOptionPane.showMessageDialog(this, "Xuất Excel thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Xuất Excel thất bại! " + ex.getMessage());
                }
            }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.add(btnExcelHeader);
        rightPanel.add(btnViewHeader);
        rightPanel.add(btnAddHeader);

        panel.add(rightPanel, BorderLayout.EAST);
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
                new Object[]{"ID", "Mã phiếu", "Trạng thái", "Tương tác"},
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3;
            }
        };

        tblAdjustment = new JTable(adjustmentModel);
        tblAdjustment.setRowHeight(36);
        tblAdjustment.setShowGrid(true);
        tblAdjustment.setGridColor(new Color(220, 220, 220));

        JTableHeader header = tblAdjustment.getTableHeader();
        header.setBackground(new Color(0, 123, 255));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        tblAdjustment.getColumnModel().getColumn(0).setCellRenderer(center);
        tblAdjustment.getColumnModel().getColumn(1).setCellRenderer(center);
        tblAdjustment.getColumnModel().getColumn(2).setCellRenderer(center);
        tblAdjustment.getColumnModel().getColumn(3).setCellRenderer(center);
        tblAdjustment.getColumnModel().getColumn(3).setCellRenderer(new ActionCellRenderer());
        tblAdjustment.getColumnModel().getColumn(3).setCellEditor(new ActionCellEditor());

        tblAdjustment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = tblAdjustment.getSelectedRow();
                    if (row >= 0) {
                        selectedAdjustment =
                                saService.getById((int) adjustmentModel.getValueAt(row, 0));
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
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblDetail = new JTable(detailModel);
        tblDetail.setRowHeight(32);
        tblDetail.setShowGrid(true);
        tblDetail.setGridColor(new Color(220, 220, 220));

        JTableHeader header = tblDetail.getTableHeader();
        header.setBackground(new Color(0, 123, 255));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

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

        btnAddDetail = new JButton("Thêm");
        btnEditDetail = new JButton("Sửa");
        btnDeleteDetail = new JButton("Xóa");

        btnAddDetail.setBackground(new Color(40, 167, 69));
        btnAddDetail.setForeground(Color.WHITE);
        btnEditDetail.setBackground(new Color(255, 193, 7));
        btnEditDetail.setForeground(Color.WHITE);
        btnDeleteDetail.setBackground(new Color(220, 53, 69));
        btnDeleteDetail.setForeground(Color.WHITE);

        btnAddDetail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEditDetail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDeleteDetail.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnAddDetail.addActionListener(e -> {
            if (!canAddDetail()) return;

            StockAdjustmentDetailDialog dialog =
                    new StockAdjustmentDetailDialog(
                            SwingUtilities.getWindowAncestor(this),
                            selectedAdjustment.getSaId()
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadDetails(selectedAdjustment);
                refreshDashboard();
            }
        });

        btnEditDetail.addActionListener(e -> {
            if (!canUpdateDetailAction()) return;

            int row = tblDetail.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa");
                return;
            }

            int sadId = (int) detailModel.getValueAt(row, 0);
            StockAdjustmentDetail detail = saService.getDetailById(sadId);

            StockAdjustmentDetailDialog dialog =
                    new StockAdjustmentDetailDialog(
                            SwingUtilities.getWindowAncestor(this),
                            detail
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadDetails(selectedAdjustment);
                refreshDashboard();
            }
        });

        btnDeleteDetail.addActionListener(e -> {
            if (!canDeleteDetailAction()) return;

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
                refreshDashboard();
            }
        });

        panel.add(btnAddDetail);
        panel.add(btnEditDetail);
        panel.add(btnDeleteDetail);

        return panel;
    }

    private boolean canAddDetail() {
        if (!canCreate) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền thêm chi tiết kiểm kho.");
            return false;
        }
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

    private boolean canUpdateDetailAction() {
        if (!canUpdate) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền sửa chi tiết kiểm kho.");
            return false;
        }
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

    private boolean canDeleteDetailAction() {
        if (!canDelete) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa chi tiết kiểm kho.");
            return false;
        }
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

    private void loadAdjustments(String searchTxt) {
        adjustmentModel.setRowCount(0);
        List<StockAdjustment> list = saService.getAll(searchTxt);

        for (StockAdjustment sa : list) {
            adjustmentModel.addRow(new Object[]{
                    sa.getSaId(),
                    sa.getSaCode(),
                    sa.getStatus(),
                    sa
            });
        }

        detailModel.setRowCount(0);
        selectedAdjustment = null;
    }

    private void loadDetails(StockAdjustment sa) {
        detailModel.setRowCount(0);
        List<StockAdjustmentDetail> list = saService.getByStockAdjustment(sa.getSaId());

        for (StockAdjustmentDetail d : list) {
            detailModel.addRow(new Object[]{
                    d.getSadId(),
                    d.getProductName(),
                    d.getLotCode(),
                    d.getSystemQty(),
                    d.getCountedQty(),
                    d.getDiffQty(),
                    d.getNote()
            });
        }
    }

    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        private final JButton editBtn = new JButton("Sửa");
        private final JButton deleteBtn = new JButton("Xóa");

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
            setOpaque(true);

            editBtn.setBackground(new Color(255, 193, 7));
            editBtn.setForeground(Color.WHITE);
            deleteBtn.setBackground(new Color(220, 53, 69));
            deleteBtn.setForeground(Color.WHITE);

            editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

            editBtn.setFocusable(false);
            deleteBtn.setFocusable(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            removeAll();

            StockAdjustment sa = (StockAdjustment) value;

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            setBorder(BorderFactory.createMatteBorder(
                    0, 0, 1, 1, table.getGridColor()
            ));

            if (sa.getStatus() == StockAdjustmentStatus.DRAFT) {
                if (canUpdate) add(editBtn);
                if (canDelete) add(deleteBtn);
            }

            return this;
        }
    }

    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        private final JButton editBtn = new JButton("Sửa");
        private final JButton deleteBtn = new JButton("Xóa");
        private StockAdjustment currentSA;

        public ActionCellEditor() {
            panel.setOpaque(true);

            editBtn.setBackground(new Color(224, 168, 0));
            editBtn.setForeground(new Color(33, 37, 41));
            deleteBtn.setBackground(new Color(176, 42, 55));
            deleteBtn.setForeground(Color.WHITE);

            editBtn.setFocusable(false);
            deleteBtn.setFocusable(false);

            editBtn.addActionListener(e -> {
                fireEditingStopped();

                if (!canUpdate) {
                    JOptionPane.showMessageDialog(panel, "Bạn không có quyền sửa phiếu kiểm kho.");
                    return;
                }

                if (currentSA == null || currentSA.getStatus() != StockAdjustmentStatus.DRAFT) {
                    JOptionPane.showMessageDialog(panel, "Chỉ được sửa phiếu ở trạng thái DRAFT.");
                    return;
                }

                openEditDialog();
            });

            deleteBtn.addActionListener(e -> {
                fireEditingStopped();

                if (!canDelete) {
                    JOptionPane.showMessageDialog(panel, "Bạn không có quyền xóa phiếu kiểm kho.");
                    return;
                }

                if (currentSA == null || currentSA.getStatus() != StockAdjustmentStatus.DRAFT) {
                    JOptionPane.showMessageDialog(panel, "Chỉ được xóa phiếu ở trạng thái DRAFT.");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Bạn có chắc muốn xóa dòng này?",
                        "Xác nhận",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    saService.delete(currentSA.getSaId());
                    loadAdjustments(!txtSearch.getText().isEmpty() ? txtSearch.getText() : "");
                    tblAdjustment.revalidate();
                    tblAdjustment.repaint();
                    refreshDashboard();
                }
            });
        }

        private void openEditDialog() {
            StockAdjustment freshSA = saService.getById(currentSA.getSaId());

            StockAdjustmentDialog dialog =
                    new StockAdjustmentDialog(
                            SwingUtilities.getWindowAncestor(panel),
                            currentUser,
                            freshSA
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadAdjustments(!txtSearch.getText().isEmpty() ? txtSearch.getText() : "");
                tblAdjustment.revalidate();
                tblAdjustment.repaint();
                refreshDashboard();
            }
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {

            currentSA = (StockAdjustment) value;
            panel.removeAll();

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }

            panel.setBorder(BorderFactory.createMatteBorder(
                    0, 0, 1, 1, table.getGridColor()
            ));

            if (currentSA.getStatus() == StockAdjustmentStatus.DRAFT) {
                if (canUpdate) panel.add(editBtn);
                if (canDelete) panel.add(deleteBtn);
            }

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentSA;
        }
    }
}