package presentation;

import bus.AuthService;
import bus.AuthService.AuthUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

public class LoginFrame extends JFrame {

    // ===== UI constants =====
    private static final int CARD_WIDTH = 520;
    private static final int FIELD_WIDTH = 420;
    private static final int FIELD_HEIGHT = 40;
    private static final int BTN_HEIGHT = 46;

    // Logo max size (giữ tỉ lệ)
    private static final int LOGO_MAX_W = 300;
    private static final int LOGO_MAX_H = 170;

    // ===== Components =====
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JCheckBox chkShowPassword = new JCheckBox("Hiện mật khẩu");
    private final JButton btnLogin = new JButton("Đăng nhập");

    private final JLabel lblUserError = new JLabel();
    private final JLabel lblPassError = new JLabel();

    private final JLabel lblStatus = new JLabel();
    private final JLabel lblCaps = new JLabel();

    // ===== Services =====
    private final AuthService authService = new AuthService();

    // ===== Runtime =====
    private char defaultEcho;
    private boolean userHasTypedUsername = false;
    private boolean userHasTypedPassword = false;

    // ===== Colors =====
    private final Color bg = new Color(245, 246, 248);
    private final Color cardBorder = new Color(225, 228, 234);
    private final Color textMuted = new Color(110, 110, 110);
    private final Color labelColor = new Color(60, 60, 60);
    private final Color errorColor = new Color(200, 0, 0);
    private final Color primary = new Color(30, 90, 255);

