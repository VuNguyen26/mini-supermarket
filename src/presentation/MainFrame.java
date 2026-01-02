package presentation;

import bus.AuthService.AuthUser;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final AuthUser currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public MainFrame(AuthUser user) {
        this.currentUser = user;

        setTitle("MINI SUPERMARKET");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        setContentPane(buildUI());
        initCards();
        wireNav();
        applyRoleVisibility();

        // default view
        showCard("Tổng quan");
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout());

        // Left sidebar
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel brand = new JLabel("MINI SUPERMARKET");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brand.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));

        addNavButton(nav, "Tổng quan");
        addNavButton(nav, "Sản phẩm");
        addNavButton(nav, "Hóa đơn");
        addNavButton(nav, "Nhập kho");
        addNavButton(nav, "Khách hàng");
        addNavButton(nav, "Nhà cung cấp");
        addNavButton(nav, "Danh mục");
        addNavButton(nav, "Nhân viên");

        JButton btnLogout = new JButton("Đăng xuất");
        styleNavButton(btnLogout);
        btnLogout.setForeground(new Color(200, 0, 0));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 8));
        bottom.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(new JScrollPane(nav,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        // Top header
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        header.setBackground(Color.WHITE);

        JLabel userInfo = new JLabel(currentUser.fullName + "  |  " + currentUser.roleName);
        userInfo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        userInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        header.add(userInfo, BorderLayout.EAST);

        // Center content
        contentPanel.setBackground(new Color(245, 245, 245));
        root.add(sidebar, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);
        root.add(contentPanel, BorderLayout.CENTER);

        return root;
    }

    private void addNavButton(JPanel nav, String name) {
        JButton b = new JButton(name);
        styleNavButton(b);
        navButtons.put(name, b);
        nav.add(b);
        nav.add(Box.createVerticalStrut(6));
    }

    private void styleNavButton(JButton b) {
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    private void initCards() {
        contentPanel.add(makePlaceholder("TỔNG QUAN (Dashboard)"), "Tổng quan");
        contentPanel.add(makePlaceholder("SẢN PHẨM (ProductPanel)"), "Sản phẩm");
        contentPanel.add(makePlaceholder("HÓA ĐƠN (SalesInvoicePanel)"), "Hóa đơn");
        contentPanel.add(makePlaceholder("NHẬP KHO (GoodsReceiptPanel)"), "Nhập kho");
        contentPanel.add(makePlaceholder("KHÁCH HÀNG (CustomerPanel)"), "Khách hàng");
        contentPanel.add(makePlaceholder("NHÀ CUNG CẤP (SupplierPanel)"), "Nhà cung cấp");
        contentPanel.add(makePlaceholder("DANH MỤC (CategoryPanel)"), "Danh mục");
        contentPanel.add(makePlaceholder("NHÂN VIÊN (UserPanel)"), "Nhân viên");
    }

    private JPanel makePlaceholder(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(245, 245, 245));
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(new Color(80, 80, 80));
        p.add(l);
        return p;
    }

    private void wireNav() {
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            String key = e.getKey();
            e.getValue().addActionListener(ev -> showCard(key));
        }
    }

    private void showCard(String name) {
        // highlight selected
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            JButton b = e.getValue();
            boolean active = e.getKey().equals(name);
            b.setBackground(active ? new Color(230, 240, 255) : Color.WHITE);
        }
        cardLayout.show(contentPanel, name);
    }

    // Role-based visibility
    // roleName từ DB: ADMIN / CASHIER / WAREHOUSE
    private void applyRoleVisibility() {
        String role = currentUser.roleName == null ? "" : currentUser.roleName.toUpperCase();

        if ("CASHIER".equals(role)) {
            // Thu ngân: chủ yếu bán hàng + hóa đơn + khách hàng + sản phẩm xem
            hideNav("Nhập kho");
            hideNav("Nhà cung cấp");
            hideNav("Danh mục");
            hideNav("Nhân viên");
        } else if ("WAREHOUSE".equals(role)) {
            // Kho: nhập kho + sản phẩm + NCC
            hideNav("Hóa đơn");
            hideNav("Khách hàng");
            hideNav("Danh mục");
            hideNav("Nhân viên");
        }
        // ADMIN: thấy hết
    }

    private void hideNav(String name) {
        JButton b = navButtons.get(name);
        if (b != null) b.setVisible(false);
    }
}
