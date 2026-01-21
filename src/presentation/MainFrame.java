package presentation;

import bus.AuthService.AuthUser;
import com.formdev.flatlaf.FlatLightLaf;
import util.PermissionCodes;
import util.RolePermission;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
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

    // ===== Theme tokens (LIGHT SIDEBAR like screenshot) =====
    private static final Color BG_APP = hex("#F8FAFF");          // nền app rất nhạt
    private static final Color CARD_BG = hex("#FFFFFF");         // nền card
    private static final Color SIDEBAR_BG = hex("#FFFFFF");      // sidebar trắng
    private static final Color SIDEBAR_BG_2 = hex("#EFF6FF");    // hover xanh nhạt
    private static final Color SIDEBAR_ACTIVE = hex("#2563EB");  // active xanh

    private static final Color TEXT_MAIN = hex("#0F172A");       // chữ chính
    private static final Color TEXT_MUTED = hex("#64748B");      // chữ phụ
    private static final Color TEXT_WHITE = hex("#FFFFFF");      // chữ trắng

    private static final Color BORDER = hex("#E5E7EB");          // viền
    private static final Color BORDER_STRONG = hex("#D9E2F2");   // viền nhẹ xanh

    // Logo (đặt file trong resources: src/main/resources/assets/logo.png)
    private static final String LOGO_PATH = "/images/Cute shopping cart logo _ Free Vector.jpg";
    private static final int BRAND_LOGO_SIZE = 62;

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

    private static Color hex(String s) {
        return Color.decode(s);
    }

    private Image loadLogoImage() {
        try {
            URL url = getClass().getResource(LOGO_PATH);
            if (url == null) return null;
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            return null;
        }
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

        // ===== Sidebar content =====
        JPanel sidebarContent = new JPanel(new BorderLayout());
        sidebarContent.setPreferredSize(new Dimension(250, 0)); // <- độ rộng sidebar (240~280)
        sidebarContent.setBackground(SIDEBAR_BG);

        JPanel brand = buildBrand();
        navPanel = buildNav();
        JButton btnLogout = buildLogoutButton();

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(SIDEBAR_BG);
        bottom.setBorder(new EmptyBorder(12, 14, 16, 14));
        bottom.add(btnLogout, BorderLayout.CENTER);

        sidebarContent.add(brand, BorderLayout.NORTH);
        sidebarContent.add(wrapNavScroll(navPanel), BorderLayout.CENTER);
        sidebarContent.add(bottom, BorderLayout.SOUTH);

        // ===== Sidebar wrapper (BO GÓC) =====
        RoundedPanel sidebarWrap = new RoundedPanel(18);
        sidebarWrap.setBackground(SIDEBAR_BG);
        sidebarWrap.setBorderStyle(BORDER, 1);
        sidebarWrap.setLayout(new BorderLayout());

        // padding wrapper: vừa đủ, không dày
        sidebarWrap.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel sidebarInnerPad = new JPanel(new BorderLayout());
        sidebarInnerPad.setOpaque(false);
        sidebarInnerPad.setBorder(new EmptyBorder(6, 6, 6, 6));
        sidebarInnerPad.add(sidebarContent, BorderLayout.CENTER);

        sidebarWrap.add(sidebarInnerPad, BorderLayout.CENTER);

        // ===== Main content =====
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);

        JPanel header = buildHeader();
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));

        contentPanel.setBackground(BG_APP);
        contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        mainContent.add(header, BorderLayout.NORTH);
        mainContent.add(contentPanel, BorderLayout.CENTER);

        // ===== Main wrapper (BO GÓC) =====
        RoundedPanel mainWrap = new RoundedPanel(22);
        mainWrap.setBackground(CARD_BG);
        mainWrap.setBorderStyle(BORDER, 1);
        mainWrap.setLayout(new BorderLayout());

        // padding wrapper
        mainWrap.setBorder(new EmptyBorder(8, 8, 8, 8));
        mainWrap.add(mainContent, BorderLayout.CENTER);

        // ===== Outer padding =====
        // (1) khoảng cách giữa sidebar và main: giảm còn 8 cho khớp
        JPanel outer = new JPanel(new BorderLayout(8, 8));
        outer.setOpaque(false);

        // (2) padding ngoài cùng: giảm còn 10 cho gọn
        outer.setBorder(new EmptyBorder(10, 10, 10, 10));

        outer.add(sidebarWrap, BorderLayout.WEST);
        outer.add(mainWrap, BorderLayout.CENTER);

        root.add(outer, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildBrand() {
        JPanel brand = new JPanel(new BorderLayout());
        brand.setBackground(SIDEBAR_BG);
        brand.setBorder(new EmptyBorder(14, 14, 10, 14));

        // Logo tròn (nếu không có ảnh sẽ fallback "MS")
        Image logoImg = loadLogoImage();
        JComponent logoComp;
        if (logoImg != null) {
            CircleImageAvatar logo = new CircleImageAvatar(logoImg, BRAND_LOGO_SIZE);
            logo.setBorderColor(BORDER);
            logoComp = logo;
        } else {
            CircleAvatar fallback = new CircleAvatar("MS", BRAND_LOGO_SIZE);
            fallback.setColors(hex("#EFF6FF"), SIDEBAR_ACTIVE, BORDER);
            logoComp = fallback;
        }

        JLabel title = new JLabel("Mini-supermarket");
        title.setForeground(SIDEBAR_ACTIVE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel subtitle = new JLabel("Point of Sale Inventory");
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.add(title);
        textCol.add(Box.createVerticalStrut(3));
        textCol.add(subtitle);

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.add(logoComp);
        row.add(Box.createHorizontalStrut(12));
        row.add(textCol);

        brand.add(row, BorderLayout.WEST);
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
        addNavItem(nav, "Phân quyền", "🔑");

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
        JButton btnLogout = new RoundedButton("Đăng xuất", 18);

        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setForeground(TEXT_WHITE);
        btnLogout.setBackground(SIDEBAR_ACTIVE);
        btnLogout.setBorder(new EmptyBorder(10, 12, 10, 12));
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnLogout.addActionListener(e -> {
            RolePermission.clear();
            new LoginFrame().setVisible(true);
            dispose();
        });

        btnLogout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(hex("#f44336"));
                btnLogout.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(SIDEBAR_ACTIVE);
                btnLogout.repaint();
            }
        });

        return btnLogout;
    }

    private static class RoundedButton extends JButton {
        private final int arc;

        public RoundedButton(String text, int arc) {
            super(text);
            this.arc = arc;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(CARD_BG);
        header.setPreferredSize(new Dimension(0, 60));
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setOpaque(true);

        // Cho phép header nhận focus để "giành focus" từ search
        header.setFocusable(true);

        pageTitleLabel = new JLabel("Dashboard");
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageTitleLabel.setForeground(TEXT_MAIN);
        pageTitleLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // ===== Search (rectangle bo góc, KHÔNG oval) =====
        int searchArc = 16; // 14/16/18 tùy bạn
        RoundedTextField search = new RoundedTextField(searchArc);
        search.setAlignmentY(Component.CENTER_ALIGNMENT);

        search.setMinimumSize(new Dimension(260, 44));
        search.setPreferredSize(new Dimension(520, 44));
        search.setMaximumSize(new Dimension(9999, 44));

        search.setFill(hex("#F8FAFF"));
        search.setStroke(BORDER);
        search.setStrokeWidth(1);

        search.setForeground(TEXT_MAIN);
        search.setCaretColor(TEXT_MAIN);

        search.putClientProperty("JComponent.outline", "none");

        final String placeholder = "Tìm theo mã sản phẩm";
        search.setText(placeholder);
        search.setForeground(hex("#94A3B8"));

        search.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                search.setStroke(hex("#2563EB")); // focus xanh
                if (placeholder.equals(search.getText())) {
                    search.setText("");
                    search.setForeground(TEXT_MAIN);
                }
                search.repaint();
            }

            @Override public void focusLost(java.awt.event.FocusEvent e) {
                search.setStroke(BORDER); // về xám
                if (search.getText().trim().isEmpty()) {
                    search.setText(placeholder);
                    search.setForeground(hex("#94A3B8"));
                }
                search.repaint();
            }
        });

        // ===== Right user info =====
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setAlignmentY(Component.CENTER_ALIGNMENT);

        String name = (currentUser != null && currentUser.fullName != null) ? currentUser.fullName : "User";
        String role = (currentUser != null && currentUser.roleName != null) ? currentUser.roleName : "";

        JLabel userName = new JLabel(name);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userName.setForeground(TEXT_MAIN);

        JLabel userRole = new JLabel(role);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRole.setForeground(TEXT_MUTED);

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
        avatar.setColors(hex("#EFF6FF"), hex("#2563EB"), BORDER_STRONG);
        avatar.setAlignmentY(Component.CENTER_ALIGNMENT);

        right.add(info);
        right.add(Box.createHorizontalStrut(10));
        right.add(avatar);

        // ===== CLICK RA NGOÀI -> MẤT FOCUS SEARCH (caret biến mất) =====
        MouseAdapter blur = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                header.requestFocusInWindow();
            }
        };
        header.addMouseListener(blur);
        pageTitleLabel.addMouseListener(blur);
        right.addMouseListener(blur);
        info.addMouseListener(blur);
        userName.addMouseListener(blur);
        userRole.addMouseListener(blur);
        avatar.addMouseListener(blur);

        // ===== Layout =====
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
        private Color bg = hex("#EFF6FF");
        private Color fg = hex("#2563EB");
        private Color border = hex("#D9E2F2");

        public CircleAvatar(String text, int size) {
            this.text = text;
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
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

            g2.setColor(bg);
            g2.fillOval(x, y, s - 1, s - 1);

            g2.setColor(border);
            g2.drawOval(x, y, s - 1, s - 1);

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

    private static class CircleImageAvatar extends JComponent {
        private Image image;
        private final int size;
        private Color borderColor = BORDER;

        public CircleImageAvatar(Image image, int size) {
            this.image = image;
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
            setOpaque(false);
        }

        public void setBorderColor(Color c) {
            this.borderColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = Math.min(getWidth(), getHeight());
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            Shape clip = new java.awt.geom.Ellipse2D.Double(x, y, s, s);
            g2.setClip(clip);

            if (image != null) {
                int iw = image.getWidth(null);
                int ih = image.getHeight(null);
                if (iw > 0 && ih > 0) {
                    double scale = Math.max((double) s / iw, (double) s / ih);
                    int dw = (int) Math.round(iw * scale);
                    int dh = (int) Math.round(ih * scale);
                    int dx = x + (s - dw) / 2;
                    int dy = y + (s - dh) / 2;
                    g2.drawImage(image, dx, dy, dw, dh, null);
                }
            } else {
                g2.setColor(hex("#EFF6FF"));
                g2.fillOval(x, y, s, s);
            }

            g2.setClip(null);
            g2.setColor(borderColor);
            g2.drawOval(x, y, s - 1, s - 1);

            g2.dispose();
        }
    }

    private void initCards() {
        contentPanel.add(wrapCard(makePlaceholder("TỔNG QUAN (DashboardPanel)")), "Tổng quan");
        contentPanel.add(wrapCard(new presentation.panels.PosSalesPanel()), "Bán hàng");
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
        contentPanel.add(wrapCard(new presentation.panels.RolePermissionPanel()), "Phân quyền");
    }

    private JComponent wrapCard(JComponent inner) {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(CARD_BG);
        card.setBorderStyle(BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel makePlaceholder(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(hex("#334155"));
        p.add(l);

        return p;
    }

    private void wireNav() {
        for (Map.Entry<String, NavItem> e : navItems.entrySet()) {
            String key = e.getKey();
            NavItem item = e.getValue();
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent ev) {
                    JPanel row = navRows.get(key);
                    if (row != null && !row.isVisible()) return;
                    showCard(key);
                }
            });
        }
    }

    private void showCard(String name) {
        for (Map.Entry<String, NavItem> e : navItems.entrySet()) {
            e.getValue().setActive(e.getKey().equals(name));
        }

        if (pageTitleLabel != null) pageTitleLabel.setText(name);
        cardLayout.show(contentPanel, name);
        setTitle("Mini Supermarket • " + name);
    }

    private void applyRoleVisibility() {
        boolean DEBUG_MODE = true;
        setNavVisible("Tổng quan", DEBUG_MODE | RolePermission.has(PermissionCodes.DASHBOARD_VIEW));
        setNavVisible("Bán hàng", DEBUG_MODE | RolePermission.has(PermissionCodes.POS_SELL));
        setNavVisible("Hóa đơn", DEBUG_MODE | RolePermission.has(PermissionCodes.INVOICE_VIEW) || RolePermission.has(PermissionCodes.REPORT_VIEW));
        setNavVisible("Sản phẩm", DEBUG_MODE | RolePermission.has(PermissionCodes.PRODUCT_VIEW));
        setNavVisible("Nhập kho", DEBUG_MODE | RolePermission.has(PermissionCodes.RECEIPT_CREATE));
        setNavVisible("Kiểm kho", DEBUG_MODE | RolePermission.has(PermissionCodes.ADJUSTMENT_CREATE) || RolePermission.has(PermissionCodes.ADJUSTMENT_APPROVE));

        setNavVisible("Khách hàng", DEBUG_MODE | RolePermission.has(PermissionCodes.CUSTOMER_VIEW) || RolePermission.has(PermissionCodes.CUSTOMER_MANAGE));
        setNavVisible("Nhà cung cấp", DEBUG_MODE | RolePermission.has(PermissionCodes.SUPPLIER_VIEW) || RolePermission.has(PermissionCodes.SUPPLIER_MANAGE));
        setNavVisible("Danh mục", DEBUG_MODE | RolePermission.has(PermissionCodes.CATEGORY_VIEW));

        setNavVisible("Khuyến mãi", DEBUG_MODE | RolePermission.has(PermissionCodes.PROMOTION_MANAGE));
        setNavVisible("Thanh toán", DEBUG_MODE | RolePermission.has(PermissionCodes.PAYMENT_VIEW));
        setNavVisible("Báo cáo", DEBUG_MODE | RolePermission.has(PermissionCodes.REPORT_VIEW));

        setNavVisible("Nhân viên", DEBUG_MODE | RolePermission.has(PermissionCodes.USER_MANAGE));
        setNavVisible("Phân quyền", DEBUG_MODE | RolePermission.has(PermissionCodes.ROLE_PERMISSION_MANAGE));

        if (navPanel != null) {
            navPanel.revalidate();
            navPanel.repaint();
        }

        showFirstAllowedCard();
    }

    private void setNavVisible(String name, boolean visible) {
        JPanel row = navRows.get(name);
        if (row != null) row.setVisible(visible);
    }

    private void showFirstAllowedCard() {
        JPanel dashRow = navRows.get("Tổng quan");
        if (dashRow != null && dashRow.isVisible()) {
            showCard("Tổng quan");
            return;
        }

        for (String key : navRows.keySet()) {
            JPanel row = navRows.get(key);
            if (row != null && row.isVisible()) {
                showCard(key);
                return;
            }
        }

        showCard("Tổng quan");
    }

    // ===== Custom Components =====
    private class NavItem extends JPanel {
        private final JLabel text;
        private final JLabel icon;
        private boolean active = false;
        private boolean hover = false;

        NavItem(String key, String iconText) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            setPreferredSize(new Dimension(0, NAV_ITEM_HEIGHT));
            setMinimumSize(new Dimension(0, NAV_ITEM_HEIGHT));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, NAV_ITEM_HEIGHT));

            icon = new JLabel(iconText);
            icon.setForeground(TEXT_MAIN);
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            icon.setBorder(new EmptyBorder(0, 4, 0, 10));

            text = new JLabel(key);
            text.setForeground(TEXT_MAIN);
            text.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            left.setOpaque(false);
            left.add(icon);
            left.add(text);

            add(left, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            icon.setForeground(active ? TEXT_WHITE : TEXT_MAIN);
            text.setForeground(active ? TEXT_WHITE : TEXT_MAIN);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 14;
            int w = getWidth();
            int h = getHeight();

            if (active) {
                g2.setColor(SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            } else if (hover) {
                g2.setColor(SIDEBAR_BG_2);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedBorder implements Border {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(6, 10, 6, 10);
        }

        @Override public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int arc;

        private Color borderColor = null;
        private int borderThickness = 1;

        // ===== Shadow =====
        private boolean shadowEnabled = false;
        private int shadowSize = 10;                 // độ lan bóng
        private int shadowOffsetY = 6;               // bóng rơi xuống
        private Color shadowColor = new Color(0, 0, 0, 25); // alpha: càng lớn càng đậm

        RoundedPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        public void setBorderStyle(Color borderColor, int thickness) {
            this.borderColor = borderColor;
            this.borderThickness = Math.max(1, thickness);
            repaint();
        }

        public void setShadow(boolean enabled) {
            this.shadowEnabled = enabled;
            repaint();
        }

        public void setShadowStyle(int size, int offsetY, Color color) {
            this.shadowSize = Math.max(0, size);
            this.shadowOffsetY = offsetY;
            this.shadowColor = (color != null) ? color : new Color(0, 0, 0, 25);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // chừa chỗ cho shadow để không bị cắt
            int pad = shadowEnabled ? shadowSize : 0;

            int x = pad;
            int y = pad;
            int rw = w - pad * 2;
            int rh = h - pad * 2;

            // ===== Shadow (fake blur bằng nhiều lớp alpha) =====
            if (shadowEnabled) {
                for (int i = shadowSize; i >= 1; i--) {
                    float alpha = (float) i / (shadowSize * 22f); // chỉnh mượt/đậm ở đây
                    g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
                    g2.setColor(shadowColor);

                    g2.fillRoundRect(
                            x - i,
                            y - i + shadowOffsetY,
                            rw + i * 2,
                            rh + i * 2,
                            arc + i * 2,
                            arc + i * 2
                    );
                }
                g2.setComposite(AlphaComposite.SrcOver);
            }

            // ===== Background =====
            g2.setColor(getBackground());
            g2.fillRoundRect(x, y, rw, rh, arc, arc);

            // ===== Border =====
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderThickness));
                int inset = borderThickness / 2;
                g2.drawRoundRect(
                        x + inset,
                        y + inset,
                        rw - borderThickness,
                        rh - borderThickness,
                        arc,
                        arc
                );
            }

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Insets getInsets() {
            // để layout bên trong không đè lên vùng shadow
            if (!shadowEnabled) return super.getInsets();
            int pad = shadowSize;
            return new Insets(pad, pad, pad + shadowOffsetY, pad);
        }
    }

    // ===== RoundedTextField (pill thật - tự vẽ nền + viền) =====
    private static class RoundedTextField extends JTextField {
        private final int arc;
        private Color fill = hex("#B4B5B7");
        private Color stroke = hex("#E5E7EB");
        private int strokeWidth = 1;

        public RoundedTextField(int arc) {
            this.arc = arc;

            // Không cho JTextField tự vẽ nền/border mặc định
            setOpaque(false);
            super.setBorder(null);

            // Padding text bên trong
            setBorder(new EmptyBorder(10, 18, 10, 18));

            // Tắt focus ring/outline của FlatLaf
            putClientProperty("JComponent.outline", "none");
        }

        public void setFill(Color c) { this.fill = c; repaint(); }
        public void setStroke(Color c) { this.stroke = c; repaint(); }
        public void setStrokeWidth(int w) { this.strokeWidth = Math.max(1, w); repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Vẽ nền bo góc
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g); // vẽ text/caret
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(strokeWidth));

            int inset = strokeWidth;
            g2.drawRoundRect(inset, inset, w - inset * 2 - 1, h - inset * 2 - 1, arc, arc);

            g2.dispose();
        }
    }
}
