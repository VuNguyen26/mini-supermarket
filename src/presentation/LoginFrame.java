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

public class LoginFrame extends JFrame {

    // ===== UI constants =====
    private static final int CARD_WIDTH = 520;
    private static final int FIELD_WIDTH = 420;
    private static final int FIELD_HEIGHT = 40;
    private static final int BTN_HEIGHT = 46;

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

    // Track "has typed" to control focus highlight
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

        // Enter = login
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
        card.setPreferredSize(new Dimension(CARD_WIDTH, 400));

        // Title
        JLabel title = new JLabel("MINI SUPERMARKET");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel subtitle = new JLabel("Đăng nhập vào hệ thống");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        // Prepare fields
        prepareTextField(txtUsername);
        preparePasswordField(txtPassword);

        // Placeholder
        txtUsername.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập");
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");

        txtUsername.putClientProperty("JComponent.hideFocusRing", true);
        txtPassword.putClientProperty("JComponent.hideFocusRing", true);

        // Fields
        card.add(buildLabeledField("Tên đăng nhập", txtUsername, lblUserError));
        card.add(Box.createVerticalStrut(12));
        card.add(buildLabeledField("Mật khẩu", txtPassword, lblPassError));
        card.add(Box.createVerticalStrut(10));

        // Caps warning
        styleCapsLabel(lblCaps);
        card.add(lblCaps);
        card.add(Box.createVerticalStrut(8));

        // Show password
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setForeground(new Color(90, 90, 90));
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(chkShowPassword);

        card.add(Box.createVerticalStrut(16));

        // Button
        stylePrimaryButton(btnLogin);
        card.add(btnLogin);

        card.add(Box.createVerticalStrut(10));

        // Status
        styleStatusLabel(lblStatus);
        card.add(lblStatus);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(card);

        root.add(wrap);
        return root;
    }

    private JComponent buildLabeledField(String label, JComponent field, JLabel errorLabel) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(labelColor);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension size = new Dimension(FIELD_WIDTH, FIELD_HEIGHT);
        field.setPreferredSize(size);
        field.setMaximumSize(size);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleErrorLabel(errorLabel);

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
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        lb.setAlignmentX(Component.LEFT_ALIGNMENT);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(errorColor);
        lb.setText(" ");
        lb.setVisible(false);
    }

    private void styleStatusLabel(JLabel lb) {
        lb.setAlignmentX(Component.LEFT_ALIGNMENT);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(errorColor);
        lb.setText(" ");
        lb.setVisible(false);
    }

    private void styleCapsLabel(JLabel lb) {
        lb.setAlignmentX(Component.LEFT_ALIGNMENT);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(new Color(180, 90, 0));
        lb.setText(" ");
        lb.setVisible(false);
    }

    private void wireEvents() {
        // Enter flow
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(e -> doLogin());
        btnLogin.addActionListener(e -> doLogin());

        // show/hide password
        defaultEcho = txtPassword.getEchoChar();
        chkShowPassword.addActionListener(e -> {
            txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : defaultEcho);
            txtPassword.requestFocusInWindow();
        });

        // Caps lock warning on focus
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCapsLockWarning(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                updateCapsLockWarning(false);
            }
        });

        // Live validation + focus ring control
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
                // nếu chưa nhập thì vẫn hide
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

    // ===== FlatLaf outline error =====
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

        // reset focus ring rule
        txtUsername.putClientProperty("JComponent.hideFocusRing", userHasTypedUsername ? false : true);
        txtPassword.putClientProperty("JComponent.hideFocusRing", userHasTypedPassword ? false : true);
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
}
