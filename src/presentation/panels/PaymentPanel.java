package presentation.panels;

import bus.PromotionService;
import dal.dao.BankConfigDAO;
import dto.BankConfig;
import dto.Promotion;
import dto.PromotionType;
import dto.SalesInvoiceDetail;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PaymentPanel extends JPanel {

    public interface PaymentListener {
        void onConfirm(String method, double customerPay, double change, double discount, double grandTotal,
                       int redeemedPoints, double pointDiscount);
        void onBack();
    }

    private static final class MethodOption {
        final String code;
        final String label;

        MethodOption(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class PromotionOption {
        final Promotion promotion;
        final String label;

        PromotionOption(Promotion promotion, String label) {
            this.promotion = promotion;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final List<SalesInvoiceDetail> details;
    private final double subTotal;
    private final double baseDiscount;
    private final double baseGrandTotal;
    private final int customerAvailablePoints;
    private final PaymentListener listener;
    private final PromotionService promotionService = new PromotionService();
    private final PromotionService.PromotionChangeListener promotionChangeListener = this::reloadPromotions;

    private double promotionDiscount;
    private double pointDiscount;
    private double totalDiscount;
    private double finalGrandTotal;
    private int redeemedPointsApplied;
    private final List<Integer> cartProductIds;

    private final DecimalFormat moneyFmt = new DecimalFormat("#,###");

    private final JTable tblInvoice = new JTable();
    private DefaultTableModel modelInvoice;

    private final JLabel lblHeaderTotal = new JLabel();
    private final JLabel lblSubTotalValue = new JLabel();
    private final JLabel lblDiscountValue = new JLabel();
    private final JLabel lblGrandTotalValue = new JLabel();

    private final JComboBox<MethodOption> cboMethod = new JComboBox<>(new MethodOption[]{
            new MethodOption("CASH", "Tiền mặt"),
            new MethodOption("CARD", "Thẻ"),
            new MethodOption("TRANSFER", "Ngân hàng (QR)")
    });

    private final JComboBox<PromotionOption> cboPromotion = new JComboBox<>();
    private final JTextField txtRedeemPoints = new JTextField();
    private final JLabel lblRedeemHint = new JLabel();

    private final JTextField txtCustomerPay = new JTextField();
    private final JLabel lblDiffTitle = new JLabel("Tiền thừa");
    private final JLabel lblDiffValue = new JLabel("---");
    private final JButton btnConfirm = new JButton("XÁC NHẬN");

    private final JPanel pnlQuickAdd = new JPanel();
    private final JPanel pnlQrCard = new JPanel(new BorderLayout(8, 8));
    private final JLabel lblQrImage = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblQrInfo = new JLabel(" ");

    // QR cache
    private volatile String qrCacheKey;
    private volatile ImageIcon qrCacheIcon;

    public PaymentPanel(List<SalesInvoiceDetail> details,
                        double subTotal,
                        double discount,
                        double grandTotal,
                        int customerAvailablePoints,
                        PaymentListener listener) {
        this.details = details;
        this.subTotal = subTotal;
        this.baseDiscount = discount;
        this.baseGrandTotal = grandTotal;
        this.customerAvailablePoints = Math.max(0, customerAvailablePoints);
        this.totalDiscount = discount;
        this.finalGrandTotal = grandTotal;
        this.listener = listener;
        this.cartProductIds = extractCartProductIds(details);

        initUI();
        reloadPromotions();
        bindEvents();
        applyMethodUI();   
        updateComputed();  

        PromotionService.addPromotionChangeListener(promotionChangeListener);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !isDisplayable()) {
                PromotionService.removePromotionChangeListener(promotionChangeListener);
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setOpaque(true);

        add(buildHeader(), BorderLayout.NORTH);

        JComponent left = buildInvoicePane();
        JComponent right = buildPaymentPane();

        left.setMinimumSize(new Dimension(520, 0));
        right.setMinimumSize(new Dimension(360, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setBorder(null);
        split.setDividerSize(10);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.65);

        add(split, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JButton btnBack = new JButton("Quay lại");
        btnBack.setFocusable(false);
        btnBack.addActionListener(e -> {
            if (listener != null) listener.onBack();
        });

        JLabel lblTitle = new JLabel("THANH TOÁN", SwingConstants.CENTER);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));

        lblHeaderTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblHeaderTotal.setFont(lblHeaderTotal.getFont().deriveFont(Font.BOLD, 20f));
        lblHeaderTotal.setText(formatMoney(finalGrandTotal));

        header.add(btnBack, BorderLayout.WEST);
        header.add(lblTitle, BorderLayout.CENTER);
        header.add(lblHeaderTotal, BorderLayout.EAST);

        return header;
    }

    private JComponent buildInvoicePane() {
        JPanel left = new JPanel(new BorderLayout(10, 10));
        left.setOpaque(false);

        // Table model
        String[] cols = {"Mã SP", "Tên SP", "Số lượng", "Giá", "Thành tiền"};
        modelInvoice = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 2) return Integer.class;
                if (columnIndex == 3 || columnIndex == 4) return Double.class;
                return Object.class;
            }
        };

        if (details != null) {
            for (SalesInvoiceDetail d : details) {
                int pid = d.getProductId();
                String name = d.getProductName();
                if (name == null || name.trim().isEmpty()) {
                    name = "SP #" + pid;
                }
                int qty = d.getQty();
                double price = d.getUnitPrice();
                double lineTotal = qty * price;

                modelInvoice.addRow(new Object[]{pid, name, qty, price, lineTotal});
            }
        }

        tblInvoice.setModel(modelInvoice);
        tblInvoice.setRowHeight(28);
        tblInvoice.setFillsViewportHeight(true);
        tblInvoice.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblInvoice.getTableHeader().setBackground(new Color(33, 150, 243));
        tblInvoice.getTableHeader().setForeground(Color.WHITE);
        tblInvoice.getTableHeader().setOpaque(true);
        tblInvoice.getTableHeader().setReorderingAllowed(false);
        tblInvoice.getTableHeader().setResizingAllowed(false);

        // Render money columns
        DefaultTableCellRenderer rightMoney = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = moneyFmt.format(((Number) value).doubleValue());
                }
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        };
        tblInvoice.getColumnModel().getColumn(3).setCellRenderer(rightMoney);
        tblInvoice.getColumnModel().getColumn(4).setCellRenderer(rightMoney);

        // Qty align center
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblInvoice.getColumnModel().getColumn(2).setCellRenderer(center);

        JScrollPane sp = new JScrollPane(tblInvoice);
        sp.setBorder(cardBorder("Chi tiết hóa đơn"));

        left.add(sp, BorderLayout.CENTER);
        left.add(buildTotalsCard(), BorderLayout.SOUTH);

        return left;
    }

    private JComponent buildTotalsCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(cardBorder(null));
        card.setOpaque(false);

        lblSubTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDiscountValue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblGrandTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);

        lblSubTotalValue.setText(formatMoney(subTotal));
        lblDiscountValue.setText(formatMoney(totalDiscount));
        lblGrandTotalValue.setText(formatMoney(finalGrandTotal));

        lblGrandTotalValue.setFont(lblGrandTotalValue.getFont().deriveFont(Font.BOLD, 18f));
        lblGrandTotalValue.setForeground(Color.RED);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        card.add(new JLabel("Tổng trước giảm:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(lblSubTotalValue, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        card.add(new JLabel("Tiền giảm giá:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(lblDiscountValue, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel("Tổng sau giảm:");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        card.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(lblGrandTotalValue, gbc);

        return card;
    }

    private JComponent buildPaymentPane() {
        JPanel right = new JPanel(new BorderLayout(10, 10));
        right.setOpaque(false);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildPaymentFormCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildQrCard());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        right.add(scroll, BorderLayout.CENTER);
        return right;
    }

    private JComponent buildPaymentFormCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(cardBorder("Thông tin thanh toán"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        // Method
        card.add(new JLabel("Phương thức"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cboMethod.setPreferredSize(new Dimension(220, 34));
        card.add(cboMethod, gbc);

        // Promotion
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        card.add(new JLabel("Khuyến mãi"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cboPromotion.setPreferredSize(new Dimension(220, 34));
        card.add(cboPromotion, gbc);

        // Redeem points
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        card.add(new JLabel("Đổi điểm"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtRedeemPoints.setPreferredSize(new Dimension(220, 34));
        txtRedeemPoints.setHorizontalAlignment(SwingConstants.RIGHT);
        txtRedeemPoints.setText("0");
        txtRedeemPoints.setEnabled(customerAvailablePoints > 0);
        card.add(txtRedeemPoints, gbc);

        gbc.gridy++;
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lblRedeemHint.setFont(lblRedeemHint.getFont().deriveFont(Font.PLAIN, 12f));
        lblRedeemHint.setForeground(new Color(80, 80, 80));
        card.add(lblRedeemHint, gbc);

        // Customer pay
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        card.add(new JLabel("Khách đưa"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtCustomerPay.setPreferredSize(new Dimension(220, 34));
        txtCustomerPay.setHorizontalAlignment(SwingConstants.RIGHT);
        txtCustomerPay.setFont(txtCustomerPay.getFont().deriveFont(15f));
        txtCustomerPay.setText(moneyFmt.format(Math.round(finalGrandTotal)));
        card.add(txtCustomerPay, gbc);

        // Diff 
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        lblDiffTitle.setFont(lblDiffTitle.getFont().deriveFont(Font.BOLD));
        card.add(lblDiffTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblDiffValue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDiffValue.setFont(lblDiffValue.getFont().deriveFont(Font.BOLD, 15f));
        card.add(lblDiffValue, gbc);

        // Quick add 
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        pnlQuickAdd.setOpaque(false);
        pnlQuickAdd.setLayout(new BorderLayout(6, 6));

        JLabel quickLbl = new JLabel("Thêm tiền (nghìn)");
        quickLbl.setFont(quickLbl.getFont().deriveFont(Font.PLAIN, 12f));

        pnlQuickAdd.add(quickLbl, BorderLayout.NORTH);
        pnlQuickAdd.add(buildQuickButtons(), BorderLayout.CENTER);

        card.add(pnlQuickAdd, gbc);

        return card;
    }

    private JComponent buildQrCard() {
        pnlQrCard.setBorder(cardBorder("Ngân hàng (QR)"));
        pnlQrCard.setOpaque(false);

        lblQrImage.setPreferredSize(new Dimension(320, 320));
        lblQrImage.setBorder(new LineBorder(new Color(0, 0, 0, 30), 1, true));

        lblQrInfo.setFont(lblQrInfo.getFont().deriveFont(Font.PLAIN, 12f));
        lblQrInfo.setForeground(new Color(80, 80, 80));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(new JLabel("Quét mã để chuyển khoản đúng số tiền."), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(lblQrImage, BorderLayout.CENTER);
        center.add(lblQrInfo, BorderLayout.SOUTH);

        pnlQrCard.removeAll();
        pnlQrCard.add(top, BorderLayout.NORTH);
        pnlQrCard.add(center, BorderLayout.CENTER);

        pnlQrCard.setVisible(false);
        return pnlQrCard;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        btnConfirm.setPreferredSize(new Dimension(160, 36));
        btnConfirm.setFont(btnConfirm.getFont().deriveFont(Font.BOLD));
        btnConfirm.setBackground(new Color(76, 175, 80));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setOpaque(true);

        footer.add(btnConfirm);
        return footer;
    }

    private JPanel buildQuickButtons() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.setOpaque(false);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);

        row1.add(buildAddBtn("+1", 1_000));
        row1.add(buildAddBtn("+2", 2_000));
        row1.add(buildAddBtn("+5", 5_000));
        row1.add(buildAddBtn("+10", 10_000));
        row1.add(buildAddBtn("+20", 20_000));

        row2.add(buildAddBtn("+50", 50_000));
        row2.add(buildAddBtn("+100", 100_000));
        row2.add(buildAddBtn("+200", 200_000));
        row2.add(buildAddBtn("+500", 500_000));
        row2.add(buildAddBtn("Reset", -1_000_000_000));

        container.add(row1);
        container.add(Box.createVerticalStrut(6));
        container.add(row2);

        return container;
    }

    private JButton buildAddBtn(String label, long addAmount) {
        JButton btn = new JButton(label);
        btn.setFocusable(false);
        btn.addActionListener(e -> {
            long base = parseMoneyToLong(txtCustomerPay.getText());
            long newValue = Math.max(0, base + addAmount);
            txtCustomerPay.setText(moneyFmt.format(newValue));
            txtCustomerPay.requestFocusInWindow();
            updateComputed();
        });
        return btn;
    }

    private CompoundBorder cardBorder(String title) {
        LineBorder line = new LineBorder(new Color(0, 0, 0, 30), 1, true);
        EmptyBorder pad = new EmptyBorder(10, 10, 10, 10);
        if (title == null || title.trim().isEmpty()) {
            return new CompoundBorder(line, pad);
        }
        return new CompoundBorder(BorderFactory.createTitledBorder(line, title), pad);
    }

    private void bindEvents() {
        cboMethod.addActionListener(e -> {
            applyMethodUI();
            updateComputed();
        });

        cboPromotion.addActionListener(e -> applySelectedPromotion());

        txtRedeemPoints.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applySelectedPromotion(); }
            @Override public void removeUpdate(DocumentEvent e) { applySelectedPromotion(); }
            @Override public void changedUpdate(DocumentEvent e) { applySelectedPromotion(); }
        });

        txtCustomerPay.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateComputed(); }
            @Override public void removeUpdate(DocumentEvent e) { updateComputed(); }
            @Override public void changedUpdate(DocumentEvent e) { updateComputed(); }
        });

        btnConfirm.addActionListener(e -> confirm());

        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back", () -> {
            if (listener != null) listener.onBack();
        });

        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm", this::confirm);

        txtCustomerPay.addActionListener(e -> confirm());
    }

    private void bindKey(KeyStroke ks, String name, Runnable action) {
        InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        im.put(ks, name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void applyMethodUI() {
        String method = getSelectedMethodCode();

        boolean isCash = "CASH".equals(method);
        boolean isTransfer = "TRANSFER".equals(method);

        txtCustomerPay.setEditable(isCash);
        pnlQuickAdd.setVisible(isCash);

        if (!isCash) {
            txtCustomerPay.setText(moneyFmt.format(Math.round(finalGrandTotal)));
        }

        pnlQrCard.setVisible(isTransfer);
        if (isTransfer) {
            loadQrCodeAsync();
        } else {
            lblQrImage.setIcon(null);
            lblQrImage.setText(" ");
            lblQrInfo.setText(" ");
        }

        revalidate();
        repaint();
    }

    private void updateComputed() {
        String method = getSelectedMethodCode();

        double pay = "CASH".equals(method) ? parseMoneyToLong(txtCustomerPay.getText()) : finalGrandTotal;
        double diff = pay - finalGrandTotal;

        if (diff >= 0) {
            lblDiffTitle.setText("Tiền thừa");
            lblDiffValue.setText(formatMoney(diff));
            lblDiffValue.setForeground(new Color(0, 120, 60));
        } else {
            lblDiffTitle.setText("Còn thiếu");
            lblDiffValue.setText(formatMoney(-diff));
            lblDiffValue.setForeground(new Color(176, 0, 32));
        }

        boolean valid = !"CASH".equals(method) || diff >= 0;
        btnConfirm.setEnabled(valid);
    }

    private void confirm() {
        if (!btnConfirm.isEnabled()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        String method = getSelectedMethodCode();

        if ("CASH".equals(method)) {
            long given = parseMoneyToLong(txtCustomerPay.getText());
            if (given <= 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền khách đưa.");
                txtCustomerPay.requestFocusInWindow();
                return;
            }

            if (given < finalGrandTotal) {
                long thiếu = (long) Math.ceil(finalGrandTotal - given);
                JOptionPane.showMessageDialog(this, "Khách đưa chưa đủ. Còn thiếu " + moneyFmt.format(thiếu) + " đ");
                txtCustomerPay.requestFocusInWindow();
                return;
            }

            double change = given - finalGrandTotal;
            if (listener != null) {
                listener.onConfirm(method, given, change, totalDiscount, finalGrandTotal, redeemedPointsApplied,
                        pointDiscount);
            }
            return;
        }

        if (listener != null) {
            listener.onConfirm(method, finalGrandTotal, 0, totalDiscount, finalGrandTotal, redeemedPointsApplied,
                    pointDiscount);
        }
    }

    private void reloadPromotions() {
        Runnable action = () -> {
            PromotionOption previous = (PromotionOption) cboPromotion.getSelectedItem();
            Integer previousId = previous != null && previous.promotion != null ? previous.promotion.getPromoId() : null;

            cboPromotion.removeAllItems();
            cboPromotion.addItem(new PromotionOption(null, "Không áp dụng"));

            List<Promotion> promotions = promotionService.getApplicablePromotions(baseGrandTotal, cartProductIds);
            for (Promotion promotion : promotions) {
                cboPromotion.addItem(new PromotionOption(promotion, formatPromotionLabel(promotion)));
            }

            if (previousId != null) {
                for (int i = 0; i < cboPromotion.getItemCount(); i++) {
                    PromotionOption option = cboPromotion.getItemAt(i);
                    if (option != null && option.promotion != null && option.promotion.getPromoId() == previousId) {
                        cboPromotion.setSelectedIndex(i);
                        break;
                    }
                }
            }

            if (cboPromotion.getSelectedItem() == null && cboPromotion.getItemCount() > 0) {
                cboPromotion.setSelectedIndex(0);
            }

            applySelectedPromotion();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    private void applySelectedPromotion() {
        PromotionOption selected = (PromotionOption) cboPromotion.getSelectedItem();
        Promotion promotion = selected != null ? selected.promotion : null;

        promotionDiscount = calculatePromotionDiscount(promotion, baseGrandTotal);
        int requestedPoints = (int) parseMoneyToLong(txtRedeemPoints.getText());
        redeemedPointsApplied = normalizeRedeemPoints(requestedPoints);

        double amountAfterPromotion = Math.max(0, subTotal - (baseDiscount + promotionDiscount));
        pointDiscount = calculatePointDiscount(redeemedPointsApplied, amountAfterPromotion);
        totalDiscount = Math.max(0, baseDiscount + promotionDiscount + pointDiscount);
        finalGrandTotal = Math.max(0, subTotal - totalDiscount);

        int pointPercent = (redeemedPointsApplied / 10) * 5;
        String rawDigits = String.valueOf(redeemedPointsApplied);
        if (!txtRedeemPoints.getText().replaceAll("[^0-9]", "").equals(rawDigits)) {
            txtRedeemPoints.setText(rawDigits);
        }
        lblRedeemHint.setText("Điểm hiện có: " + customerAvailablePoints
                + " • Đang dùng: " + redeemedPointsApplied
                + " điểm (" + pointPercent + "%)");

        lblSubTotalValue.setText(formatMoney(subTotal));
        lblDiscountValue.setText(formatMoney(totalDiscount));
        lblGrandTotalValue.setText(formatMoney(finalGrandTotal));
        lblHeaderTotal.setText(formatMoney(finalGrandTotal));

        if (!"CASH".equals(getSelectedMethodCode())) {
            txtCustomerPay.setText(moneyFmt.format(Math.round(finalGrandTotal)));
        }

        applyMethodUI();
        updateComputed();
    }

    private int normalizeRedeemPoints(int requestedPoints) {
        if (customerAvailablePoints <= 0 || requestedPoints <= 0) {
            return 0;
        }
        int capped = Math.min(requestedPoints, customerAvailablePoints);
        return (capped / 10) * 10;
    }

    private double calculatePointDiscount(int redeemedPoints, double amount) {
        if (redeemedPoints <= 0 || amount <= 0) {
            return 0;
        }
        int percent = (redeemedPoints / 10) * 5;
        if (percent > 100) {
            percent = 100;
        }
        return amount * (percent / 100.0);
    }

    private double calculatePromotionDiscount(Promotion promotion, double amount) {
        if (promotion == null || promotion.getType() == null || promotion.getValue() == null) {
            return 0;
        }

        double rawDiscount;
        if (promotion.getType() == PromotionType.PERCENT) {
            rawDiscount = amount * (promotion.getValue().doubleValue() / 100.0);
        } else {
            rawDiscount = promotion.getValue().doubleValue();
        }

        if (rawDiscount < 0) {
            rawDiscount = 0;
        }

        return Math.min(rawDiscount, Math.max(0, amount));
    }

    private String formatPromotionLabel(Promotion promotion) {
        if (promotion == null) {
            return "Không áp dụng";
        }

        String code = safe(promotion.getPromoCode());
        String name = safe(promotion.getPromoName());

        String valueText;
        if (promotion.getType() == PromotionType.PERCENT) {
            valueText = trimBigDecimal(promotion.getValue()) + "%";
        } else {
            valueText = moneyFmt.format(promotion.getValue() != null ? promotion.getValue().doubleValue() : 0) + " đ";
        }

        return code + " - " + name + " (" + valueText + ")";
    }

    private String trimBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private List<Integer> extractCartProductIds(List<SalesInvoiceDetail> invoiceDetails) {
        List<Integer> ids = new ArrayList<>();
        if (invoiceDetails == null) {
            return ids;
        }
        for (SalesInvoiceDetail detail : invoiceDetails) {
            if (detail != null && detail.getProductId() > 0) {
                ids.add(detail.getProductId());
            }
        }
        return ids;
    }

    private String getSelectedMethodCode() {
        Object obj = cboMethod.getSelectedItem();
        if (obj instanceof MethodOption) {
            return ((MethodOption) obj).code;
        }
        return "CASH";
    }

    private void loadQrCodeAsync() {
        final long amount = Math.max(0, Math.round(finalGrandTotal));
        final String key = "amount=" + amount;
        // Cache
        if (key.equals(qrCacheKey) && qrCacheIcon != null) {
            lblQrImage.setText(" ");
            lblQrImage.setIcon(qrCacheIcon);
            return;
        }

        lblQrImage.setIcon(null);
        lblQrImage.setText("Đang tạo mã QR...");
        lblQrInfo.setText(" ");

        new Thread(() -> {
            try {
                BankConfig config = new BankConfigDAO().getFirstConfig();
                if (config == null) {
                    throw new Exception("Chưa cấu hình tài khoản ngân hàng (bankconfig).");
                }

                String bankId = safe(config.getBankId());
                String accountNumber = safe(config.getaccountNumber());
                String template = safe(config.getTemplate());
                if (template.isEmpty()) template = "compact";

                if (bankId.isEmpty() || accountNumber.isEmpty()) {
                    throw new Exception("Thiếu bankId hoặc số tài khoản trong bankconfig.");
                }

                String addInfo = URLEncoder.encode("Thanh toan POS", StandardCharsets.UTF_8);
                String accName = safe(config.getAccountName());
                String accNameEnc = URLEncoder.encode(accName, StandardCharsets.UTF_8);

                String url = String.format(
                        "https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s",
                        bankId, accountNumber, template, String.valueOf(amount), addInfo, accNameEnc
                );

                BufferedImage img = ImageIO.read(new URL(url));
                if (img == null) {
                    throw new Exception("Không tải được hình QR (image null).");
                }

                Image scaled = img.getScaledInstance(320, 320, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaled);

                qrCacheKey = key;
                qrCacheIcon = icon;

                String infoLine = "STK: " + accountNumber + (accName.isEmpty() ? "" : (" • " + accName));

                SwingUtilities.invokeLater(() -> {
                    lblQrImage.setText(" ");
                    lblQrImage.setIcon(icon);
                    lblQrInfo.setText(infoLine);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblQrImage.setIcon(null);
                    lblQrImage.setText("<html><div style='text-align:center'>Lỗi tạo QR<br/>"
                            + escapeHtml(ex.getMessage()) + "</div></html>");
                    lblQrInfo.setText(" ");
                });
            }
        }, "qr-loader").start();
    }


    private String formatMoney(double amount) {
        long v = Math.round(amount);
        return moneyFmt.format(Math.max(0, v)) + " đ";
    }

    private long parseMoneyToLong(String input) {
        if (input == null) return 0;
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
