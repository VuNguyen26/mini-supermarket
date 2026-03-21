package presentation.panels;

import bus.CategoryService;
import bus.ProductService;
import com.formdev.flatlaf.FlatClientProperties;
import dto.Category;
import presentation.dialogs.CategoryDialog;
import util.DialogUtils;
import util.RolePermission;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryPanel extends JPanel {

    private final CategoryService service = new CategoryService();
    private final ProductService productService = new ProductService();
    
    private JTextField txtSearch;
    private JPanel cardsPanel;
    private JButton btnAdd;

    private boolean canView;
    private boolean canCreate;
    private boolean canUpdate;
    private boolean canDelete;
    
    private List<Category> categoryList = new ArrayList<>();
    private List<Category> filteredList = new ArrayList<>();

    public CategoryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 246, 248));

        initPermissions();

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildScrollableCards(), BorderLayout.CENTER);

        loadData();
        applyPermissions();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(new Color(245, 246, 248));

        // Left side - Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(new Color(245, 246, 248));

        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm danh mục");
        txtSearch.putClientProperty("JComponent.arc", 8);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchCategories();
            }
        });
        
        searchPanel.add(txtSearch);

        // Right side - Add button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(245, 246, 248));

        btnAdd = createStyledButton("+ Tạo Danh mục", new Color(33, 150, 243), Color.WHITE, true);
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.addActionListener(e -> addCategory());
        
        buttonPanel.add(btnAdd);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(buttonPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JScrollPane buildScrollableCards() {
        cardsPanel = new CardsPanel();

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(new Color(245, 246, 248));
        scrollPane.getViewport().setBackground(new Color(245, 246, 248));
        // Only vertical scroll; track viewport width to wrap cards
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    private void loadData() {
        try {
            categoryList = service.getAll();
            filteredList = new ArrayList<>(categoryList);
            refreshCards();
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tải danh mục: " + e.getMessage());
        }
    }

    private void searchCategories() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        filteredList.clear();

        if (keyword.isEmpty()) {
            filteredList.addAll(categoryList);
        } else {
            for (Category c : categoryList) {
                if (c.getCategoryName() != null && c.getCategoryName().toLowerCase().contains(keyword)) {
                    filteredList.add(c);
                }
            }
        }
        refreshCards();
    }

    private void refreshCards() {
        cardsPanel.removeAll();

        for (Category c : filteredList) {
            cardsPanel.add(createCategoryCard(c));
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    // Panel that wraps cards and tracks viewport width to avoid horizontal scrolling
    // Panel hiển thị card: CỐ ĐỊNH 3 card / 1 hàng
    private static class CardsPanel extends JPanel implements Scrollable {

        private static final int GAP = 20;
        private static final int COLS = 4;

        public CardsPanel() {
            super(new GridLayout(0, COLS, GAP, GAP));
            setBackground(new Color(245, 246, 248));
            setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height - 20;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public Dimension getPreferredSize() {
            // tính chiều cao để scroll mượt (GridLayout cần)
            int total = getComponentCount();
            int rows = (int) Math.ceil(total / (double) COLS);

            int cardHeight = 180;
            int height = (rows * cardHeight) + ((rows + 1) * GAP);

            if (getParent() instanceof JViewport) {
                int width = ((JViewport) getParent()).getWidth();
                return new Dimension(width, height);
            }
            return super.getPreferredSize();
        }
    }

    private JPanel createCategoryCard(Category category) {
        JPanel card = new RoundedPanel(18, new Color(225, 228, 234), 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);

        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        card.setPreferredSize(new Dimension(280, 180));
        card.setMaximumSize(new Dimension(280, 180));

        // Header: Category name
        JLabel lblTitle = new JLabel(category.getCategoryName());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(15, 23, 42));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));

        // Info panel: Số lượng (left) và Trạng thái (right)
        JPanel infoPanel = new JPanel(new BorderLayout(0, 0));
        infoPanel.setOpaque(false);
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        int productCount = 0;
        try {
            productCount = productService.getProductCountByCategory(category.getCategoryId());
        } catch (Exception e) {
            System.err.println("Error counting products for category " + category.getCategoryId() + ": " + e.getMessage());
        }

        String status = productCount == 0 ? "Hết" : "Còn";

        JLabel lblQty = new JLabel("Số lượng: " + productCount);
        lblQty.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblQty.setForeground(new Color(60, 60, 60));
        lblQty.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel lblStatus = new JLabel("Trạng thái: " + status);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(productCount == 0 ? new Color(244, 67, 54) : new Color(76, 175, 80));
        lblStatus.setHorizontalAlignment(SwingConstants.RIGHT);

        infoPanel.add(lblQty, BorderLayout.WEST);
        infoPanel.add(lblStatus, BorderLayout.EAST);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(infoPanel);
        card.add(Box.createVerticalGlue());

        // Action buttons in one row
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnView = createStyledButton("Xem", new Color(76, 175, 80), Color.WHITE, false);
        btnView.setPreferredSize(new Dimension(65, 34));
        btnView.setEnabled(canView);
        btnView.addActionListener(e -> {
            if (!canView) {
                DialogUtils.showError(this, "Bạn không có quyền xem danh mục.");
                return;
            }
            viewCategoryProducts(category);
        });

        JButton btnEdit = createStyledButton("Sửa", new Color(33, 150, 243), Color.WHITE, true);
        btnEdit.setPreferredSize(new Dimension(65, 34));
        btnEdit.setEnabled(canUpdate);
        btnEdit.addActionListener(e -> {
            if (!canUpdate) {
                DialogUtils.showError(this, "Bạn không có quyền sửa danh mục.");
                return;
            }

            CategoryDialog dialog = new CategoryDialog((Frame) SwingUtilities.getWindowAncestor(this), category);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadData();
            }
        });

        JButton btnDelete = createStyledButton("Xóa", new Color(244, 67, 54), Color.WHITE, false);
        btnDelete.setPreferredSize(new Dimension(65, 34));
        btnDelete.setEnabled(canDelete);
        btnDelete.addActionListener(e -> {
            if (!canDelete) {
                DialogUtils.showError(this, "Bạn không có quyền xóa danh mục.");
                return;
            }

            if (DialogUtils.confirm(this, "Xác nhận xóa danh mục: " + category.getCategoryName() + "?")) {
                try {
                    service.delete(category.getCategoryId());
                    DialogUtils.showInfo(this, "Đã xóa danh mục");
                    loadData();
                } catch (Exception ex) {
                    DialogUtils.showError(this, "Lỗi xóa: " + ex.getMessage());
                }
            }
        });

        actionPanel.add(btnView);
        actionPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        actionPanel.add(btnEdit);
        actionPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        actionPanel.add(btnDelete);
        card.add(actionPanel);

        return card;
    }

    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color borderColor;
        private final int borderThickness;

        public RoundedPanel(int arc, Color borderColor, int borderThickness) {
            this.arc = arc;
            this.borderColor = borderColor;
            this.borderThickness = borderThickness;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // nền bo góc
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderThickness));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            g2.dispose();
        }
    }


    private void addCategory() {
        CategoryDialog dialog = new CategoryDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void viewCategoryProducts(Category category) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sản phẩm - " + category.getCategoryName(), true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo((Frame) SwingUtilities.getWindowAncestor(this));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel lblTitle = new JLabel("Danh sách sản phẩm trong danh mục: " + category.getCategoryName());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        content.add(lblTitle, BorderLayout.NORTH);

        // Products table
        try {
            java.util.List<dto.Product> products = productService.filter(category.getCategoryId(), null, null);
            
            String[] columns = {"STT", "Mã", "Tên sản phẩm", "Đơn vị", "Giá nhập", "Giá bán", "Tồn kho"};
            Object[][] data = new Object[products.size()][7];
            
            for (int i = 0; i < products.size(); i++) {
                dto.Product p = products.get(i);
                data[i][0] = i + 1;
                data[i][1] = p.getBarcode();
                data[i][2] = p.getProductName();
                data[i][3] = p.getUnit();
                data[i][4] = util.MoneyUtils.format(p.getImportPrice());
                data[i][5] = util.MoneyUtils.format(p.getSalePrice());
                data[i][6] = p.getStockQty();
            }
            
            JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            table.setDefaultEditor(Object.class, null); // read-only
            
            // Style header: blue background, white text
            javax.swing.table.JTableHeader header = table.getTableHeader();
            header.setBackground(new Color(33, 150, 243));
            header.setForeground(Color.WHITE);
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            table.getColumnModel().getColumn(0).setPreferredWidth(40);
            table.getColumnModel().getColumn(1).setPreferredWidth(80);
            table.getColumnModel().getColumn(2).setPreferredWidth(250);
            
            JScrollPane scrollPane = new JScrollPane(table);
            content.add(scrollPane, BorderLayout.CENTER);
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tải sản phẩm: " + e.getMessage());
        }

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnClose);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color bg, Color fg, boolean primary) {
        JButton button = new JButton(text);
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        if (primary) {
            button.putClientProperty(FlatClientProperties.STYLE_CLASS, "primary");
        }
        if (bg != null) {
            button.setBackground(bg);
        }
        if (fg != null) {
            button.setForeground(fg);
        }
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void applyPermissions() {
        btnAdd.setEnabled(canCreate);
    }

    private void initPermissions() {
        canView = RolePermission.has("CATEGORY_VIEW");
        canCreate = RolePermission.has("CATEGORY_CREATE");
        canUpdate = RolePermission.has("CATEGORY_UPDATE");
        canDelete = RolePermission.has("CATEGORY_DELETE");
    }
}
