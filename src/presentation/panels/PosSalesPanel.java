package presentation.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import org.jdesktop.swingx.prompt.PromptSupport;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import dal.dao.ProductDAO;
import dal.dao.CustomerDAO;
import dto.Product;
import dto.Customer;
import dto.Payment;
import dto.SalesInvoice;
import dto.SalesInvoiceDetail;
import bus.SalesService;

public class PosSalesPanel extends JPanel {
    Font lolitaFont = new Font("lolita", Font.PLAIN, 14);

    // Card Layout Components
    private static final String CARD_POS = "POS";
    private static final String CARD_PAYMENT = "PAYMENT";
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardRoot = new JPanel(cardLayout);
    private final JPanel pnlPosCard = new JPanel(new BorderLayout());
    private final JPanel pnlPaymentCard = new JPanel(new BorderLayout());

    // Left Components
    private JTextField txtSearch;
    private JTable tableProducts;
    private DefaultTableModel modelProducts;
    private List<Product> productList;
    private ProductDAO productDAO = new ProductDAO();

    // Right Components
    private JComboBox<Customer> cboCustomers;
    private CustomerDAO customerDAO = new CustomerDAO();

    private JTable tableCart;
    private DefaultTableModel modelCart;

    // Payment Summary (in POS card)
    private JLabel lblSubTotalValue;
    private JLabel lblDiscountValue;
    private JLabel lblTotalValue;

    // Money formatter
    private DecimalFormat df = new DecimalFormat("#,###");

    private static final Color PRODUCT_LINK_BLUE = new Color(33, 150, 243);

    public PosSalesPanel() {
        initComponent();
        loadDataToTable();
        loadCustomers();
    }

