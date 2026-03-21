package presentation.panels;

import bus.AuthService.AuthUser;
import bus.PromotionService;
import dto.Promotion;
import dto.PromotionProduct;
import presentation.dialogs.PromotionDialog;
import presentation.dialogs.PromotionProductDialog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import util.RolePermission;

public class PromotionPanel extends JPanel {

    private final AuthUser currentUser;
    private final PromotionService promoService = new PromotionService();

    private JTable tblPromotion;
    private JTable tblPromotionProduct;
    private JTextField txtSearch;

    private DefaultTableModel promotionModel;
    private DefaultTableModel ppModel;

    private Promotion selectedPromotion;

    private boolean canView;
    private boolean canCreate;
    private boolean canUpdate;
    private boolean canDelete;

    private JButton btnAddPromotion;
    private JButton btnViewPromotion;
    private JButton btnExportExcel;

    private JButton btnAddPromotionProduct;
    private JButton btnEditPromotionProduct;
    private JButton btnDeletePromotionProduct;


    private void initPermissions() {
        canView = RolePermission.has("PROMOTION_VIEW");
        canCreate = RolePermission.has("PROMOTION_CREATE");
        canUpdate = RolePermission.has("PROMOTION_UPDATE");
        canDelete = RolePermission.has("PROMOTION_DELETE");
    }

