package presentation;

import bus.AuthService.AuthUser;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final AuthUser currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // Nav items + rows (row = item + gap)
    private final Map<String, NavItem> navItems = new LinkedHashMap<>();
    private final Map<String, JPanel> navRows = new LinkedHashMap<>();
    private JPanel navPanel; // để revalidate/repaint sau khi hide theo role

    // Header dynamic title
    private JLabel pageTitleLabel;

    // ===== Theme tokens =====
    private static final Color BG_APP = new Color(245, 247, 250);
    private static final Color SIDEBAR_BG = new Color(16, 24, 40);
    private static final Color SIDEBAR_BG_2 = new Color(22, 34, 56);
    private static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color TEXT_WHITE = new Color(236, 245, 255);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(228, 231, 236);

    // Kích thước menu item
    private static final int NAV_ITEM_HEIGHT = 48;
    private static final int NAV_GAP = 8;

    public MainFrame(AuthUser user) {
        this.currentUser = user;

        setupLookAndFeel();

        setTitle("Mini Supermarket • Dashboard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1260, 760);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        setContentPane(buildUI());
        initCards();
        wireNav();
        applyRoleVisibility();
    }

    private void setupLookAndFeel() {
        try {
            FlatLightLaf.setup();

            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));

            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        } catch (Exception ignored) {}
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_APP);

        // ===== Sidebar =====
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(10, 15, 25)));

        JPanel brand = buildBrand();
        navPanel = buildNav();              // <-- lưu field để xử lý role
        JButton btnLogout = buildLogoutButton();

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(SIDEBAR_BG);
        bottom.setBorder(new EmptyBorder(12, 14, 16, 14));
        bottom.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(wrapNavScroll(navPanel), BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        // ===== Main area =====
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_APP);

        JPanel header = buildHeader();
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));

        contentPanel.setBackground(BG_APP);
        contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        main.add(header, BorderLayout.NORTH);
        main.add(contentPanel, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(main, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildBrand() {
        JPanel brand = new JPanel(new BorderLayout());
        brand.setBackground(SIDEBAR_BG);
        brand.setBorder(new EmptyBorder(16, 16, 12, 16));

        JLabel title = new JLabel("MINI SUPERMARKET");
        title.setForeground(TEXT_WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel subtitle = new JLabel("Point of Sale • Inventory");
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(subtitle);

        brand.add(text, BorderLayout.CENTER);
        return brand;
    }

    private JPanel buildNav() {
        JPanel nav = new JPanel();
        nav.setBackground(SIDEBAR_BG);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(6, 10, 10, 10));

        addNavItem(nav, "Tổng quan", "🏠");
        addNavItem(nav, "Bán hàng", "🛒");
        addNavItem(nav, "Hóa đơn", "🧾");

        addNavItem(nav, "Sản phẩm", "📦");
        addNavItem(nav, "Nhập kho", "🚚");
        addNavItem(nav, "Kiểm kho", "🧮");

        addNavItem(nav, "Khách hàng", "👥");
        addNavItem(nav, "Nhà cung cấp", "🏭");
        addNavItem(nav, "Danh mục", "🧩");

        addNavItem(nav, "Khuyến mãi", "🏷️");
        addNavItem(nav, "Thanh toán", "💳");
        addNavItem(nav, "Báo cáo", "📊");

        addNavItem(nav, "Nhân viên", "🛡️");

        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private void addNavItem(JPanel nav, String name, String iconText) {
        NavItem item = new NavItem(name, iconText);
        navItems.put(name, item);

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.add(item);
        row.add(Box.createVerticalStrut(NAV_GAP));

        // Khóa kích thước row để không bị giãn chiều cao
        int rowH = NAV_ITEM_HEIGHT + NAV_GAP;
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));
        row.setPreferredSize(new Dimension(0, rowH));
        row.setMinimumSize(new Dimension(0, rowH));

        navRows.put(name, row);
        nav.add(row);
    }

    private JScrollPane wrapNavScroll(JPanel nav) {
        JScrollPane sp = new JScrollPane(nav,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        return sp;
    }

    private JButton buildLogoutButton() {
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setForeground(new Color(255, 220, 220));
        btnLogout.setBackground(new Color(127, 29, 29));
        btnLogout.setBorder(new EmptyBorder(10, 12, 10, 12));
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        btnLogout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(new Color(153, 27, 27));
            }
            @Override public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(new Color(127, 29, 29));
            }
        });

        return btnLogout;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 60));
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setOpaque(true);

        pageTitleLabel = new JLabel("Dashboard");
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageTitleLabel.setForeground(new Color(17, 24, 39));
        pageTitleLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JTextField search = new JTextField();
        search.setAlignmentY(Component.CENTER_ALIGNMENT);
        search.setMinimumSize(new Dimension(260, 36));
        search.setPreferredSize(new Dimension(520, 36));
        search.setMaximumSize(new Dimension(9999, 36));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        final String placeholder = "Tìm kiếm…";
        search.setText(placeholder);
        search.setForeground(new Color(156, 163, 175));
        search.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (placeholder.equals(search.getText())) {
                    search.setText("");
                    search.setForeground(new Color(17, 24, 39));
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (search.getText().trim().isEmpty()) {
                    search.setText(placeholder);
                    search.setForeground(new Color(156, 163, 175));
                }
            }
        });

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setAlignmentY(Component.CENTER_ALIGNMENT);

        String name = currentUser.fullName == null ? "User" : currentUser.fullName;
        String role = currentUser.roleName == null ? "" : currentUser.roleName;

        JLabel userName = new JLabel(name);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userName.setForeground(new Color(17, 24, 39));

        JLabel userRole = new JLabel(role);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRole.setForeground(new Color(100, 116, 139));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setAlignmentY(Component.CENTER_ALIGNMENT);
        userName.setAlignmentX(Component.RIGHT_ALIGNMENT);
        userRole.setAlignmentX(Component.RIGHT_ALIGNMENT);
        info.add(userName);
        info.add(Box.createVerticalStrut(2));
        info.add(userRole);

        String initials = getInitials(name);
        CircleAvatar avatar = new CircleAvatar(initials, 36);
        avatar.setColors(
                new Color(239, 246, 255),
                new Color(37, 99, 235),
                new Color(219, 234, 254)
        );
        avatar.setAlignmentY(Component.CENTER_ALIGNMENT);

        right.add(info);
        right.add(Box.createHorizontalStrut(10));
        right.add(avatar);

        header.add(pageTitleLabel);
        header.add(Box.createHorizontalStrut(16));
        header.add(search);
        header.add(Box.createHorizontalGlue());
        header.add(right);

        return header;
    }

    private String getInitials(String fullName) {
        if (fullName == null) return "U";
        String s = fullName.trim();
        if (s.isEmpty()) return "U";

        // Lấy 2 chữ cái đầu của 2 từ (nếu có), còn không thì lấy 2 ký tự đầu
        String[] parts = s.split("\\s+");
        if (parts.length >= 2) {
            String a = parts[0].substring(0, 1);
            String b = parts[parts.length - 1].substring(0, 1);
            return (a + b).toUpperCase();
        }
        return (s.length() >= 2 ? s.substring(0, 2) : s.substring(0, 1)).toUpperCase();
    }


    private static class CircleAvatar extends JComponent {
        private String text;
        private Color bg = new Color(239, 246, 255);
        private Color fg = new Color(37, 99, 235);
        private Color border = new Color(219, 234, 254);

        public CircleAvatar(String text, int size) {
            this.text = text;
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Tài khoản");
        }

        public void setColors(Color bg, Color fg, Color border) {
            this.bg = bg;
            this.fg = fg;
            this.border = border;
            repaint();
        }

        public void setText(String text) {
            this.text = text;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = Math.min(getWidth(), getHeight());
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            // circle bg
            g2.setColor(bg);
            g2.fillOval(x, y, s - 1, s - 1);

            // circle border
            g2.setColor(border);
            g2.drawOval(x, y, s - 1, s - 1);

            // centered text
            g2.setColor(fg);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(text);
            int th = fm.getAscent();
            int tx = x + (s - tw) / 2;
            int ty = y + (s + th) / 2 - 2;
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }

    private void initCards() {
        contentPanel.add(wrapCard(makePlaceholder("TỔNG QUAN (DashboardPanel)")), "Tổng quan");
        contentPanel.add(wrapCard(makePlaceholder("BÁN HÀNG (PosSalesPanel)")), "Bán hàng");
        contentPanel.add(wrapCard(makePlaceholder("HÓA ĐƠN (SalesInvoicePanel)")), "Hóa đơn");

        contentPanel.add(wrapCard(makePlaceholder("SẢN PHẨM (ProductPanel + tab tồn theo lô/HSD)")), "Sản phẩm");
        contentPanel.add(wrapCard(makePlaceholder("NHẬP KHO (GoodsReceiptPanel)")), "Nhập kho");
        contentPanel.add(wrapCard(makePlaceholder("KIỂM KHO / ĐIỀU CHỈNH (StockAdjustmentPanel)")), "Kiểm kho");

        contentPanel.add(wrapCard(makePlaceholder("KHÁCH HÀNG (CustomerPanel + tab điểm/loyalty)")), "Khách hàng");
        contentPanel.add(wrapCard(makePlaceholder("NHÀ CUNG CẤP (SupplierPanel)")), "Nhà cung cấp");
        contentPanel.add(wrapCard(makePlaceholder("DANH MỤC (CategoryPanel)")), "Danh mục");

        contentPanel.add(wrapCard(makePlaceholder("KHUYẾN MÃI (PromotionPanel)")), "Khuyến mãi");
        contentPanel.add(wrapCard(makePlaceholder("THANH TOÁN (PaymentPanel)")), "Thanh toán");
        contentPanel.add(wrapCard(makePlaceholder("BÁO CÁO (ReportPanel)")), "Báo cáo");

        contentPanel.add(wrapCard(makePlaceholder("NHÂN VIÊN (UserPanel)")), "Nhân viên");
    }

    private JComponent wrapCard(JComponent inner) {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));
        card.setLayout(new BorderLayout());
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel makePlaceholder(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(new Color(51, 65, 85));
        p.add(l);

        return p;
    }

    private void wireNav() {
        for (Map.Entry<String, NavItem> e : navItems.entrySet()) {
            String key = e.getKey();
            NavItem item = e.getValue();
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent ev) {
                    // Nếu menu đang bị ẩn theo role thì bỏ qua
                    JPanel row = navRows.get(key);
                    if (row != null && !row.isVisible()) return;

                    showCard(key);
                }
            });
        }
    }

    private void showCard(String name) {
        // update nav styles
        for (Map.Entry<String, NavItem> e : navItems.entrySet()) {
            e.getValue().setActive(e.getKey().equals(name));
        }

        if (pageTitleLabel != null) pageTitleLabel.setText(name);
        cardLayout.show(contentPanel, name);
        setTitle("Mini Supermarket • " + name);
    }

    private void applyRoleVisibility() {
        String role = currentUser.roleName == null ? "" : currentUser.roleName.toUpperCase();

        // ALLOW LIST: role nào được xem menu nào
        java.util.Set<String> allowed;

        switch (role) {
            case "ADMIN" -> allowed = java.util.Set.of(
                    "Tổng quan","Bán hàng","Hóa đơn","Sản phẩm","Nhập kho","Kiểm kho",
                    "Khách hàng","Nhà cung cấp","Danh mục","Khuyến mãi","Thanh toán","Báo cáo","Nhân viên"
            );

            case "MANAGER" -> allowed = java.util.Set.of(
                    "Tổng quan","Bán hàng","Hóa đơn","Sản phẩm","Nhập kho","Kiểm kho",
                    "Khách hàng","Nhà cung cấp","Danh mục","Khuyến mãi","Thanh toán","Báo cáo"
                    // Manager thường không quản user
            );

            case "CASHIER" -> allowed = java.util.Set.of(
                    "Tổng quan","Bán hàng","Hóa đơn","Khách hàng"
            );

            case "WAREHOUSE" -> allowed = java.util.Set.of(
                    "Tổng quan","Sản phẩm","Nhập kho","Kiểm kho","Nhà cung cấp","Danh mục","Báo cáo"
            );

            case "ACCOUNTANT" -> allowed = java.util.Set.of(
                    "Tổng quan","Hóa đơn","Thanh toán","Báo cáo"
            );

            default -> allowed = java.util.Set.of("Tổng quan"); // fallback an toàn
        }

        // Apply visibility theo allow list
        for (String key : navRows.keySet()) {
            JPanel row = navRows.get(key);
            if (row != null) row.setVisible(allowed.contains(key));
        }

        // Refresh layout sau khi ẩn/hiện
        if (navPanel != null) {
            navPanel.revalidate();
            navPanel.repaint();
        }

        // Chuyển sang màn hình đầu tiên được phép (tránh showCard vào card bị ẩn)
        showFirstAllowedCard();
    }

    private void showFirstAllowedCard() {
        // Ưu tiên mở Dashboard nếu được phép
        JPanel dashRow = navRows.get("Tổng quan");
        if (dashRow != null && dashRow.isVisible()) {
            showCard("Tổng quan");
            return;
        }

        // Nếu không thì mở menu đầu tiên đang visible
        for (String key : navRows.keySet()) {
            JPanel row = navRows.get(key);
            if (row != null && row.isVisible()) {
                showCard(key);
                return;
            }
        }
    }

    private void hideNav(String name) {
        JPanel row = navRows.get(name);
        if (row != null) row.setVisible(false);
    }

    // ===== Custom Components =====
    private class NavItem extends JPanel {
        private final JLabel text;
        private boolean active = false;

        NavItem(String key, String iconText) {
            setOpaque(true);
            setBackground(SIDEBAR_BG);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Khóa chiều cao item để tránh BoxLayout kéo giãn
            setPreferredSize(new Dimension(0, NAV_ITEM_HEIGHT));
            setMinimumSize(new Dimension(0, NAV_ITEM_HEIGHT));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, NAV_ITEM_HEIGHT));

            JPanel indicator = new JPanel();
            indicator.setPreferredSize(new Dimension(4, 0));
            indicator.setOpaque(true);
            indicator.setBackground(SIDEBAR_BG);

            JLabel icon = new JLabel(iconText);
            icon.setForeground(TEXT_WHITE);
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            icon.setBorder(new EmptyBorder(0, 6, 0, 10));

            text = new JLabel(key);
            text.setForeground(TEXT_MUTED);
            text.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            left.setOpaque(false);
            left.add(icon);
            left.add(text);

            add(indicator, BorderLayout.WEST);
            add(left, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!active) setBackground(SIDEBAR_BG_2);
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!active) setBackground(SIDEBAR_BG);
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;

            Component indicator = getComponent(0);
            if (indicator instanceof JPanel p) {
                p.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
            }

            setBackground(active ? new Color(24, 35, 58) : SIDEBAR_BG);
            text.setForeground(active ? TEXT_WHITE : TEXT_MUTED);
            repaint();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int arc;

        RoundedPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
