package presentation.panels;

import dto.User;
import bus.UserService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserPanel extends JPanel {

    // =========================
    // Services / Data
    // =========================
    private final UserService userService = new UserService();

    // =========================
    // UI Components
    // =========================
    private JTextField txtSearch;

    private JTable tbl;
    private DefaultTableModel model;

    private JTextField txtId;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtFullName;
    private JTextField txtPhone;

    private JComboBox<String> cboRole;
    private JComboBox<String> cboStatus;

    private final Map<String, Integer> roleMap = new LinkedHashMap<>();

    // =========================
    // Theme
    // =========================
    private static final Color C_BG = new Color(245, 247, 250);
    private static final Color C_CARD = Color.WHITE;
    private static final Color C_PRIMARY = new Color(37, 99, 235);
    private static final Color C_PRIMARY_HOVER = new Color(29, 78, 216);
    private static final Color C_TEXT = new Color(15, 23, 42);
    private static final Color C_MUTED = new Color(100, 116, 139);
    private static final Color C_BORDER = new Color(226, 232, 240);
    private static final Color C_TABLE_ALT = new Color(248, 250, 252);
    private static final Color C_ROW_SELECTED = new Color(52, 117, 178);

    public UserPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(C_BG);
        setOpaque(true);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        initRoleMap();
        initUI();
        loadTableSafe();
    }

    private void initRoleMap() {
        roleMap.put("1 - Admin", 1);
        roleMap.put("2 - Manager", 2);
        roleMap.put("3 - Cashier", 3);
        roleMap.put("4 - Warehouse", 4);
        roleMap.put("5 - Accountant", 5);
    }

    private void initUI() {
        // =========================
        // TOP BAR (Title + Search)
        // =========================
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Nhân viên");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 20f));
        lblTitle.setForeground(C_TEXT);

        txtSearch = new JTextField();
        txtSearch.setToolTipText("Tìm theo username / họ tên / sđt");
        styleTextField(txtSearch);

        JButton btnSearch = new JButton("Tìm");
        JButton btnReload = new JButton("Tải lại");
        styleButton(btnSearch, true);
        styleButton(btnReload, false);

        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRight.setOpaque(false);
        searchRight.add(btnSearch);
        searchRight.add(btnReload);

        JPanel searchBox = new JPanel(new BorderLayout(8, 8));
        searchBox.setOpaque(false);
        searchBox.add(txtSearch, BorderLayout.CENTER);
        searchBox.add(searchRight, BorderLayout.EAST);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(searchBox, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        // =========================
        // MAIN CONTENT (Table + Form)
        // =========================
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);
        add(content, BorderLayout.CENTER);

        // ===== LEFT: TABLE CARD =====
        JPanel tableCard = new JPanel(new BorderLayout(10, 10));
        tableCard.setBackground(C_CARD);
        tableCard.setOpaque(true);
        tableCard.setBorder(cardBorder());

        JPanel tableHeader = new JPanel(new BorderLayout(8, 8));
        tableHeader.setOpaque(false);

        JLabel lblTable = new JLabel("Danh sách nhân viên");
        lblTable.setFont(lblTable.getFont().deriveFont(Font.BOLD, 14f));
        lblTable.setForeground(C_TEXT);

        JLabel lblHint = new JLabel("Chọn 1 dòng để sửa / khóa / mở khóa");
        lblHint.setForeground(C_MUTED);
        lblHint.setFont(lblHint.getFont().deriveFont(12f));

        JPanel tableHeaderLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        tableHeaderLeft.setOpaque(false);
        tableHeaderLeft.add(lblTable);
        tableHeaderLeft.add(lblHint);

        tableHeader.add(tableHeaderLeft, BorderLayout.WEST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "ID", "Username", "Họ tên", "SĐT", "Role", "Status", "Created"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbl = new JTable(model);
        tbl.setRowHeight(30);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Bật cuộn ngang + set width để không che cột Created
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        styleTable(tbl);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        tableCard.add(sp, BorderLayout.CENTER);

        // Width columns
        tbl.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        tbl.getColumnModel().getColumn(1).setPreferredWidth(140);  // Username
        tbl.getColumnModel().getColumn(2).setPreferredWidth(180);  // Họ tên
        tbl.getColumnModel().getColumn(3).setPreferredWidth(120);  // SĐT
        tbl.getColumnModel().getColumn(4).setPreferredWidth(90);   // Role
        tbl.getColumnModel().getColumn(5).setPreferredWidth(100);  // Status
        tbl.getColumnModel().getColumn(6).setPreferredWidth(200);  // Created

        content.add(tableCard, BorderLayout.CENTER);

        // ===== RIGHT: FORM CARD =====
        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setBackground(C_CARD);
        formCard.setOpaque(true);
        formCard.setBorder(cardBorder());
        formCard.setPreferredSize(new Dimension(420, 0));

        JLabel lblForm = new JLabel("Thông tin nhân viên");
        lblForm.setFont(lblForm.getFont().deriveFont(Font.BOLD, 14f));
        lblForm.setForeground(C_TEXT);

        formCard.add(lblForm, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        txtId = new JTextField();
        txtId.setEnabled(false);

        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtFullName = new JTextField();
        txtPhone = new JTextField();

        cboRole = new JComboBox<>(roleMap.keySet().toArray(new String[0]));
        cboStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        // style fields
        styleTextField(txtId);
        styleTextField(txtUsername);
        styleTextField(txtPassword);
        styleTextField(txtFullName);
        styleTextField(txtPhone);
        styleComboBox(cboRole);
        styleComboBox(cboStatus);

        int r = 0;
        addRow(form, g, r++, "User ID", txtId);
        addRow(form, g, r++, "Username", txtUsername);
        addRow(form, g, r++, "Password (để trống nếu không đổi)", txtPassword);
        addRow(form, g, r++, "Họ tên", txtFullName);
        addRow(form, g, r++, "SĐT", txtPhone);
        addRow(form, g, r++, "Role", cboRole);
        addRow(form, g, r++, "Status", cboStatus);

        formCard.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new GridLayout(3, 2, 10, 10));
        btns.setOpaque(false);

        JButton btnAdd = new JButton("Thêm");
        JButton btnUpdate = new JButton("Sửa");
        JButton btnLock = new JButton("Khóa");
        JButton btnUnlock = new JButton("Mở khóa");
        JButton btnClear = new JButton("Clear");
        JButton btnRefresh = new JButton("Reload");

        // style buttons
        styleButton(btnAdd, true);
        styleButton(btnUpdate, false);
        styleButton(btnLock, false);
        styleButton(btnUnlock, false);
        styleButton(btnClear, false);
        styleButton(btnRefresh, false);

        btns.add(btnAdd);
        btns.add(btnUpdate);
        btns.add(btnLock);
        btns.add(btnUnlock);
        btns.add(btnClear);
        btns.add(btnRefresh);

        formCard.add(btns, BorderLayout.SOUTH);

        content.add(formCard, BorderLayout.EAST);

        // =========================
        // EVENTS
        // =========================
        btnSearch.addActionListener(e -> searchSafe());
        btnReload.addActionListener(e -> loadTableSafe());

        btnRefresh.addActionListener(e -> loadTableSafe());
        btnClear.addActionListener(e -> clearForm());

        btnAdd.addActionListener(e -> addSafe());
        btnUpdate.addActionListener(e -> updateSafe());
        btnLock.addActionListener(e -> lockSafe());
        btnUnlock.addActionListener(e -> unlockSafe());

        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelectedRow();
        });

        txtSearch.addActionListener(e -> searchSafe());
    }

    private void addRow(JPanel form, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0;

        JLabel lb = new JLabel(label);
        lb.setForeground(C_TEXT);
        form.add(lb, g);

        g.gridx = 1;
        g.gridy = row;
        g.weightx = 1;
        form.add(field, g);
    }

    private void loadTableSafe() {
        try {
            List<User> list = userService.getAll();
            loadTable(list);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void searchSafe() {
        try {
            List<User> list = userService.search(txtSearch.getText());
            loadTable(list);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadTable(List<User> list) {
        model.setRowCount(0);

        // STT từ trên xuống dưới (1..n), ID giữ nguyên
        int stt = 1;
        for (User u : list) {
            model.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getPhone(),
                    u.getRoleId(),
                    u.getStatus(),
                    u.getCreatedAt()
            });
        }
        clearForm();
    }

    private void fillFormFromSelectedRow() {
        int row = tbl.getSelectedRow();
        if (row < 0) return;

        // model columns: ID(1), Username(2), FullName(3), Phone(4), Role(5), Status(6), Created(7)
        txtId.setText(String.valueOf(model.getValueAt(row, 0)));
        txtUsername.setText(String.valueOf(model.getValueAt(row, 1)));
        txtFullName.setText(String.valueOf(model.getValueAt(row, 2)));
        txtPhone.setText(String.valueOf(model.getValueAt(row, 3)));

        int roleId = Integer.parseInt(String.valueOf(model.getValueAt(row, 4)));
        setSelectedRole(roleId);

        String status = String.valueOf(model.getValueAt(row, 5));
        cboStatus.setSelectedItem(status);

        txtPassword.setText("");
        txtUsername.setEnabled(false);
        txtUsername.setBackground(new Color(241, 245, 249));
    }

    private void setSelectedRole(int roleId) {
        int idx = 0;
        for (Map.Entry<String, Integer> e : roleMap.entrySet()) {
            if (e.getValue() == roleId) {
                cboRole.setSelectedIndex(idx);
                return;
            }
            idx++;
        }
        cboRole.setSelectedIndex(0);
    }

    private int getSelectedRoleId() {
        String key = (String) cboRole.getSelectedItem();
        return roleMap.getOrDefault(key, 3);
    }

    private void clearForm() {
        txtId.setText("");
        txtUsername.setText("");
        txtUsername.setEnabled(true);
        txtUsername.setBackground(Color.WHITE);

        txtPassword.setText("");
        txtFullName.setText("");
        txtPhone.setText("");

        cboRole.setSelectedIndex(0);
        cboStatus.setSelectedItem("ACTIVE");

        tbl.clearSelection();
    }

    private void addSafe() {
        try {
            String username = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());
            String fullName = txtFullName.getText();
            String phone = txtPhone.getText();
            int roleId = getSelectedRoleId();
            String status = (String) cboStatus.getSelectedItem();

            int id = userService.add(username, pass, fullName, phone, roleId, status);
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công! ID = " + id);
            loadTableSafe();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateSafe() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Chọn 1 nhân viên trong bảng để sửa.");
                return;
            }
            int userId = Integer.parseInt(txtId.getText());
            String fullName = txtFullName.getText();
            String phone = txtPhone.getText();
            int roleId = getSelectedRoleId();
            String status = (String) cboStatus.getSelectedItem();
            String pass = new String(txtPassword.getPassword());

            boolean ok = userService.update(userId, fullName, phone, roleId, status, pass);
            JOptionPane.showMessageDialog(this, ok ? "Cập nhật thành công!" : "Cập nhật thất bại!");
            loadTableSafe();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void lockSafe() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Chọn 1 nhân viên để khóa.");
                return;
            }
            int userId = Integer.parseInt(txtId.getText());
            int c = JOptionPane.showConfirmDialog(this, "Khóa tài khoản này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;

            boolean ok = userService.lock(userId);
            JOptionPane.showMessageDialog(this, ok ? "Đã khóa!" : "Khóa thất bại!");
            loadTableSafe();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void unlockSafe() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Chọn 1 nhân viên để mở khóa.");
                return;
            }
            int userId = Integer.parseInt(txtId.getText());

            boolean ok = userService.unlock(userId);
            JOptionPane.showMessageDialog(this, ok ? "Đã mở khóa!" : "Mở khóa thất bại!");
            loadTableSafe();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        // Lỗi validate: không in đỏ console
        if (ex instanceof IllegalArgumentException) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lỗi hệ thống/DB: vẫn log để debug
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Có lỗi xảy ra. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // =========================
    // Styling helpers
    // =========================
    private CompoundBorder cardBorder() {
        return new CompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                new EmptyBorder(12, 12, 12, 12)
        );
    }

    private void styleButton(JButton b, boolean primary) {
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? C_PRIMARY : C_BORDER),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        b.setBackground(primary ? C_PRIMARY : C_CARD);
        b.setForeground(primary ? Color.WHITE : C_TEXT);
        b.setOpaque(true);

        b.addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (primary) {
                b.setBackground(m.isRollover() ? C_PRIMARY_HOVER : C_PRIMARY);
            } else {
                b.setBackground(m.isRollover() ? new Color(241, 245, 249) : C_CARD);
            }
        });
    }

    private void styleTextField(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)
        ));
        c.setOpaque(true);
        c.setBackground(Color.WHITE);
        c.setForeground(C_TEXT);
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setBorder(BorderFactory.createLineBorder(C_BORDER));
        cb.setBackground(Color.WHITE);
        cb.setOpaque(true);
    }

    private void styleTable(JTable t) {
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(C_BORDER);
        t.setSelectionBackground(C_ROW_SELECTED);
        t.setSelectionForeground(Color.WHITE);

        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(241, 245, 249));
        h.setForeground(C_TEXT);
        h.setFont(h.getFont().deriveFont(Font.BOLD));
        h.setReorderingAllowed(false);

        DefaultTableCellRenderer base = new DefaultTableCellRenderer();
        base.setOpaque(true);

        t.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, col) -> {
            Component comp = base.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                comp.setBackground(row % 2 == 0 ? Color.WHITE : C_TABLE_ALT);
                comp.setForeground(C_TEXT);
            }
            return comp;
        });
    }
}