package presentation.panels;

import dal.dao.BankConfigDAO;
import dto.BankConfig;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;


public class PaymentPanel extends JPanel {
    public interface PaymentListener {
        void onConfirm(String method, double customerPay, double change);
        void onCancel();
    }

    private final double totalAmount;
    private final PaymentListener listener;
    private final DecimalFormat moneyFmt = new DecimalFormat("#,###");
    private final JComboBox<String> cboMethod = new JComboBox<>(new String[]{"CASH", "CARD", "TRANSFER"});
    private final JTextField txtCustomerPay = new JTextField();
    private final JLabel lblTotalValue = new JLabel();
    private final JLabel lblChangeTitle = new JLabel("Tiền thừa");
    private final JLabel lblChangeValue = new JLabel("---");
    private final JPanel pnlQuickAdd = new JPanel();
    private final JPanel pnlQrCard = new JPanel(new BorderLayout(8, 8));
    private final JLabel lblQrImage = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblQrInfo = new JLabel(" ");
    private BufferedImage qrCached;
    private String qrCacheKey;

    public PaymentPanel(double totalAmount, PaymentListener listener) {
        this.totalAmount = totalAmount;
        this.listener = listener;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        setPreferredSize(new Dimension(720, 520));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        cboMethod.addActionListener(e -> applyMethodUI());
        txtCustomerPay.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateChangePreview();
            }
        });

        bindKeys();

        lblTotalValue.setText(moneyFmt.format(Math.round(totalAmount)) + " đ");
        cboMethod.setSelectedItem("CASH");
        applyMethodUI();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setOpaque(false);

        JLabel title = new JLabel("THANH TOÁN");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setOpaque(false);

        JLabel totalLbl = new JLabel("Tổng cần thanh toán");
        totalLbl.setFont(totalLbl.getFont().deriveFont(Font.PLAIN, 12f));

        lblTotalValue.setFont(lblTotalValue.getFont().deriveFont(Font.BOLD, 26f));

        totalBox.add(totalLbl, BorderLayout.NORTH);
        totalBox.add(lblTotalValue, BorderLayout.CENTER);

        header.add(title, BorderLayout.WEST);
        header.add(totalBox, BorderLayout.EAST);

        return header;
    }

    private JComponent buildContent() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);

        JPanel formCard = buildCard();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        formCard.add(new JLabel("Phương thức"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cboMethod.setPreferredSize(new Dimension(180, 34));
        formCard.add(cboMethod, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        formCard.add(new JLabel("Khách đưa"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtCustomerPay.setPreferredSize(new Dimension(180, 34));
        formCard.add(txtCustomerPay, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        formCard.add(lblChangeTitle, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        lblChangeValue.setFont(lblChangeValue.getFont().deriveFont(Font.BOLD, 16f));
        formCard.add(lblChangeValue, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        pnlQuickAdd.setOpaque(false);
        pnlQuickAdd.setLayout(new BorderLayout(6, 6));

        JLabel quickLbl = new JLabel("Thêm tiền (nghìn)");
        quickLbl.setFont(quickLbl.getFont().deriveFont(Font.PLAIN, 12f));

        pnlQuickAdd.add(quickLbl, BorderLayout.NORTH);
        pnlQuickAdd.add(buildQuickButtons(), BorderLayout.CENTER);

        formCard.add(pnlQuickAdd, gbc);

        pnlQrCard.setBorder(cardBorder());
        pnlQrCard.setPreferredSize(new Dimension(320, 0));

        JLabel qrTitle = new JLabel("QUÉT MÃ QR");
        qrTitle.setFont(qrTitle.getFont().deriveFont(Font.BOLD, 14f));

        lblQrImage.setPreferredSize(new Dimension(280, 280));
        lblQrImage.setBorder(new LineBorder(new Color(0, 0, 0, 30), 1, true));
        lblQrInfo.setFont(lblQrInfo.getFont().deriveFont(Font.PLAIN, 12f));

        JPanel qrTop = new JPanel(new BorderLayout());
        qrTop.setOpaque(false);
        qrTop.add(qrTitle, BorderLayout.NORTH);
        qrTop.add(new JLabel("Chuyển khoản đúng số tiền để hệ thống ghi nhận nhanh hơn."), BorderLayout.CENTER);

        JPanel qrCenter = new JPanel(new BorderLayout(8, 8));
        qrCenter.setOpaque(false);
        qrCenter.add(lblQrImage, BorderLayout.CENTER);
        qrCenter.add(lblQrInfo, BorderLayout.SOUTH);

        pnlQrCard.setLayout(new BorderLayout(8, 8));
        pnlQrCard.add(qrTop, BorderLayout.NORTH);
        pnlQrCard.add(qrCenter, BorderLayout.CENTER);

        content.add(formCard, BorderLayout.CENTER);
        content.add(pnlQrCard, BorderLayout.EAST);

        return content;
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

        container.add(row1);
        container.add(Box.createVerticalStrut(6));
        container.add(row2);

        return container;
    }

    private JButton buildAddBtn(String label, long addAmount) {
        JButton btn = new JButton(label);
        btn.setFocusable(false);
        btn.addActionListener(e -> {
            Long current = tryParseMoney(txtCustomerPay.getText());
            long base = (current == null) ? 0 : current;
            long newValue = base + addAmount;
            txtCustomerPay.setText(moneyFmt.format(newValue));
            txtCustomerPay.requestFocusInWindow();
            updateChangePreview();
        });
        return btn;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton btnCancel = new JButton("Hủy bỏ (ESC)");
        JButton btnConfirm = new JButton("XÁC NHẬN (ENTER)");

        btnCancel.addActionListener(e -> {
            if (listener != null) listener.onCancel();
        });

        btnConfirm.addActionListener(e -> onConfirm());
        txtCustomerPay.addActionListener(e -> onConfirm());

        footer.add(btnCancel);
        footer.add(btnConfirm);

        return footer;
    }

    private JPanel buildCard() {
        JPanel p = new JPanel();
        p.setBorder(cardBorder());
        p.setOpaque(true);
        return p;
    }

    private CompoundBorder cardBorder() {
        return new CompoundBorder(
                new LineBorder(new Color(0, 0, 0, 30), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        );
    }

    private void bindKeys() {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) listener.onCancel();
            }
        });

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
        getActionMap().put("confirm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onConfirm();
            }
        });
    }

    private void applyMethodUI() {
        String method = (String) cboMethod.getSelectedItem();
        if (method == null) method = "CASH";

        boolean isCash = "CASH".equals(method);
        boolean isTransfer = "TRANSFER".equals(method);

        txtCustomerPay.setEditable(isCash);
        pnlQuickAdd.setVisible(isCash);

        pnlQrCard.setVisible(isTransfer);

        if (!isCash) {
            txtCustomerPay.setText(moneyFmt.format(Math.round(totalAmount)));
        } else {
            if (txtCustomerPay.getText() == null) txtCustomerPay.setText("");
        }

        updateChangePreview();

        if (isTransfer) {
            loadQrCodeAsync();
        } else {
            lblQrImage.setIcon(null);
            lblQrImage.setText(" ");
            lblQrInfo.setText(" ");
        }
    }

    private void updateChangePreview() {
        Long given = tryParseMoney(txtCustomerPay.getText());
        if (given == null) {
            lblChangeTitle.setText("Tiền thừa");
            lblChangeValue.setText("---");
            return;
        }

        double diff = given - totalAmount;

        if (diff >= 0) {
            lblChangeTitle.setText("Tiền thừa");
            lblChangeValue.setText(moneyFmt.format(Math.round(diff)) + " đ");
        } else {
            lblChangeTitle.setText("Còn thiếu");
            lblChangeValue.setText(moneyFmt.format(Math.round(-diff)) + " đ");
        }
    }

    private void onConfirm() {
        String method = (String) cboMethod.getSelectedItem();
        if (method == null) method = "CASH";

        if ("CASH".equals(method)) {
            Long given = tryParseMoney(txtCustomerPay.getText());
            if (given == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền khách đưa.");
                txtCustomerPay.requestFocusInWindow();
                return;
            }

            if (given < totalAmount) {
                long thiếu = (long) Math.ceil(totalAmount - given);
                JOptionPane.showMessageDialog(this, "Khách đưa chưa đủ. Còn thiếu " + moneyFmt.format(thiếu) + " đ");
                txtCustomerPay.requestFocusInWindow();
                return;
            }

            double change = given - totalAmount;
            if (listener != null) {
                listener.onConfirm(method, given.doubleValue(), change);
            }
        } else {
            if (listener != null) {
                listener.onConfirm(method, totalAmount, 0);
            }
        }
    }

    private Long tryParseMoney(String input) {
        if (input == null) return null;
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void loadQrCodeAsync() {
        BankConfig config = new BankConfigDAO().getFirstConfig();
        if (config == null || config.getBankId() == null || config.getBankId().trim().isEmpty()
                || config.getaccountNumber() == null || config.getaccountNumber().trim().isEmpty()
                || config.getTemplate() == null || config.getTemplate().trim().isEmpty()) {
            lblQrImage.setIcon(null);
            lblQrImage.setText("Chưa cấu hình QR");
            lblQrInfo.setText("Vui lòng cấu hình bankconfig trong DB.");
            return;
        }

        long amount = Math.round(totalAmount);
        String addInfo = "Thanh toán POS";
        String accountName = (config.getAccountName() == null) ? "" : config.getAccountName();

        String url = buildVietQrUrl(config.getBankId(), config.getaccountNumber(), config.getTemplate(), amount, addInfo, accountName);

        String cacheKey = url;
        if (cacheKey.equals(qrCacheKey) && qrCached != null) {
            setQrImage(qrCached, config);
            return;
        }

        qrCacheKey = cacheKey;
        lblQrImage.setIcon(null);
        lblQrImage.setText("Đang tải QR...");
        lblQrInfo.setText(" ");

        new Thread(() -> {
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                if (img == null) throw new RuntimeException("Không tải được QR");

                qrCached = img;

                SwingUtilities.invokeLater(() -> setQrImage(img, config));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblQrImage.setIcon(null);
                    lblQrImage.setText("Không tải được QR");
                    lblQrInfo.setText(ex.getMessage() == null ? "" : ex.getMessage());
                });
            }
        }, "vietqr-loader").start();
    }

    private void setQrImage(BufferedImage img, BankConfig config) {
        int w = 280;
        int h = 280;
        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        lblQrImage.setText(" ");
        lblQrImage.setIcon(new ImageIcon(scaled));

        String info = "STK: " + config.getaccountNumber()
                + " • Tên: " + (config.getAccountName() == null ? "" : config.getAccountName());
        lblQrInfo.setText(info);
    }

    private String buildVietQrUrl(String bankId, String accountNumber, String template, long amount, String addInfo, String accountName) {
        String base = "https://img.vietqr.io/image/" + bankId + "-" + accountNumber + "-" + template + ".png";
        String qAddInfo = URLEncoder.encode(addInfo == null ? "" : addInfo, StandardCharsets.UTF_8);
        String qName = URLEncoder.encode(accountName == null ? "" : accountName, StandardCharsets.UTF_8);

        return base
                + "?amount=" + amount
                + "&addInfo=" + qAddInfo
                + "&accountName=" + qName;
    }
}
