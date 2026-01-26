package presentation.panels;

import bus.AuthService.AuthUser;
import bus.PromotionService;
import dto.Promotion;
import dto.PromotionProduct;
import dto.StockAdjustment;
import presentation.dialogs.PromotionDialog;
import presentation.dialogs.PromotionProductDialog;
import presentation.dialogs.StockAdjustmentDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PromotionPanel extends JPanel {

    private final AuthUser currentUser;
    private final PromotionService promoService = new PromotionService();

    private JTable tblPromotion;
    private JTable tblPromotionProduct;

    private DefaultTableModel promotionModel;
    private DefaultTableModel ppModel;

    private Promotion selectedPromotion;

    public PromotionPanel(AuthUser currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadPromotions();
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

        JLabel title = new JLabel("Danh sách chương trình khuyến mãi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(title, BorderLayout.WEST);

        JButton btnAdd = new JButton("+ Thêm khuyến mãi");
        JButton btnView = new JButton("Xem chi tiết");

        btnAdd.addActionListener(e -> {
            PromotionDialog dialog =
                    new PromotionDialog(
                            SwingUtilities.getWindowAncestor(this),
                            currentUser
                    );
            dialog.setVisible(true);
            if(dialog.isSaved()){
                loadPromotions();
            }
        });

        btnView.addActionListener(e -> {
            if (selectedPromotion == null){
                JOptionPane.showMessageDialog(this, "Chưa chọn chương trình khuyến mãi");
                return;
            }
            PromotionDialog dialog = new PromotionDialog(SwingUtilities.getWindowAncestor(this), selectedPromotion);
            dialog.setVisible(true);

        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.add(btnView);
        rightPanel.add(btnAdd);

        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }


    /* ================= MAIN ================= */

    private JComponent buildMainContent() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildPromotionTable(),
                buildDetailSection()
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
        tblPromotion.setRowHeight(44);
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

    private JComponent buildDetailSection() {
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
        tblPromotionProduct.setRowHeight(38);
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
            if (!canEditProduct()) return;

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

        // // ===== SỬA =====
        btnEdit.addActionListener(e -> {
            if (!canEditProduct()) return;

            int row = tblPromotionProduct.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa");
                return;
            }

            int ppId = (int) ppModel.getValueAt(row, 0);

            PromotionProduct promotionProduct =
                    promoService.getPPById(ppId); // 🔥 QUAN TRỌNG

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

        // // ===== XÓA =====
        btnDelete.addActionListener(e -> {
            if (!canEditProduct()) return;

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

        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnDelete);
        return panel;
    }

    private boolean canEditProduct() {
        if (selectedPromotion == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn chương trình khuyến mãi");
            return false;
        }
        return true;
    }

    /* ================= LOAD DATA ================= */

    private void loadPromotions() {
        promotionModel.setRowCount(0);
        List<Promotion> list = promoService.getAll();
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

            // ===== Đồng bộ màu với JTable =====
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

            editBtn.setPreferredSize(btnSize);
            deleteBtn.setPreferredSize(btnSize);

            editBtn.addActionListener(e -> {
                fireEditingStopped();
                openEditDialog();
            });

            deleteBtn.addActionListener(e -> {
                fireEditingStopped();

                int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "Bạn có chắc muốn xóa dòng này?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
                );

                if(confirm == JOptionPane.YES_OPTION){
                    promoService.delete(currentPromo.getPromoId());
                    loadPromotions();
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
                loadPromotions();
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

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentPromo;
        }
    }

}