    private void refreshDashboard() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof presentation.MainFrame) {
            ((presentation.MainFrame) owner).refreshDashboard();
        }
    }

    private void initComponent() {
        setOpaque(false);
        setLayout(new BorderLayout());

        // POS main view
        pnlPosCard.setOpaque(false);

        JComponent left = buildLeftProductPane();
        JComponent right = buildRightPaymentPane();

        left.setMinimumSize(new Dimension(300, 0));
        right.setMinimumSize(new Dimension(320, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setBorder(null);
        split.setDividerSize(10);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.5);
        split.setDividerLocation(600);

        pnlPosCard.add(split, BorderLayout.CENTER);

        // Payment overlay card
        pnlPaymentCard.setOpaque(false);

        // Card container
        cardRoot.setOpaque(false);
        cardRoot.add(pnlPosCard, CARD_POS);
        cardRoot.add(pnlPaymentCard, CARD_PAYMENT);

        add(cardRoot, BorderLayout.CENTER);
        showPosCard();
    }

    private void showPosCard() {
        cardLayout.show(cardRoot, CARD_POS);
    }

    private void showPaymentCard(PaymentPanel panel) {
        pnlPaymentCard.removeAll();
        pnlPaymentCard.add(panel, BorderLayout.CENTER);
        pnlPaymentCard.revalidate();
        pnlPaymentCard.repaint();
        cardLayout.show(cardRoot, CARD_PAYMENT);
        panel.requestFocusInWindow();
    }

    private void loadDataToTable() {
        productList = productDAO.getAllProducts();
        modelProducts.setRowCount(0);
        for (Product p : productList) {
            Object[] row = {
                    p.getProductId(),
                    p.getProductName(),
                    p.getUnit(),
                    p.getSalePrice(),
                    p.getStockQty()
            };
            modelProducts.addRow(row);
        }
    }

    public void refreshProducts() {
        loadDataToTable();
        if (txtSearch != null) {
            String keyword = txtSearch.getText();
            if (keyword != null && !keyword.trim().isEmpty()) {
                filterProduct(keyword);
            }
        }
    }

    private void loadCustomers() {
        cboCustomers.removeAllItems();
        cboCustomers.addItem(new Customer(0, "Khách lẻ", ""));
        List<Customer> customers = customerDAO.findAll();
        for (Customer c : customers) {
            cboCustomers.addItem(c);
        }
    }

    private JComponent buildLeftProductPane() {
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(400, 0));

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        txtSearch = new JTextField();
        PromptSupport.setPrompt("Nhập mã hoặc tên sản phẩm...", txtSearch);
        var titled = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Tìm kiếm", TitledBorder.LEFT, TitledBorder.TOP, lolitaFont);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(titled, BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterProduct(txtSearch.getText());
            }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        left.add(searchPanel, BorderLayout.NORTH);

        // Product Table
        String[] headersProduct = { "Mã", "Tên sản phẩm", "Đơn vị", "Giá bán", "Tồn kho" };
        modelProducts = new DefaultTableModel(headersProduct, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? Double.class : Object.class;
            }
        };
        tableProducts = new JTable(modelProducts);
        tableProducts.setRowHeight(30);
        tableProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableProducts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableProducts.getTableHeader().setBackground(new Color(33, 150, 243));
        tableProducts.getTableHeader().setForeground(Color.WHITE);
        tableProducts.getTableHeader().setOpaque(true);
        tableProducts.getTableHeader().setReorderingAllowed(false);
        tableProducts.getTableHeader().setResizingAllowed(false);
        DefaultTableCellRenderer blackTextRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        tableProducts.getColumnModel().getColumn(0).setCellRenderer(blackTextRenderer);
        tableProducts.getColumnModel().getColumn(1).setCellRenderer(blackTextRenderer);
        tableProducts.getColumnModel().getColumn(2).setCellRenderer(blackTextRenderer);
        tableProducts.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = df.format(value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        tableProducts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    addToCart();
            }
        });

        left.add(new JScrollPane(tableProducts), BorderLayout.CENTER);
        return left;
    }

    private JComponent buildRightPaymentPane() {
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setPreferredSize(new Dimension(400, 0));
        right.setOpaque(false);

        // Customer Panel
        JPanel customerPanel = new JPanel(new BorderLayout());
        customerPanel.setOpaque(false);
        customerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Khách hàng", TitledBorder.LEFT, TitledBorder.TOP,
                lolitaFont));

        cboCustomers = new JComboBox<>();
        cboCustomers.setEditable(true);
        cboCustomers.setPreferredSize(new Dimension(0, 35));

        Component editor = cboCustomers.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            JTextField textField = (JTextField) editor;
            PromptSupport.setPrompt("Chọn hoặc tìm kiếm khách hàng...", textField);
            textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String keyword = textField.getText();
                    cboCustomers.removeAllItems();
                    List<Customer> customers = customerDAO.searchCustomer(keyword);
                    if (customers.isEmpty()) {
                        cboCustomers.addItem(new Customer(0, "Khách lẻ", ""));
                    } else {
                        for (Customer c : customers) {
                            cboCustomers.addItem(c);
                        }
                    }
                    textField.setText(keyword);
                    cboCustomers.showPopup();
                }
            });
        }

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(-8, 2, 2, 2));
        inner.add(cboCustomers, BorderLayout.CENTER);
        customerPanel.add(inner, BorderLayout.CENTER);
        right.add(customerPanel, BorderLayout.NORTH);

        // Cart Panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setOpaque(false);
        String[] headersCart = { "Mã", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền" };
        modelCart = new DefaultTableModel(headersCart, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch(columnIndex) {
                    case 0: return Integer.class;
                    case 1: return String.class;
                    case 2: return Integer.class;
                    case 3: return Double.class;
                    case 4: return Double.class;
                    default: return Object.class;
                }
            }
        };
        tableCart = new JTable(modelCart);
        tableCart.setRowHeight(30);
        tableCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableCart.getTableHeader().setBackground(new Color(33, 150, 243));
        tableCart.getTableHeader().setForeground(Color.WHITE);
        tableCart.getTableHeader().setOpaque(true);
        tableCart.getTableHeader().setReorderingAllowed(false);
        tableCart.getTableHeader().setResizingAllowed(false);
        modelCart.addTableModelListener(e -> {
            if (e.getColumn() == 2)
                updateTotalMoney();
        });

        tableCart.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = df.format(value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        tableCart.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = df.format(value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        tableCart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    removeFromCart();
            }
        });

        cartPanel.add(new JScrollPane(tableCart), BorderLayout.CENTER);
        right.add(cartPanel, BorderLayout.CENTER);

        // Payment Footer
        JPanel paymentContainer = new JPanel(new BorderLayout(0, 8));
        paymentContainer.setOpaque(false);

        JPanel paymentSummary = new JPanel(new GridLayout(3, 1, 0, 5));
        paymentSummary.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(new JLabel("Tạm tính: "), BorderLayout.WEST);
        lblSubTotalValue = new JLabel("0", SwingConstants.RIGHT);
        p1.add(lblSubTotalValue, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(new JLabel("Giảm giá: "), BorderLayout.WEST);
        lblDiscountValue = new JLabel("0", SwingConstants.RIGHT);
        p2.add(lblDiscountValue, BorderLayout.CENTER);

        JPanel p3 = new JPanel(new BorderLayout());
        JLabel lblTotal = new JLabel("TỔNG TIỀN: ");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        p3.add(lblTotal, BorderLayout.WEST);
        lblTotalValue = new JLabel("0", SwingConstants.RIGHT);
        lblTotalValue.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalValue.setForeground(Color.RED);
        p3.add(lblTotalValue, BorderLayout.EAST);

        paymentSummary.add(p1);
        paymentSummary.add(p2);
        paymentSummary.add(p3);

        JButton btnPay = new JButton("THANH TOÁN");
        btnPay.setBackground(new Color(0, 122, 204));
        btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Arial", Font.BOLD, 14));
        btnPay.setPreferredSize(new Dimension(0, 50));
        btnPay.addActionListener(e -> doPayment());

        paymentContainer.add(paymentSummary, BorderLayout.CENTER);
        paymentContainer.add(btnPay, BorderLayout.SOUTH);

        right.add(paymentContainer, BorderLayout.SOUTH);
        return right;
    }

    private void filterProduct(String keyword) {
        modelProducts.setRowCount(0);
        String key = keyword.toLowerCase();
        for (Product p : productList) {
            boolean matches = p.getProductName().toLowerCase().contains(key);
            if (!matches && p.getBarcode() != null) {
                matches = p.getBarcode().toLowerCase().contains(key);
            }
            if (matches) {
                Object[] row = {
                        p.getProductId(),
                        p.getProductName(),
                        p.getUnit(),
                        p.getSalePrice(),
                        p.getStockQty()
                };
                modelProducts.addRow(row);
            }
        }
    }

    private void addToCart() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow == -1)
            return;

        int productId = (int) modelProducts.getValueAt(selectedRow, 0);
        String name = (String) modelProducts.getValueAt(selectedRow, 1);
        double price = toDouble(modelProducts.getValueAt(selectedRow, 3));
        int stock = (int) modelProducts.getValueAt(selectedRow, 4);

        if (stock <= 0) {
            JOptionPane.showMessageDialog(this, "Sản phẩm này đã hết hàng!", "Hết hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < modelCart.getRowCount(); i++) {
            int cartId = (int) modelCart.getValueAt(i, 0);
            if (cartId == productId) {
                int currentQty = Integer.parseInt(modelCart.getValueAt(i, 2).toString());
                if (currentQty >= stock) {
                    JOptionPane.showMessageDialog(this, "Không đủ tồn kho để bán thêm!", "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                modelCart.setValueAt(currentQty + 1, i, 2);
                modelCart.setValueAt((currentQty + 1) * price, i, 4);
                updateTotalMoney();
                return;
            }
        }

        modelCart.addRow(new Object[] { productId, name, 1, price, price });
        updateTotalMoney();
    }

    private void removeFromCart() {
        int selectedRow = tableCart.getSelectedRow();
        if (selectedRow != -1) {
            modelCart.removeRow(selectedRow);
            updateTotalMoney();
        }
    }

    private void updateTotalMoney() {
        double subTotal = 0;
        for (int i = 0; i < modelCart.getRowCount(); i++) {
            int qty = toInteger(modelCart.getValueAt(i, 2));
            double price = toDouble(modelCart.getValueAt(i, 3));
            double lineTotal = qty * price;
            modelCart.setValueAt(lineTotal, i, 4);
            subTotal += lineTotal;
        }

        lblSubTotalValue.setText(df.format(subTotal));
        lblTotalValue.setText(df.format(subTotal) + " đ");
    }

    private void doPayment() {
        if (modelCart.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!");
            return;
        }

        final List<SalesInvoiceDetail> detailsSnapshot = buildDetailsFromCart();

        final double subTotal = parseMoney(lblSubTotalValue.getText());
        final double discount = parseMoney(lblDiscountValue.getText());
        double grandTotal = parseMoney(lblTotalValue.getText());
        if (grandTotal <= 0) {
            grandTotal = Math.max(0, subTotal - discount);
        }

        Customer selectedCustomer = resolveSelectedCustomer();
        int customerPoints = 0;
        if (selectedCustomer != null) {
            Integer points = selectedCustomer.getPoints();
            customerPoints = points != null ? points : 0;
        }

        PaymentPanel paymentPanel = new PaymentPanel(detailsSnapshot, subTotal, discount, grandTotal, customerPoints,
                new PaymentPanel.PaymentListener() {
                    @Override
                    public void onConfirm(String method, double customerPay, double change, double discount,
                            double grandTotal, int redeemedPoints, double pointDiscount) {
                        startProcessTransaction(method, customerPay, change, detailsSnapshot, discount, grandTotal,
                                redeemedPoints, pointDiscount);
                    }

                    @Override
                    public void onBack() {
                        showPosCard();
                    }
                });

        showPaymentCard(paymentPanel);
    }

    private List<SalesInvoiceDetail> buildDetailsFromCart() {
        List<SalesInvoiceDetail> details = new ArrayList<>();
        for (int i = 0; i < modelCart.getRowCount(); i++) {
            int pId = (int) modelCart.getValueAt(i, 0);
            String pName = (String) modelCart.getValueAt(i, 1);
            int qty = Integer.parseInt(modelCart.getValueAt(i, 2).toString());
            double price = Double.parseDouble(modelCart.getValueAt(i, 3).toString());

            SalesInvoiceDetail item = new SalesInvoiceDetail(pId, 0, qty, price);
            item.setProductName(pName);
            details.add(item);
        }
        return details;
    }

        private void startProcessTransaction(String method, double given, double change,
            List<SalesInvoiceDetail> detailsSnapshot, double finalDiscount, double finalGrandTotal,
            int redeemedPoints, double pointDiscount) {
            final Customer selectedCustomer = resolveSelectedCustomer();
            final Customer customerForPrint = selectedCustomer != null ? selectedCustomer : new Customer(0, "Khách lẻ", "");
            final int cusId = customerForPrint.getCustomerIdValue();

        final double subTotal = parseMoney(lblSubTotalValue.getText());
        final double discount = Math.max(0, finalDiscount);
        final double checkedGrandTotal = finalGrandTotal > 0
            ? finalGrandTotal
            : Math.max(0, subTotal - discount);

        final SalesInvoice invoice = new SalesInvoice();
        invoice.setCustomerId(cusId);
        invoice.setCreatedBy(1);
        invoice.setPaymentMethod(method);
        invoice.setSubTotal(subTotal);
        invoice.setDiscount(discount);
        invoice.setGrandTotal(checkedGrandTotal);

        final Payment payment = new Payment(0, method, checkedGrandTotal, "Thanh toán POS");
        final SalesService service = new SalesService();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private Exception error;

            @Override
            protected Boolean doInBackground() {
                try {
                    return service.processSale(invoice, detailsSnapshot, payment, redeemedPoints, pointDiscount);
                } catch (Exception ex) {
                    error = ex;
                    return false;
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());

                boolean success;
                try {
                    success = get();
                } catch (Exception ex) {
                    error = ex;
                    success = false;
                }

                if (success) {
                    JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(PosSalesPanel.this);

                    // Dialogs
                        new presentation.dialogs.PaymentDialog(parent, checkedGrandTotal, change, invoice.getInvId())
                            .setVisible(true);
                    new presentation.dialogs.InvoicePrintPreviewDialog(parent, invoice, detailsSnapshot,
                            customerForPrint, given, change).setVisible(true);

                    // Reset UI
                    modelCart.setRowCount(0);
                    updateTotalMoney();
                    loadDataToTable();
                    loadCustomers();
                    refreshDashboard();

                    showPosCard();
                    return;
                }

                String msg;
                if (error != null && error.getMessage() != null && !error.getMessage().trim().isEmpty()) {
                    msg = error.getMessage();
                } else {
                    msg = "Thanh toán thất bại! Có thể do kho không đủ hàng.";
                }
                JOptionPane.showMessageDialog(PosSalesPanel.this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        };

        worker.execute();
    }

    private Customer resolveSelectedCustomer() {
        Object selectedObj = cboCustomers.getSelectedItem();
        if (selectedObj instanceof Customer) {
            return (Customer) selectedObj;
        }

        String keyword = "";
        Component editor = cboCustomers.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            keyword = ((JTextField) editor).getText();
        }

        if (keyword == null) {
            return new Customer(0, "Khách lẻ", "");
        }

        keyword = keyword.trim();
        if (keyword.isEmpty() || "Khách lẻ".equalsIgnoreCase(keyword)) {
            return new Customer(0, "Khách lẻ", "");
        }

        String phoneCandidate = keyword;
        if (keyword.contains("-")) {
            String[] parts = keyword.split("-");
            phoneCandidate = parts[parts.length - 1].trim();
        }
        String phoneDigits = phoneCandidate.replaceAll("[^0-9]", "");
        if (!phoneDigits.isEmpty()) {
            Customer byPhone = customerDAO.findByPhone(phoneDigits);
            if (byPhone != null) {
                return byPhone;
            }
        }

        List<Customer> matches = customerDAO.search(keyword);
        for (Customer customer : matches) {
            if (keyword.equalsIgnoreCase(customer.getCustomerName())
                    || keyword.equalsIgnoreCase(customer.toString())) {
                return customer;
            }
        }

        if (matches.size() == 1) {
            return matches.get(0);
        }

        return new Customer(0, "Khách lẻ", "");
    }

    private double parseMoney(String text) {
        try {
            if (text == null)
                return 0;
            return Double.parseDouble(text.replace(",", "").replace(" đ", "").replace(" đồng", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Double) return (Double) obj;
        if (obj instanceof Integer) return ((Integer) obj).doubleValue();
        if (obj instanceof java.math.BigDecimal) return ((java.math.BigDecimal) obj).doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int toInteger(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Double) return ((Double) obj).intValue();
        if (obj instanceof java.math.BigDecimal) return ((java.math.BigDecimal) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}