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

    // Payment Panel
    private JLabel lblSubTotalValue;
    private JLabel lblDiscountValue;
    private JLabel lblTotalValue;

    // Money formatter
    private DecimalFormat df = new DecimalFormat("#,###");

    public PosSalesPanel() {
        initComponent();
        loadDataToTable();
        loadCustomers();
    }

    private void initComponent() {
        setOpaque(false);
        setLayout(new BorderLayout());

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

        add(split, BorderLayout.CENTER);
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

    private void loadCustomers() {
        cboCustomers.removeAllItems();
        List<Customer> customers = customerDAO.searchCustomer("");
        if (customers.isEmpty()) {
            cboCustomers.addItem(new Customer(0, "Khách lẻ", ""));
        } else {
            for (Customer c : customers) {
                cboCustomers.addItem(c);
            }
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
        };
        tableCart = new JTable(modelCart);
        tableCart.setRowHeight(30);
        modelCart.addTableModelListener(e -> {
            if (e.getColumn() == 2)
                updateTotalMoney();
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
        btnPay.addActionListener(e -> {
            new Thread(() -> doPayment()).start();
        });

        paymentContainer.add(paymentSummary, BorderLayout.CENTER);
        paymentContainer.add(btnPay, BorderLayout.SOUTH);

        right.add(paymentContainer, BorderLayout.SOUTH);
        return right;
    }

    private void filterProduct(String keyword) {
        modelProducts.setRowCount(0);
        String key = keyword.toLowerCase();
        for (Product p : productList) {
            if (p.getProductName().toLowerCase().contains(key) || p.getBarcode().toLowerCase().contains(key)) {
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
        double price = (double) modelProducts.getValueAt(selectedRow, 3);
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
            int qty = Integer.parseInt(modelCart.getValueAt(i, 2).toString());
            double price = Double.parseDouble(modelCart.getValueAt(i, 3).toString());
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

        double totalAmount = 0;
        for (int i = 0; i < modelCart.getRowCount(); i++) {
            totalAmount += Double.parseDouble(modelCart.getValueAt(i, 4).toString().replace(",", "").replace(" đ", ""));
        }

        JDialog paymentDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Thanh toán", true);
        paymentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        PaymentPanel pnlPayment = new PaymentPanel(totalAmount, new PaymentPanel.PaymentListener() {
            @Override
            public void onConfirm(String method, double customerPay, double change) {
                paymentDialog.dispose();
                processTransaction(method, customerPay, change);
            }

            @Override
            public void onCancel() {
                paymentDialog.dispose();
            }
        });

        paymentDialog.setContentPane(pnlPayment);
        paymentDialog.setPreferredSize(new Dimension(720, 520));
        paymentDialog.pack();
        paymentDialog.setLocationRelativeTo(this);

        paymentDialog.setVisible(true);
    }


private void processTransaction(String method, double given, double change) {
        try {
            double subTotal = parseMoney(lblSubTotalValue.getText());
            double discount = parseMoney(lblDiscountValue.getText());
            double grandTotal = parseMoney(lblTotalValue.getText());

            SalesInvoice invoice = new SalesInvoice();
            
            Object selectedObj = cboCustomers.getSelectedItem();
            int cusId = 0;
            if (selectedObj instanceof Customer) {
                cusId = ((Customer) selectedObj).getCustomerId();
            }
            invoice.setCustomerId(cusId);
            
            invoice.setCreatedBy(1); 
            invoice.setPaymentMethod(method);
            invoice.setSubTotal(subTotal);  
            invoice.setDiscount(discount);   
            invoice.setGrandTotal(grandTotal);
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
            Payment payment = new Payment(0, method, grandTotal, "Thanh toán POS");
            SalesService service = new SalesService();
            boolean success = service.processSale(invoice, details, payment);

            if (success) {
                JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                new presentation.dialogs.PaymentDialog(parent, grandTotal, change, invoice.getInvId()).setVisible(true);
                new presentation.dialogs.InvoicePrintPreviewDialog(parent, invoice, details, (Customer)selectedObj, given, change).setVisible(true);
                modelCart.setRowCount(0);
                updateTotalMoney();
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Thanh toán thất bại! Có thể do kho không đủ hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    private double parseMoney(String text) {
        try {
            if(text == null) return 0;
            return Double.parseDouble(text.replace(",", "").replace(" đ", "").replace(" đồng", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}