    public LoginFrame() {
        setTitle("MINI SUPERMARKET - Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        setContentPane(buildUI());
        wireEvents();

        getRootPane().setDefaultButton(btnLogin);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        root.setBackground(bg);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cardBorder, 1, true),
                new EmptyBorder(22, 32, 22, 32)
        ));
        card.setMaximumSize(new Dimension(CARD_WIDTH, Integer.MAX_VALUE));

        // ===== Logo =====
        JLabel logo = buildLogoLabel(LOGO_MAX_W, LOGO_MAX_H);
        card.add(logo);
        card.add(Box.createVerticalStrut(10));

        // ===== Title =====
        JLabel title = new JLabel("MINI SUPERMARKET");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel subtitle = new JLabel("Đăng nhập vào hệ thống");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        // ===== Form panel (CENTER, fixed width = FIELD_WIDTH) =====
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.setMaximumSize(new Dimension(FIELD_WIDTH, Integer.MAX_VALUE));

        // Prepare fields
        prepareTextField(txtUsername);
        preparePasswordField(txtPassword);

        // Placeholder (FlatLaf)
        txtUsername.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập");
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");

        txtUsername.putClientProperty("JComponent.hideFocusRing", true);
        txtPassword.putClientProperty("JComponent.hideFocusRing", true);

        // Fields
        form.add(buildLabeledFieldInForm("Tên đăng nhập", txtUsername, lblUserError));
        form.add(Box.createVerticalStrut(12));
        form.add(buildLabeledFieldInForm("Mật khẩu", txtPassword, lblPassError));
        form.add(Box.createVerticalStrut(10));

        // Caps warning
        styleCapsLabel(lblCaps);
        form.add(lblCaps);
        form.add(Box.createVerticalStrut(8));

        // Show password
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setForeground(new Color(90, 90, 90));
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chkShowPassword);

        form.add(Box.createVerticalStrut(16));

        // Button
        stylePrimaryButton(btnLogin);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(btnLogin);

        form.add(Box.createVerticalStrut(10));

        // Status
        styleStatusLabel(lblStatus);
        form.add(lblStatus);

        card.add(form);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(card);

        root.add(wrap);
        return root;
    }

    // Field wrapper nằm trong form fixed-width => không bị lệch
    private JComponent buildLabeledFieldInForm(String label, JComponent field, JLabel errorLabel) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(FIELD_WIDTH, Integer.MAX_VALUE));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(labelColor);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension size = new Dimension(FIELD_WIDTH, FIELD_HEIGHT);
        field.setPreferredSize(size);
        field.setMaximumSize(size);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleErrorLabel(errorLabel);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrap.add(l);
        wrap.add(Box.createVerticalStrut(4));
        wrap.add(field);
        wrap.add(Box.createVerticalStrut(4));
        wrap.add(errorLabel);

        return wrap;
    }

    private void prepareTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.putClientProperty("JTextField.margin", new Insets(8, 10, 8, 10));
        field.putClientProperty("JComponent.roundRect", true);
    }

    private void preparePasswordField(JPasswordField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.putClientProperty("JTextField.margin", new Insets(8, 10, 8, 10));
        field.putClientProperty("JComponent.roundRect", true);
    }

    private void stylePrimaryButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.setOpaque(false);
        b.setContentAreaFilled(true);

        b.setMargin(new Insets(10, 12, 10, 12));
        b.setBorderPainted(false);

        b.setBackground(primary);
        b.setForeground(Color.WHITE);

        b.setPreferredSize(new Dimension(FIELD_WIDTH, BTN_HEIGHT));
        b.setMaximumSize(new Dimension(FIELD_WIDTH, BTN_HEIGHT));

        b.putClientProperty("JComponent.arc", 28);
    }

    private void styleErrorLabel(JLabel lb) {
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(errorColor);
        lb.setText(" ");
        lb.setVisible(false);
    }

    private void styleStatusLabel(JLabel lb) {
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(errorColor);
        lb.setText(" ");
        lb.setVisible(false);
    }

    private void styleCapsLabel(JLabel lb) {
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(new Color(180, 90, 0));
        lb.setText(" ");
        lb.setVisible(false);
    }

    private void wireEvents() {
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(e -> doLogin());
        btnLogin.addActionListener(e -> doLogin());

        defaultEcho = txtPassword.getEchoChar();
        chkShowPassword.addActionListener(e -> {
            txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : defaultEcho);
            txtPassword.requestFocusInWindow();
        });

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { updateCapsLockWarning(true); }
            @Override public void focusLost(FocusEvent e) { updateCapsLockWarning(false); }
        });

        installLiveValidationUsername();
        installLiveValidationPassword();
    }

    private void installLiveValidationUsername() {
        txtUsername.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            userHasTypedUsername = !safeTrim(txtUsername.getText()).isEmpty();

            clearFieldError(txtUsername, lblUserError);
            clearStatus();

            txtUsername.putClientProperty("JComponent.hideFocusRing", !userHasTypedUsername);
        }));

        txtUsername.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtUsername.putClientProperty("JComponent.hideFocusRing", !userHasTypedUsername
                        && !"error".equals(txtUsername.getClientProperty("JComponent.outline")));
            }
        });
    }

    private void installLiveValidationPassword() {
        txtPassword.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            userHasTypedPassword = txtPassword.getPassword().length > 0;

            clearFieldError(txtPassword, lblPassError);
            clearStatus();
            updateCapsLockWarning(txtPassword.isFocusOwner());

            txtPassword.putClientProperty("JComponent.hideFocusRing", !userHasTypedPassword);
        }));

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtPassword.putClientProperty("JComponent.hideFocusRing", !userHasTypedPassword
                        && !"error".equals(txtPassword.getClientProperty("JComponent.outline")));
            }
        });
    }

    private void doLogin() {
        clearAllErrors();

        String username = safeTrim(txtUsername.getText());
        String password = safeTrim(new String(txtPassword.getPassword()));

        boolean ok = true;

        if (username.isEmpty()) {
            showFieldError(txtUsername, lblUserError, "Vui lòng nhập tên đăng nhập.");
            ok = false;
        }
        if (password.isEmpty()) {
            showFieldError(txtPassword, lblPassError, "Vui lòng nhập mật khẩu.");
            ok = false;
        }
        if (!ok) return;

        setLoading(true);

        new SwingWorker<AuthUser, Void>() {
            @Override
            protected AuthUser doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    AuthUser user = get();
                    if (user == null) {
                        setStatus("Sai tài khoản/mật khẩu hoặc tài khoản bị khóa.");
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Sai tài khoản/mật khẩu hoặc tài khoản bị khóa.",
                                "Đăng nhập thất bại",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    MainFrame main = new MainFrame(user);
                    main.setVisible(true);
                    dispose();

                } catch (Exception ex) {
                    setStatus("Lỗi kết nối DB / đăng nhập: " + ex.getMessage());
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Lỗi kết nối DB / đăng nhập:\n" + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (isDisplayable()) setLoading(false);
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        txtUsername.setEnabled(!loading);
        txtPassword.setEnabled(!loading);
        chkShowPassword.setEnabled(!loading);

        btnLogin.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
    }

    private void showFieldError(JComponent field, JLabel errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        field.putClientProperty("JComponent.outline", "error");
        field.putClientProperty("JComponent.hideFocusRing", false);

        field.requestFocusInWindow();
    }

    private void clearFieldError(JComponent field, JLabel errorLabel) {
        if (errorLabel.isVisible()) {
            errorLabel.setText(" ");
            errorLabel.setVisible(false);
        }
        if ("error".equals(field.getClientProperty("JComponent.outline"))) {
            field.putClientProperty("JComponent.outline", null);
        }
    }

    private void clearAllErrors() {
        clearFieldError(txtUsername, lblUserError);
        clearFieldError(txtPassword, lblPassError);
        clearStatus();
        updateCapsLockWarning(false);

        txtUsername.putClientProperty("JComponent.hideFocusRing", !userHasTypedUsername);
        txtPassword.putClientProperty("JComponent.hideFocusRing", !userHasTypedPassword);
    }

    private void setStatus(String message) {
        if (message == null || message.isBlank()) {
            clearStatus();
            return;
        }
        lblStatus.setText(message);
        lblStatus.setVisible(true);
    }

    private void clearStatus() {
        lblStatus.setText(" ");
        lblStatus.setVisible(false);
    }

    private void updateCapsLockWarning(boolean show) {
        if (!show) {
            lblCaps.setText(" ");
            lblCaps.setVisible(false);
            return;
        }
        try {
            boolean caps = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
            if (caps) {
                lblCaps.setText("Caps Lock đang bật");
                lblCaps.setVisible(true);
            } else {
                lblCaps.setText(" ");
                lblCaps.setVisible(false);
            }
        } catch (Exception ignored) {
            lblCaps.setText(" ");
            lblCaps.setVisible(false);
        }
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static class SimpleDocListener implements DocumentListener {
        private final Runnable onChange;
        SimpleDocListener(Runnable onChange) { this.onChange = onChange; }
        @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
        @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
        @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
    }

    // ===== Logo helpers =====
    private JLabel buildLogoLabel(int maxW, int maxH) {
        JLabel lb = new JLabel();
        lb.setAlignmentX(Component.CENTER_ALIGNMENT);
        lb.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icon = loadScaledIconKeepRatio("/images/Cute shopping cart logo _ Free Vector.jpg", maxW, maxH);
        if (icon != null) lb.setIcon(icon);

        return lb;
    }

    private ImageIcon loadScaledIconKeepRatio(String resourcePath, int maxW, int maxH) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) return null;

        ImageIcon src = new ImageIcon(url);
        int w = src.getIconWidth();
        int h = src.getIconHeight();
        if (w <= 0 || h <= 0) return src;

        double scale = Math.min((double) maxW / w, (double) maxH / h);
        int newW = Math.max(1, (int) Math.round(w * scale));
        int newH = Math.max(1, (int) Math.round(h * scale));

        Image img = src.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
