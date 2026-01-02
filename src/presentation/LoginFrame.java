package presentation;

import bus.AuthService;
import bus.AuthService.AuthUser;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JLabel lblStatus = new JLabel(" ");

    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("MINI SUPERMARKET - Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(520, 380);
        setLocationRelativeTo(null);
        setResizable(false);

        setContentPane(buildUI());
        wireEvents();
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 245, 245));

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(360, 260));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MINI SUPERMARKET");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel subtitle = new JLabel("Đăng nhập vào hệ thống");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 100, 100));

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        card.add(label("Tên đăng nhập"));
        card.add(Box.createVerticalStrut(6));
        card.add(styledField(txtUsername));
        card.add(Box.createVerticalStrut(12));

        card.add(label("Mật khẩu"));
        card.add(Box.createVerticalStrut(6));
        card.add(styledField(txtPassword));
        card.add(Box.createVerticalStrut(16));

        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setFocusPainted(false);
        btnLogin.setBackground(new Color(30, 90, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(200, 0, 0));

        card.add(btnLogin);
        card.add(Box.createVerticalStrut(12));
        card.add(lblStatus);

        root.add(card);
        return root;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private JComponent styledField(JComponent field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setPreferredSize(new Dimension(300, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return field;
    }

    private void wireEvents() {
        // Enter để login
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        btnLogin.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        lblStatus.setText(" ");
        btnLogin.setEnabled(false);

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Vui lòng nhập username và password.");
            btnLogin.setEnabled(true);
            return;
        }

        try {
            AuthUser user = authService.login(username, password);
            if (user == null) {
                lblStatus.setText("Sai tài khoản/mật khẩu hoặc tài khoản bị khóa.");
                btnLogin.setEnabled(true);
                return;
            }

            // Login OK -> mở MainFrame
            MainFrame main = new MainFrame(user);
            main.setVisible(true);
            dispose();

        } catch (Exception ex) {
            lblStatus.setText("Lỗi kết nối DB / đăng nhập: " + ex.getMessage());
            btnLogin.setEnabled(true);
        }
    }
}
