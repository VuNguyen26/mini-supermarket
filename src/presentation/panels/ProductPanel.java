package presentation.panels;

import bus.CategoryService;
import bus.ProductService;
import dto.Category;
import dto.Product;
import presentation.dialogs.ProductDialog;
import util.DialogUtils;
import util.MoneyUtils;
import util.RolePermission;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductPanel - Quản lý sản phẩm (UI giống ảnh mẫu)
 */
public class ProductPanel extends JPanel {
    
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    
    private JTextField txtSearch;
    private JComboBox<String> cboCategory;
    private JComboBox<String> cboStatus;
    private JTable table;
    private ProductTableModel tableModel;
    private JButton btnCreate, btnRefresh;
    
    private List<Product> productList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    public ProductPanel() {
        initComponents();
        loadCategories();
        loadData();
        applyPermissions();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Top Panel - Search and Filter
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        // Combined controls row (search + filters on one line)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        controlsPanel.setBackground(Color.WHITE);

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 35));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã sản phẩm...");
        txtSearch.addActionListener(e -> searchProducts());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void onChange() { searchProducts(); }
            @Override public void insertUpdate(DocumentEvent e) { onChange(); }
            @Override public void removeUpdate(DocumentEvent e) { onChange(); }
            @Override public void changedUpdate(DocumentEvent e) { onChange(); }
        });

        JLabel lblCategory = new JLabel("Danh mục:");
        lblCategory.setPreferredSize(new Dimension(70, 35));
        cboCategory = new JComboBox<>();
        cboCategory.setPreferredSize(new Dimension(150, 35));
        cboCategory.addActionListener(e -> filterProducts());

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setPreferredSize(new Dimension(70, 35));
        cboStatus = new JComboBox<>(new String[]{"Tất cả", "Còn hàng", "Hết hàng"});
        cboStatus.setPreferredSize(new Dimension(130, 35));
        cboStatus.addActionListener(e -> filterProducts());

        controlsPanel.add(txtSearch);
        controlsPanel.add(lblCategory);
        controlsPanel.add(cboCategory);
        controlsPanel.add(lblStatus);
        controlsPanel.add(cboStatus);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnCreate = createStyledButton("+ Tạo sản phẩm", new Color(33, 150, 243), Color.WHITE);
        btnCreate.setPreferredSize(new Dimension(150, 40));
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreate.addActionListener(e -> createProduct());

        btnRefresh = createStyledButton("Làm mới", new Color(76, 175, 80), Color.WHITE);
        btnRefresh.addActionListener(e -> loadData());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnCreate);

        topPanel.add(controlsPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table Panel
        tableModel = new ProductTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(33, 150, 243));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));

        // Center align number columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // STT
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Tồn kho
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer); // Trạng thái
        table.getColumnModel().getColumn(9).setCellRenderer(centerRenderer); // Thao tác

        // Right align money columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Giá nhập
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); // Giá bán

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Mã SP
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Đơn vị
        table.getColumnModel().getColumn(3).setPreferredWidth(200); // Tên
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Danh mục
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Giá nhập
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Giá bán
        table.getColumnModel().getColumn(7).setPreferredWidth(80);  // Tồn kho
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Trạng thái
        table.getColumnModel().getColumn(9).setPreferredWidth(170); // Thao tác (đủ chỗ cho 2 nút)

        // Double click to edit or click on action column
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                
                if (row >= 0) {
                    if (evt.getClickCount() == 2) {
                        // Double click anywhere to edit
                        editProduct();
                    }
                }
            }
        });

        // Action column uses buttons
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel - Info
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        JLabel lblInfo = new JLabel("Tổng số sản phẩm: 0");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomPanel.add(lblInfo);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setPreferredSize(new Dimension(100, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private ImageIcon loadIconAny(int size, String... names) {
        for (String name : names) {
            try {
                ImageIcon raw = new ImageIcon("resources/images/" + name);
                if (raw.getIconWidth() > 0) {
                    Image img = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                }
            } catch (Exception ignore) {}
        }
        return null;
    }

    private void loadCategories() {
        try {
            categoryList = categoryService.getAll();
            cboCategory.removeAllItems();
            cboCategory.addItem("Tất cả");
            for (Category c : categoryList) {
                cboCategory.addItem(c.getCategoryName());
            }
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tải danh mục: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            productList = productService.getAll();
            tableModel.setProducts(productList);
            updateInfo();
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void searchProducts() {
        String keyword = txtSearch.getText().trim();
        try {
            if (keyword.isEmpty()) {
                loadData();
            } else {
                productList = productService.search(keyword);
                tableModel.setProducts(productList);
                updateInfo();
            }
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void filterProducts() {
        try {
            String categoryName = (String) cboCategory.getSelectedItem();
            String stockOption = (String) cboStatus.getSelectedItem();

            Integer categoryId = null;
            if (categoryName != null && !categoryName.equals("Tất cả")) {
                for (Category c : categoryList) {
                    if (c.getCategoryName().equals(categoryName)) {
                        categoryId = c.getCategoryId();
                        break;
                    }
                }
            }

            // Query by category only; apply stock filter in-memory for clarity
            productList = productService.filter(categoryId, null, null);

            if ("Còn hàng".equals(stockOption)) {
                productList = productList.stream()
                        .filter(p -> p.getStockQty() > 0)
                        .collect(Collectors.toList());
            } else if ("Hết hàng".equals(stockOption)) {
                productList = productList.stream()
                        .filter(p -> p.getStockQty() <= 0)
                        .collect(Collectors.toList());
            }
            tableModel.setProducts(productList);
            updateInfo();
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi lọc dữ liệu: " + e.getMessage());
        }
    }

    private void createProduct() {
        ProductDialog dialog = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void editProduct() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            Product product = productList.get(row);
            ProductDialog dialog = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), product);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadData();
            }
        }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            Product product = productList.get(row);
            if (DialogUtils.confirm(this, "Xác nhận xóa sản phẩm: " + product.getProductName() + "?")) {
                try {
                    productService.delete(product.getProductId());
                    DialogUtils.showInfo(this, "Đã xóa sản phẩm");
                    loadData();
                } catch (Exception e) {
                    DialogUtils.showError(this, "Lỗi xóa: " + e.getMessage());
                }
            }
        }
    }
    /*
    // Old deleteProduct - now active
        int row = table.getSelectedRow();
        if (row >= 0) {
            Product product = productList.get(row);
            if (DialogUtils.confirm(this, "Xác nhận xóa sản phẩm: " + product.getProductName() + "?")) {
                try {
                    productService.delete(product.getProductId());
                    DialogUtils.showInfo(this, "Đã xóa sản phẩm");
                    loadData();
                } catch (Exception e) {
                    DialogUtils.showError(this, "Lỗi xóa: " + e.getMessage());
                }
            }
        }
    }
    */

    private void updateInfo() {
        JLabel lblInfo = (JLabel) ((JPanel) getComponent(2)).getComponent(0);
        lblInfo.setText("Tổng số sản phẩm: " + productList.size());
    }

    private void applyPermissions() {
        boolean canCreate = RolePermission.has("PRODUCT_CREATE");
        // canUpdate và canDelete có thể được sử dụng sau nếu cần

        btnCreate.setEnabled(canCreate);
    }

    // Table Model
    class ProductTableModel extends AbstractTableModel {
        private final String[] columns = {
            "STT", "Mã Sản Phẩm", "Đơn vị", "Tên Sản Phẩm", "Danh mục",
            "Nhập (VND)", "Bán (VND)", "Tồn Kho", "Trạng Thái", "Thao Tác"
        };
        private List<Product> products = new ArrayList<>();

        public void setProducts(List<Product> products) {
            this.products = products;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return products.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Product p = products.get(row);
            switch (col) {
                case 0: return row + 1;
                case 1: return p.getBarcode();
                case 2: return p.getUnit();
                case 3: return p.getProductName();
                case 4: return p.getCategoryName();
                case 5: return MoneyUtils.format(p.getImportPrice());
                case 6: return MoneyUtils.format(p.getSalePrice());
                case 7: return p.getStockQty();
                case 8: {
                    int stock = p.getStockQty();
                    if (stock == 0) return "Hết hàng";
                    else if (stock < 20) return "Sắp hết hàng";
                    else return "Còn";
                }
                case 9: return "Sửa | Xóa";
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 9; // Chỉ cột thao tác
        }
    }

    // Renderer/editor classes for action buttons (outer class scope)
    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnEdit = createStyledButton("Sửa", new Color(33, 150, 243), Color.WHITE);
        private final JButton btnDelete = createStyledButton("Xóa", new Color(244, 67, 54), Color.WHITE);

        public ActionCellRenderer() {
            setOpaque(true);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            Dimension d = new Dimension(45, 24);
            btnEdit.setPreferredSize(d);
            btnDelete.setPreferredSize(d);
            btnEdit.setFocusable(false);
            btnDelete.setFocusable(false);
            add(btnEdit);
            add(Box.createRigidArea(new Dimension(6, 0)));
            add(btnDelete);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel();
        private final JButton btnEdit = createStyledButton("Sửa", new Color(33, 150, 243), Color.WHITE);
        private final JButton btnDelete = createStyledButton("Xóa", new Color(244, 67, 54), Color.WHITE);
        private int editingRow = -1;

        public ActionCellEditor() {
            panel.setOpaque(true);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            Dimension d = new Dimension(45, 24);
            btnEdit.setPreferredSize(d);
            btnDelete.setPreferredSize(d);
            panel.add(btnEdit);
            panel.add(Box.createRigidArea(new Dimension(6, 0)));
            panel.add(btnDelete);

            btnEdit.addActionListener(e -> {
                if (editingRow >= 0) {
                    table.setRowSelectionInterval(editingRow, editingRow);
                    editProduct();
                }
                fireEditingStopped();
            });

            btnDelete.addActionListener(e -> {
                if (editingRow >= 0) {
                    table.setRowSelectionInterval(editingRow, editingRow);
                    deleteProduct();
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return null; }
    }
}
