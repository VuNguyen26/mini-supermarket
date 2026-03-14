package presentation.panels;

import bus.CategoryService;
import bus.ProductService;
import dal.dao.ProductImageDAO;
import dto.Category;
import dto.Product;
import dto.ProductImage;
import presentation.dialogs.ProductDialog;
import util.DialogUtils;
import util.MoneyUtils;
import util.RolePermission;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductPanel - Quản lý sản phẩm
 */
public class ProductPanel extends JPanel {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();

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

    private void refreshDashboard() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof presentation.MainFrame) {
            ((presentation.MainFrame) owner).refreshDashboard();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(0, 40));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã sản phẩm...");
        txtSearch.addActionListener(e -> searchProducts());

        JButton btnSearch = createStyledButton("Tìm kiếm", new Color(33, 150, 243), Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(0, 40));
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.addActionListener(e -> searchProducts());

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);

        JLabel lblCategory = new JLabel("Danh mục:");
        lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        cboCategory = new JComboBox<>();
        cboCategory.setPreferredSize(new Dimension(140, 40));
        cboCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboCategory.addActionListener(e -> filterProducts());

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        cboStatus = new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE"});
        cboStatus.setPreferredSize(new Dimension(140, 40));
        cboStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboStatus.addActionListener(e -> filterProducts());

        filterPanel.add(lblCategory);
        filterPanel.add(cboCategory);
        filterPanel.add(lblStatus);
        filterPanel.add(cboStatus);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnCreate = createStyledButton("+ Tạo sản phẩm", new Color(33, 150, 243), Color.WHITE);
        btnCreate.setPreferredSize(new Dimension(160, 40));
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreate.addActionListener(e -> createProduct());

        btnRefresh = createStyledButton("Làm mới", new Color(76, 175, 80), Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.addActionListener(e -> loadData());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnCreate);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new ProductTableModel();
        table = new JTable(tableModel);

        table.setRowHeight(80);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(33, 150, 243));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(1, 1));

        // Wrap text renderer
        DefaultTableCellRenderer wrapRenderer = new DefaultTableCellRenderer() {
            private final JTextArea textArea = new JTextArea();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(true);
                textArea.setFont(table.getFont());
                textArea.setText(value == null ? "" : value.toString());
                textArea.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

                if (isSelected) {
                    textArea.setBackground(table.getSelectionBackground());
                    textArea.setForeground(table.getSelectionForeground());
                } else {
                    textArea.setBackground(Color.WHITE);
                    textArea.setForeground(Color.BLACK);
                }
                return textArea;
            }
        };

        // Renderers
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageCellRenderer()); // Ảnh
        table.getColumnModel().getColumn(4).setCellRenderer(wrapRenderer); // Tên SP
        table.getColumnModel().getColumn(5).setCellRenderer(wrapRenderer); // Danh mục

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // STT
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Đơn vị
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer); // Tồn kho
        table.getColumnModel().getColumn(9).setCellRenderer(centerRenderer); // Trạng thái

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); // Giá nhập
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer); // Giá bán

        // Widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(100);   // Ảnh
        table.getColumnModel().getColumn(2).setPreferredWidth(130);  // Mã SP
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Đơn vị
        table.getColumnModel().getColumn(4).setPreferredWidth(220);  // Tên
        table.getColumnModel().getColumn(5).setPreferredWidth(150);  // Danh mục
        table.getColumnModel().getColumn(6).setPreferredWidth(100);  // Giá nhập
        table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Giá bán
        table.getColumnModel().getColumn(8).setPreferredWidth(80);   // Tồn kho
        table.getColumnModel().getColumn(9).setPreferredWidth(100);  // Trạng thái
        table.getColumnModel().getColumn(10).setPreferredWidth(170); // Thao tác

        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0 && evt.getClickCount() == 2) {
                    table.setRowSelectionInterval(row, row);
                    editProduct();
                }
            }
        });

        table.getColumnModel().getColumn(10).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(10).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        add(scrollPane, BorderLayout.CENTER);

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

    private ImageIcon loadProductThumbnail(Product product, int size) {
        try {
            ProductImage primaryImage = productImageDAO.findPrimaryImage(product.getProductId());
            if (primaryImage == null || primaryImage.getImageUrl() == null || primaryImage.getImageUrl().trim().isEmpty()) {
                return null;
            }

            String imagePath = primaryImage.getImageUrl().trim();
            BufferedImage bufferedImage;

            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                bufferedImage = ImageIO.read(new URL(imagePath));
            } else {
                File file = new File(imagePath);
                if (!file.exists()) {
                    return null;
                }
                bufferedImage = ImageIO.read(file);
            }

            if (bufferedImage == null) {
                return null;
            }

            Image scaled = bufferedImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);

        } catch (Exception e) {
            System.err.println("loadProductThumbnail error: " + e.getMessage());
            return null;
        }
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

    public void refreshProducts() {
        loadData();
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
            String status = (String) cboStatus.getSelectedItem();

            Integer categoryId = null;
            if (categoryName != null && !categoryName.equals("Tất cả")) {
                for (Category c : categoryList) {
                    if (c.getCategoryName().equals(categoryName)) {
                        categoryId = c.getCategoryId();
                        break;
                    }
                }
            }

            String statusFilter = (status != null && !status.equals("Tất cả")) ? status : null;

            productList = productService.filter(categoryId, null, statusFilter);
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
            refreshDashboard();
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
                refreshDashboard();
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
                    refreshDashboard();
                } catch (Exception e) {
                    DialogUtils.showError(this, "Lỗi xóa: " + e.getMessage());
                }
            }
        }
    }

    private void updateInfo() {
        JLabel lblInfo = (JLabel) ((JPanel) getComponent(2)).getComponent(0);
        lblInfo.setText("Tổng số sản phẩm: " + productList.size());
    }

    private void applyPermissions() {
        boolean canCreate = RolePermission.has("PRODUCT_CREATE");
        btnCreate.setEnabled(canCreate);
    }

    class ProductTableModel extends AbstractTableModel {
        private final String[] columns = {
                "STT", "Ảnh", "Mã Sản Phẩm", "Đơn vị", "Tên Sản Phẩm", "Danh mục",
                "Nhập (VND)", "Bán (VND)", "Tồn Kho", "Trang Thái", "Thao Tác"
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
                case 0:
                    return row + 1;
                case 1:
                    return p;
                case 2:
                    return p.getBarcode();
                case 3:
                    return p.getUnit();
                case 4:
                    return p.getProductName();
                case 5:
                    return p.getCategoryName();
                case 6:
                    return MoneyUtils.format(p.getImportPrice());
                case 7:
                    return MoneyUtils.format(p.getSalePrice());
                case 8:
                    return p.getStockQty();
                case 9: {
                    int stock = p.getStockQty();
                    if (stock == 0) return "Hết hàng";
                    else if (stock < 20) return "Sắp hết hàng";
                    else return "Còn";
                }
                case 10:
                    return "Sửa | Xóa";
                default:
                    return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 10;
        }
    }

    private class ImageCellRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        public ImageCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText("");
            setIcon(null);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(Color.WHITE);
            }

            if (value instanceof Product) {
                Product product = (Product) value;
                ImageIcon icon = loadProductThumbnail(product, 60);
                if (icon != null) {
                    setIcon(icon);
                } else {
                    setText("Không ảnh");
                    setForeground(Color.GRAY);
                }
            }

            return this;
        }
    }

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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
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
        public Object getCellEditorValue() {
            return null;
        }
    }
}