package presentation.dialogs;

import bus.CategoryService;
import bus.ProductService;
import bus.BrandService;
import dal.dao.ProductImageDAO;
import dto.Category;
import dto.Product;
import dto.Brand;
import dto.ProductImage;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * ProductDialog - Thêm/sửa sản phẩm
 */
public class ProductDialog extends JDialog {
    
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final BrandService brandService = new BrandService();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    
    private JTextField txtBarcode, txtName, txtUnit;
    private JTextField txtImportPrice, txtSalePrice, txtStock, txtMinStock;
    private JComboBox<String> cboCategory, cboStatus, cboBrand;
    private JButton btnSave, btnCancel, btnScan, btnUploadImage;
    private JPanel imageListPanel;

    private final java.util.List<Integer> deletedImageIds = new java.util.ArrayList<>();
    
    private Product product;
    private boolean saved = false;
    private List<Category> categoryList;
    private List<Brand> brandList;
    private List<ProductImage> productImages = new java.util.ArrayList<>();
    private int primaryImageId = -1;

    public ProductDialog(Frame parent, Product product) {
        super(parent, product == null ? "Tạo sản phẩm mới" : "Sửa sản phẩm", true);
        this.product = product;
        
        initComponents();
        loadCategories();
        loadBrands();
        if (product != null) {
            fillData();
        }
        
        setSize(850, 650);
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

        // Row 0: Mã sản phẩm (1 col) + Product Image Panel (1 col, rowspan 3)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.gridheight = 1;
        cardPanel.add(createLabeledField("Mã sản phẩm *", txtBarcode = new JTextField()), gbc);
        
        gbc.gridx = 1; gbc.gridheight = 3; gbc.fill = GridBagConstraints.BOTH; gbc.anchor = GridBagConstraints.NORTH;
        cardPanel.add(createImagePanel(), gbc);
        
        // Reset fill for other fields
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridheight = 1;

        // Row 1: Tên sản phẩm
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Tên sản phẩm *", txtName = new JTextField()), gbc);

        // Row 2: Thương hiệu
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Thương hiệu", cboBrand = new JComboBox<>()), gbc);

        // Row 3: Đơn vị | Danh mục | Trạng thái (3 cols)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Đơn vị *", txtUnit = new JTextField()), gbc);
        gbc.gridx = 1;
        cardPanel.add(createLabeledField("Danh mục *", cboCategory = new JComboBox<>()), gbc);
        gbc.gridx = 2;
        cardPanel.add(createLabeledField("Trạng thái *", cboStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"})), gbc);

        // Row 4: Giá nhập | Giá bán (2 cols)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Giá nhập (VND) *", txtImportPrice = new JTextField()), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cardPanel.add(createLabeledField("Giá bán (VND) *", txtSalePrice = new JTextField()), gbc);

        // Row 5: Tồn kho | Tồn tối thiểu (2 cols)
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        cardPanel.add(createLabeledField("Tồn kho *", txtStock = new JTextField()), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cardPanel.add(createLabeledField("Tồn tối thiểu", txtMinStock = new JTextField()), gbc);

        mainPanel.add(cardPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Wrap mainPanel in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

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

    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setOpaque(false);

        JLabel lblImageTitle = new JLabel("Ảnh sản phẩm");
        lblImageTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblImageTitle.setForeground(new Color(60, 60, 60));
        lblImageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Scrollable image list panel
        imageListPanel = new JPanel();
        imageListPanel.setLayout(new BoxLayout(imageListPanel, BoxLayout.Y_AXIS));
        imageListPanel.setBackground(new Color(240, 240, 240));
        
        JScrollPane scrollPane = new JScrollPane(imageListPanel);
        scrollPane.setPreferredSize(new Dimension(250, 200));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        btnUploadImage = createStyledButton("+ Thêm ảnh", new Color(76, 175, 80), Color.WHITE);
        btnUploadImage.setPreferredSize(new Dimension(100, 35));
        btnUploadImage.setMaximumSize(new Dimension(100, 35));
        btnUploadImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnUploadImage.addActionListener(e -> uploadImage());

        imagePanel.add(lblImageTitle);
        imagePanel.add(Box.createVerticalStrut(5));
        imagePanel.add(scrollPane);
        imagePanel.add(Box.createVerticalStrut(5));
        imagePanel.add(btnUploadImage);

        return imagePanel;
    }

    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return f.isDirectory() || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
            }

            @Override
            public String getDescription() {
                return "Image files (JPG, PNG, GIF)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            addImage(selectedFile.getAbsolutePath());
        }
    }

    private void addImage(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "File ảnh không tồn tại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) {
                JOptionPane.showMessageDialog(this, "Không thể đọc file ảnh", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String savedPath = copyImageToLocalFolder(imagePath);

            ProductImage img = new ProductImage();
            img.setImageId(-(productImages.size() + 1));
            img.setImageUrl(savedPath);
            img.setIsPrimary(productImages.isEmpty());

            if (img.getIsPrimary()) {
                primaryImageId = img.getImageId();
            }

            productImages.add(img);
            refreshImageList();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải ảnh: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshImageList() {
        imageListPanel.removeAll();
        
        if (productImages.isEmpty()) {
            JLabel lblNoImage = new JLabel("Chưa có ảnh");
            lblNoImage.setForeground(new Color(100, 100, 100));
            lblNoImage.setAlignmentX(Component.LEFT_ALIGNMENT);
            imageListPanel.add(lblNoImage);
        } else {
            for (ProductImage img : productImages) {
                JPanel itemPanel = createImageItem(img);
                imageListPanel.add(itemPanel);
                imageListPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        imageListPanel.revalidate();
        imageListPanel.repaint();
    }

    private JPanel createImageItem(ProductImage img) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout(5, 5));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        itemPanel.setPreferredSize(new Dimension(240, 80));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Thumbnail panel (WEST)
        JLabel lblThumbnail = new JLabel();
        lblThumbnail.setPreferredSize(new Dimension(70, 70));
        lblThumbnail.setOpaque(true);
        lblThumbnail.setBackground(new Color(240, 240, 240));
        lblThumbnail.setHorizontalAlignment(JLabel.CENTER);
        lblThumbnail.setVerticalAlignment(JLabel.CENTER);

        try {
            BufferedImage bufferedImage;
            String imagePath = img.getImageUrl();

            if (imagePath != null && (imagePath.startsWith("http://") || imagePath.startsWith("https://"))) {
                bufferedImage = ImageIO.read(new java.net.URL(imagePath));
            } else {
                bufferedImage = ImageIO.read(new File(imagePath));
            }

            if (bufferedImage != null) {
                int thumbWidth = 70;
                int thumbHeight = 70;
                double scale = Math.min((double) thumbWidth / bufferedImage.getWidth(),
                        (double) thumbHeight / bufferedImage.getHeight());
                int w = (int) (bufferedImage.getWidth() * scale);
                int h = (int) (bufferedImage.getHeight() * scale);
                Image scaledImage = bufferedImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                lblThumbnail.setIcon(new ImageIcon(scaledImage));
            } else {
                lblThumbnail.setText("Lỗi ảnh");
                lblThumbnail.setForeground(new Color(100, 100, 100));
            }
        } catch (Exception e) {
            System.err.println("Error loading thumbnail: " + e.getMessage());
            lblThumbnail.setText("Lỗi ảnh");
            lblThumbnail.setForeground(new Color(100, 100, 100));
        }


        itemPanel.add(lblThumbnail, BorderLayout.WEST);

        // Center panel (info + checkbox)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel lblFileName = new JLabel(new File(img.getImageUrl()).getName());
        lblFileName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFileName.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblFileName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JCheckBox chkPrimary = new JCheckBox("Ảnh chính", img.getIsPrimary() != null && img.getIsPrimary());
        chkPrimary.setOpaque(false);
        chkPrimary.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkPrimary.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkPrimary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        chkPrimary.addActionListener(e -> {
            if (chkPrimary.isSelected()) {
                for (ProductImage pi : productImages) {
                    pi.setIsPrimary(false);
                }
                img.setIsPrimary(true);
                primaryImageId = img.getImageId();
                refreshImageList();
            }
        });

        centerPanel.add(lblFileName);
        centerPanel.add(chkPrimary);
        itemPanel.add(centerPanel, BorderLayout.CENTER);

        // Delete button (EAST)
        JButton btnDelete = new JButton("Xóa");
        btnDelete.setPreferredSize(new Dimension(50, 30));
        btnDelete.setMaximumSize(new Dimension(50, 70));
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDelete.setBackground(new Color(244, 67, 54));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setOpaque(true);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> {
            if (img.getImageId() != null && img.getImageId() > 0) {
                deletedImageIds.add(img.getImageId());
            }

            productImages.remove(img);

            if (img.getIsPrimary() != null && img.getIsPrimary()) {
                if (!productImages.isEmpty()) {
                    for (ProductImage pi : productImages) {
                        pi.setIsPrimary(false);
                    }
                    productImages.get(0).setIsPrimary(true);
                    primaryImageId = productImages.get(0).getImageId() != null ? productImages.get(0).getImageId() : -1;
                } else {
                    primaryImageId = -1;
                }
            }

            refreshImageList();
        });

        itemPanel.add(btnDelete, BorderLayout.EAST);
        return itemPanel;
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

    private void loadBrands(){
        try {
            brandList = brandService.getAll();
            cboBrand.removeAllItems();
            for (Brand b : brandList) {
                cboBrand.addItem(b.getBrandName());
            }
        } catch (Exception e){
            JOptionPane.showMessageDialog(this, "Lỗi tải thương hiệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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

        // Select brand if available
        if (product.getBrandId() != null) {
            for (int i = 0; i < brandList.size(); i++) {
                if (brandList.get(i).getBrandId().equals(product.getBrandId())) {
                    cboBrand.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Load product images
        try {
            if (product.getProductId() != null && product.getProductId() > 0) {
                List<ProductImage> dbImages = productImageDAO.findByProductId(product.getProductId());
                if (dbImages != null && !dbImages.isEmpty()) {
                    productImages.addAll(dbImages);
                    // Find primary image
                    for (ProductImage img : productImages) {
                        if (img.getIsPrimary() != null && img.getIsPrimary()) {
                            primaryImageId = img.getImageId();
                            break;
                        }
                    }
                }
            }
            refreshImageList();
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }


    private String copyImageToLocalFolder(String sourcePath) throws Exception {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            throw new Exception("File nguồn không tồn tại");
        }

        File uploadDir = new File("resources/images/products");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalName = sourceFile.getName();
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot);
        }

        String newFileName = "product_" + System.currentTimeMillis() + ext;
        File destFile = new File(uploadDir, newFileName);

        java.nio.file.Files.copy(
                sourceFile.toPath(),
                destFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );

        return destFile.getPath().replace("\\", "/");
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

            // Set brand (optional)
            int brandIdx = cboBrand.getSelectedIndex();
            if (brandIdx >= 0 && brandIdx < brandList.size()) {
                product.setBrandId(brandList.get(brandIdx).getBrandId());
            } else {
                product.setBrandId(null);
            }

            // Save
            if (product.getProductId() == null || product.getProductId() == 0) {
                int newProductId = productService.create(product);
                product.setProductId(newProductId);
                JOptionPane.showMessageDialog(this, "Tạo sản phẩm thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                // Save images if exist (after product created)
                if (!productImages.isEmpty()) {
                    try {
                        for (ProductImage img : productImages) {
                            img.setProductId(product.getProductId());
                            if (img.getImageId() < 0) { // New image
                                productImageDAO.insert(img);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error saving images: " + e.getMessage());
                    }
                }
            } else {
                productService.update(product);
                JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                // Save new images
                if (!productImages.isEmpty()) {
                    try {
                        // 1. Xóa các ảnh cũ đã bị user xóa trên UI
                        for (Integer imageId : deletedImageIds) {
                            productImageDAO.delete(imageId);
                        }

                        // 2. Insert các ảnh mới
                        java.util.List<Integer> insertedImageIds = new java.util.ArrayList<>();

                        for (ProductImage img : productImages) {
                            if (img.getImageId() != null && img.getImageId() < 0) {
                                img.setProductId(product.getProductId());
                                int newImageId = productImageDAO.insert(img);
                                if (newImageId > 0) {
                                    img.setImageId(newImageId);
                                    insertedImageIds.add(newImageId);
                                }
                            }
                        }

                        // 3. Set lại ảnh chính đúng theo primaryImageId
                        int selectedPrimaryId = -1;
                        for (ProductImage img : productImages) {
                            if (img.getIsPrimary() != null && img.getIsPrimary()) {
                                if (img.getImageId() != null) {
                                    selectedPrimaryId = img.getImageId();
                                    break;
                                }
                            }
                        }

                        if (selectedPrimaryId > 0) {
                            productImageDAO.setPrimaryImage(product.getProductId(), selectedPrimaryId);
                        }

                    } catch (Exception e) {
                        System.err.println("Error saving/updating images: " + e.getMessage());
                    }
                }
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
