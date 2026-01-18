package presentation.dialogs;

import bus.CategoryService;
import bus.ProductService;
import dto.Category;
import dto.Product;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * ProductDialog - Thêm/sửa sản phẩm
 */
public class ProductDialog extends JDialog {
    
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    
    private JTextField txtBarcode, txtName, txtUnit;
    private JTextField txtImportPrice, txtSalePrice, txtStock, txtMinStock;
    private JComboBox<String> cboCategory, cboStatus;
    private JButton btnSave, btnCancel, btnScan;
    
    private Product product;
    private boolean saved = false;
    private List<Category> categoryList;

    public ProductDialog(Frame parent, Product product) {
        super(parent, product == null ? "Tạo sản phẩm mới" : "Sửa sản phẩm", true);
        this.product = product;
        
        initComponents();
        loadCategories();
        if (product != null) {
            fillData();
        }
        
        setSize(750, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 246, 248));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(245, 246, 248));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        JLabel lblTitle = new JLabel(product == null ? "Tạo sản phẩm mới" : "Chỉnh sửa sản phẩm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Main Form Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 246, 248));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Card Panel
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 234), 1, true),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        cardPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Row 0: Mã sản phẩm (1 col)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Mã sản phẩm *", txtBarcode = new JTextField()), gbc);

        // Row 1: Tên sản phẩm (3 cols)
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        cardPanel.add(createLabeledField("Tên sản phẩm *", txtName = new JTextField()), gbc);

        // Row 2: Đơn vị | Danh mục | Trạng thái (3 cols)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Đơn vị *", txtUnit = new JTextField()), gbc);
        gbc.gridx = 1;
        cardPanel.add(createLabeledField("Danh mục *", cboCategory = new JComboBox<>()), gbc);
        gbc.gridx = 2;
        cardPanel.add(createLabeledField("Trạng thái *", cboStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"})), gbc);

        // Row 3: Giá nhập | Giá bán (2 cols)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Giá nhập (VND) *", txtImportPrice = new JTextField()), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cardPanel.add(createLabeledField("Giá bán (VND) *", txtSalePrice = new JTextField()), gbc);

        // Row 4: Tồn kho | Tồn tối thiểu (2 cols)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Tồn kho *", txtStock = new JTextField()), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cardPanel.add(createLabeledField("Tồn tối thiểu", txtMinStock = new JTextField()), gbc);

        mainPanel.add(cardPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        add(mainPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(245, 246, 248));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        btnCancel = createStyledButton("Hủy", new Color(100, 100, 100), Color.WHITE);
        btnCancel.addActionListener(e -> dispose());

        btnSave = createStyledButton("Lưu thông tin", new Color(33, 150, 243), Color.WHITE);
        btnSave.addActionListener(e -> save());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createLabeledField(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(60, 60, 60));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (field instanceof JTextField) {
            ((JTextField) field).setPreferredSize(new Dimension(0, 35));
            ((JTextField) field).setFont(new Font("Segoe UI", Font.PLAIN, 13));
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setPreferredSize(new Dimension(0, 35));
            ((JComboBox<?>) field).setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);

        return panel;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    private void loadCategories() {
        try {
            categoryList = categoryService.getAll();
            cboCategory.removeAllItems();
            for (Category c : categoryList) {
                cboCategory.addItem(c.getCategoryName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh mục: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillData() {
        txtBarcode.setText(product.getBarcode());
        txtName.setText(product.getProductName());
        txtUnit.setText(product.getUnit());
        txtImportPrice.setText(String.valueOf(product.getImportPrice()));
        txtSalePrice.setText(String.valueOf(product.getSalePrice()));
        txtStock.setText(String.valueOf(product.getStockQty()));
        txtMinStock.setText(String.valueOf(product.getMinStock()));
        cboStatus.setSelectedItem(product.getStatus());

        // Select category
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getCategoryId() == product.getCategoryId()) {
                cboCategory.setSelectedIndex(i);
                break;
            }
        }
    }

    private void save() {
        try {
            // Validate
            String barcode = txtBarcode.getText().trim();
            String name = txtName.getText().trim();
            String unit = txtUnit.getText().trim();
            String importPriceStr = txtImportPrice.getText().trim();
            String salePriceStr = txtSalePrice.getText().trim();
            String stockStr = txtStock.getText().trim();
            String minStockStr = txtMinStock.getText().trim();

            if (barcode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mã sản phẩm", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                txtBarcode.requestFocus();
                return;
            }
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sản phẩm", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                txtName.requestFocus();
                return;
            }
            if (unit.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đơn vị", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                txtUnit.requestFocus();
                return;
            }
            if (cboCategory.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double importPrice = 0, salePrice = 0;
            int stock = 0, minStock = 0;

            try {
                importPrice = Double.parseDouble(importPriceStr);
                salePrice = Double.parseDouble(salePriceStr);
                stock = Integer.parseInt(stockStr);
                minStock = Integer.parseInt(minStockStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Giá và số lượng phải là số hợp lệ", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (importPrice <= 0 || salePrice <= 0) {
                JOptionPane.showMessageDialog(this, "Giá phải lớn hơn 0", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (stock < 0 || minStock < 0) {
                JOptionPane.showMessageDialog(this, "Số lượng không được âm", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create/Update product
            if (product == null) {
                product = new Product();
            }

            product.setBarcode(barcode);
            product.setProductName(name);
            product.setUnit(unit);
            product.setImportPrice(BigDecimal.valueOf(importPrice));
            product.setSalePrice(BigDecimal.valueOf(salePrice));
            product.setStockQty(stock);
            product.setMinStock(minStock);
            product.setStatus((String) cboStatus.getSelectedItem());

            // Set category
            int categoryIdx = cboCategory.getSelectedIndex();
            product.setCategoryId(categoryList.get(categoryIdx).getCategoryId());

            // Save
            if (product.getProductId() == null || product.getProductId() == 0) {
                productService.create(product);
                JOptionPane.showMessageDialog(this, "Tạo sản phẩm thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                productService.update(product);
                JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