    public PromotionPanel(AuthUser currentUser) {
        this.currentUser = currentUser;
        initPermissions();
        initUI();
        applyPermissions();
        loadPromotions(!txtSearch.getText().isEmpty() ? txtSearch.getText() : "");
    }
    private void applyPermissions() {
        if (btnAddPromotion != null) btnAddPromotion.setEnabled(canCreate);
        if (btnViewPromotion != null) btnViewPromotion.setEnabled(canView);
        if (btnExportExcel != null) btnExportExcel.setEnabled(canView);

        if (btnAddPromotionProduct != null) btnAddPromotionProduct.setEnabled(canCreate);
        if (btnEditPromotionProduct != null) btnEditPromotionProduct.setEnabled(canUpdate);
        if (btnDeletePromotionProduct != null) btnDeletePromotionProduct.setEnabled(canDelete);
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
        panel.setPreferredSize(new Dimension(0, 40));

        JLabel title = new JLabel("Danh sách khuyến mãi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setMinimumSize(new Dimension(150, 20));
        txtSearch.setPreferredSize(new Dimension(200, 28));
        txtSearch.setMaximumSize(new Dimension(270, 30));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        txtSearch.addActionListener(e -> {
            if (!canView) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền xem khuyến mãi");
                return;
            }
            String keyword = txtSearch.getText().trim();
            loadPromotions(keyword);
        });

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.add(title);
        leftPanel.add(txtSearch);

        panel.add(leftPanel, BorderLayout.WEST);

        // dùng field thay vì biến local
        btnAddPromotion = new JButton("+ Thêm khuyến mãi");
        btnViewPromotion = new JButton("Xem chi tiết");
        btnExportExcel = new JButton("Xuất Excel");

        btnAddPromotion.setPreferredSize(new Dimension(150, 30));
        btnViewPromotion.setPreferredSize(new Dimension(110, 30));
        btnExportExcel.setPreferredSize(new Dimension(100, 30));

        btnAddPromotion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnViewPromotion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportExcel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnAddPromotion.setBackground(new Color(40, 167, 69));
        btnAddPromotion.setForeground(Color.WHITE);
        btnViewPromotion.setBackground(new Color(0, 123, 255));
        btnViewPromotion.setForeground(Color.WHITE);
        btnExportExcel.setBackground(new Color(29, 111, 66));
        btnExportExcel.setForeground(Color.WHITE);

        btnAddPromotion.setEnabled(canCreate);
        btnViewPromotion.setEnabled(canView);
        btnExportExcel.setEnabled(canView);

        btnAddPromotion.addActionListener(e -> {
            if (!canCreate) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền thêm khuyến mãi");
                return;
            }

            PromotionDialog dialog =
                    new PromotionDialog(
                            SwingUtilities.getWindowAncestor(this),
                            currentUser
                    );
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                txtSearch.setText("");
                loadPromotions(txtSearch.getText());
            }
        });

        btnViewPromotion.addActionListener(e -> {
            if (!canView) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền xem khuyến mãi");
                return;
            }

            if (selectedPromotion == null) {
                JOptionPane.showMessageDialog(this, "Chưa chọn chương trình khuyến mãi");
                return;
            }

            PromotionDialog dialog =
                    new PromotionDialog(
                            SwingUtilities.getWindowAncestor(this),
                            selectedPromotion
                    );
            dialog.setVisible(true);
        });

        btnExportExcel.addActionListener(e -> {
            if (!canView) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền xuất Excel khuyến mãi");
                return;
            }

            List<Promotion> list = new ArrayList<>();

            TableModel model = tblPromotion.getModel();
            int rowCount = model.getRowCount();

            if (rowCount <= 0) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất Excel!");
                return;
            }

            for (int i = 0; i < rowCount; i++) {
                Object value = model.getValueAt(i, 0);
                if (value != null) {
                    list.add(promoService.getById((int) value));
                }
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Lưu tại");
            chooser.setSelectedFile(new File("Promotion.xlsx"));
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
                    Sheet sheet = workbook.createSheet("Chương trình khuyến mãi");

                    Row titleRow = sheet.createRow(0);
                    titleRow.createCell(0).setCellValue("Danh sách chương trình khuyến mãi");
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

                    Row headerRow = sheet.createRow(1);
                    headerRow.createCell(0).setCellValue("STT");
                    headerRow.createCell(1).setCellValue("ID");
                    headerRow.createCell(2).setCellValue("Mã");
                    headerRow.createCell(3).setCellValue("Tên KM");
                    headerRow.createCell(4).setCellValue("Loại");
                    headerRow.createCell(5).setCellValue("Giá trị");
                    headerRow.createCell(6).setCellValue("Đơn tối thiểu");
                    headerRow.createCell(7).setCellValue("Bắt đầu");
                    headerRow.createCell(8).setCellValue("Kết thúc");
                    headerRow.createCell(9).setCellValue("Trạng thái");
                    headerRow.createCell(10).setCellValue("Ngày tạo");

                    int stt = 1;
                    for (Promotion p : list) {
                        Row dataRow = sheet.createRow(stt + 1);
                        dataRow.createCell(0).setCellValue(stt);
                        dataRow.createCell(1).setCellValue(p.getPromoId());
                        dataRow.createCell(2).setCellValue(p.getPromoCode());
                        dataRow.createCell(3).setCellValue(p.getPromoName());
                        dataRow.createCell(4).setCellValue(p.getType().name());
                        dataRow.createCell(5).setCellValue(p.getValue().doubleValue());
                        dataRow.createCell(6).setCellValue(p.getMinOrderAmount().doubleValue());
                        dataRow.createCell(7).setCellValue(p.getStartAt().toString().replace('T', ' '));
                        dataRow.createCell(8).setCellValue(p.getEndAt().toString().replace('T', ' '));
                        dataRow.createCell(9).setCellValue(p.getStatus());
                        dataRow.createCell(10).setCellValue(p.getCreatedAt().toString().replace('T', ' '));
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
        rightPanel.add(btnExportExcel);
        rightPanel.add(btnViewPromotion);
        rightPanel.add(btnAddPromotion);

        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    /* ================= MAIN ================= */

    private JComponent buildMainContent() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildPromotionTable(),
                buildPromotionProductSection()
        );
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0.5);
        return splitPane;
    }

    /* ================= TABLE: PROMOTION ================= */

    private JComponent buildPromotionTable() {
        promotionModel = new DefaultTableModel(
                new Object[]{"ID", "Mã KM", "Tên", "Trạng thái", "Tương tác"},
                0
        ) {
            public boolean isCellEditable(int r, int c) {
                return c == 4;
            }
        };

        tblPromotion = new JTable(promotionModel);
        tblPromotion.setRowHeight(36);
        tblPromotion.setShowGrid(true);
        tblPromotion.setGridColor(new Color(220, 220, 220));
        JTableHeader header = tblPromotion.getTableHeader();
        header.setBackground(new Color(0, 123, 255));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));


        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        tblPromotion.getColumnModel().getColumn(0).setCellRenderer(center); 
        tblPromotion.getColumnModel().getColumn(1).setCellRenderer(center);
        tblPromotion.getColumnModel().getColumn(2).setCellRenderer(center); 
        tblPromotion.getColumnModel().getColumn(3).setCellRenderer(center); 
        tblPromotion.getColumnModel().getColumn(4).setCellRenderer(center);
        tblPromotion.getColumnModel().getColumn(4).setCellRenderer(new ActionCellRenderer());
        tblPromotion.getColumnModel().getColumn(4).setCellEditor(new ActionCellEditor());


        // Click → load danh sách sản phẩm của chương trình khuyến mãi
        tblPromotion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = tblPromotion.getSelectedRow();
                    if (row >= 0) {
                        selectedPromotion =
                                promoService.getById(
                                        (int) promotionModel.getValueAt(row, 0)
                                );
                        loadPromotionProduct(selectedPromotion);
                    }
                }
            }
        });

        return new JScrollPane(tblPromotion);
    }

    /* ================= PRODUCT SECTION ================= */

    private JComponent buildPromotionProductSection() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JLabel title = new JLabel("Danh sách sản phẩm của chương trình khuyến mãi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        ppModel = new DefaultTableModel(
                new Object[]{"ID", "ID sản phẩm", "Tên sản phẩm"},
                0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblPromotionProduct = new JTable(ppModel);
        tblPromotionProduct.setRowHeight(32);
        tblPromotionProduct.setShowGrid(true);
        tblPromotionProduct.setGridColor(new Color(220, 220, 220));
        JTableHeader header = tblPromotionProduct.getTableHeader();
        header.setBackground(new Color(0, 123, 255));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));


        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        tblPromotionProduct.getColumnModel().getColumn(0).setCellRenderer(center); 
        tblPromotionProduct.getColumnModel().getColumn(1).setCellRenderer(center);
        tblPromotionProduct.getColumnModel().getColumn(2).setCellRenderer(left); 

        panel.add(new JScrollPane(tblPromotionProduct), BorderLayout.CENTER);
        panel.add(buildProductsButtons(), BorderLayout.SOUTH);

        return panel;
    }

    /* ================= DETAIL BUTTONS ================= */

    private JComponent buildProductsButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnAddPromotionProduct = new JButton("Thêm");
        btnEditPromotionProduct = new JButton("Sửa");
        btnDeletePromotionProduct = new JButton("Xóa");

        btnAddPromotionProduct.setBackground(new Color(40, 167, 69));
        btnAddPromotionProduct.setForeground(Color.WHITE);
        btnEditPromotionProduct.setBackground(new Color(255, 193, 7));
        btnEditPromotionProduct.setForeground(Color.WHITE);
        btnDeletePromotionProduct.setBackground(new Color(220, 53, 69));
        btnDeletePromotionProduct.setForeground(Color.WHITE);

        btnAddPromotionProduct.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEditPromotionProduct.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDeletePromotionProduct.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnAddPromotionProduct.setEnabled(canCreate);
        btnEditPromotionProduct.setEnabled(canUpdate);
        btnDeletePromotionProduct.setEnabled(canDelete);

        btnAddPromotionProduct.addActionListener(e -> {
            if (!canCreateProduct()) return;

            PromotionProductDialog dialog =
                    new PromotionProductDialog(
                            SwingUtilities.getWindowAncestor(this),
                            selectedPromotion
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadPromotionProduct(selectedPromotion);
            }
        });

        btnEditPromotionProduct.addActionListener(e -> {
            if (!canUpdateProduct()) return;

            int row = tblPromotionProduct.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa");
                return;
            }

            int ppId = (int) ppModel.getValueAt(row, 0);

            PromotionProduct promotionProduct =
                    promoService.getPPById(ppId);

            PromotionProductDialog dialog =
                    new PromotionProductDialog(
                            SwingUtilities.getWindowAncestor(this),
                            selectedPromotion,
                            promotionProduct
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadPromotionProduct(selectedPromotion);
            }
        });

        btnDeletePromotionProduct.addActionListener(e -> {
            if (!canDeleteProduct()) return;

            int row = tblPromotionProduct.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng để xóa");
                return;
            }

            int ppId = (int) ppModel.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa dòng này?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                promoService.deleteProduct(ppId);
                loadPromotionProduct(selectedPromotion);
            }
        });

        panel.add(btnAddPromotionProduct);
        panel.add(btnEditPromotionProduct);
        panel.add(btnDeletePromotionProduct);
        return panel;
    }

    private boolean ensurePromotionSelected() {
        if (selectedPromotion == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn chương trình khuyến mãi");
            return false;
        }
        return true;
    }

    private boolean canCreateProduct() {
        if (!canCreate) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền thêm sản phẩm vào khuyến mãi");
            return false;
        }
        return ensurePromotionSelected();
    }

    private boolean canUpdateProduct() {
        if (!canUpdate) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền sửa sản phẩm trong khuyến mãi");
            return false;
        }
        return ensurePromotionSelected();
    }

    private boolean canDeleteProduct() {
        if (!canDelete) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa sản phẩm khỏi khuyến mãi");
            return false;
        }
        return ensurePromotionSelected();
    }

    /* ================= LOAD DATA ================= */

    private void loadPromotions(String searchTxt) {
        promotionModel.setRowCount(0);
        List<Promotion> list = promoService.getAll(searchTxt);
        for (Promotion promo : list) {
            promotionModel.addRow(new Object[]{
                    promo.getPromoId(),
                    promo.getPromoCode(),
                    promo.getPromoName(),
                    promo.getStatus(),
                    promo
            });
        }
        ppModel.setRowCount(0);
        selectedPromotion = null;
    }

    private void loadPromotionProduct(Promotion promo) {
        ppModel.setRowCount(0);
        List<PromotionProduct> list =
                promoService.getByPromotionId(promo.getPromoId());

        for (PromotionProduct d : list) {
            ppModel.addRow(new Object[]{
                    d.getPpId(),
                    d.getProductId(),
                    d.getProducName(),
            });
        }
    }

    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        private final JButton editBtn = new JButton("Sửa");
        private final JButton deleteBtn = new JButton("Xóa");
        private final Dimension btnSize = new Dimension(55, 25);

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
            setOpaque(true);
            editBtn.setBackground(new Color(255, 193, 7));
            editBtn.setForeground(Color.WHITE);
            deleteBtn.setBackground(new Color(220, 53, 69));
            deleteBtn.setForeground(Color.WHITE);
            editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            editBtn.setFocusable(false);
            deleteBtn.setFocusable(false);

            editBtn.setPreferredSize(btnSize);
            deleteBtn.setPreferredSize(btnSize);

            add(editBtn);
            add(deleteBtn);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            removeAll();
            add(editBtn);
            add(deleteBtn);

            editBtn.setEnabled(canUpdate);
            deleteBtn.setEnabled(canDelete);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            setBorder(BorderFactory.createMatteBorder(
                    0, 0, 1, 1, table.getGridColor()
            ));

            return this;
        }
    }


    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        private final JButton editBtn = new JButton("Sửa");
        private final JButton deleteBtn = new JButton("Xóa");
        private final Dimension btnSize = new Dimension(55, 25);
        private Promotion currentPromo;

        public ActionCellEditor() {
            panel.setOpaque(true);

            editBtn.setFocusable(false);
            deleteBtn.setFocusable(false);
            
            editBtn.setBackground(new Color(224, 168, 0));
            editBtn.setForeground(new Color(33, 37, 41));
            deleteBtn.setBackground(new Color(176, 42, 55));
            deleteBtn.setForeground(Color.WHITE);
            editBtn.setPreferredSize(btnSize);
            deleteBtn.setPreferredSize(btnSize);

            editBtn.addActionListener(e -> {
                fireEditingStopped();

                if (!canUpdate) {
                    JOptionPane.showMessageDialog(panel, "Bạn không có quyền sửa khuyến mãi");
                    return;
                }

                openEditDialog();
            });

            deleteBtn.addActionListener(e -> {
                fireEditingStopped();

                if (!canDelete) {
                    JOptionPane.showMessageDialog(panel, "Bạn không có quyền xóa khuyến mãi");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Bạn có chắc muốn xóa dòng này?",
                        "Xác nhận",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    promoService.delete(currentPromo.getPromoId());
                    loadPromotions(!txtSearch.getText().isEmpty() ? txtSearch.getText() : "");
                    tblPromotion.revalidate();
                    tblPromotion.repaint();
                }
            });

            panel.add(editBtn);
            panel.add(deleteBtn);
        }

        private void openEditDialog() {

            // 🔥 LOAD LẠI DATA MỚI NHẤT TỪ DB
            Promotion freshPromo =
                    promoService.getById(currentPromo.getPromoId());

            PromotionDialog dialog =
                    new PromotionDialog(
                            SwingUtilities.getWindowAncestor(panel),
                            currentUser,
                            freshPromo
                    );

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadPromotions(!txtSearch.getText().isEmpty() ? txtSearch.getText() : "");
                tblPromotion.revalidate();
                tblPromotion.repaint();
            }
        }

        @Override
        public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected,
            int row, int column) {
            
            currentPromo = (Promotion) value;
            panel.removeAll();
            panel.add(editBtn);
            panel.add(deleteBtn);

            // ===== Đồng bộ màu =====
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }

            panel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 1, table.getGridColor()
            ));

            editBtn.setEnabled(canUpdate);
            deleteBtn.setEnabled(canDelete);

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentPromo;
        }
    }

}
