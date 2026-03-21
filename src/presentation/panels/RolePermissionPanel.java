package presentation.panels;

import bus.PermissionService;
import dto.Permission;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class RolePermissionPanel extends JPanel {

    private final PermissionService service = new PermissionService();

    private JComboBox<RoleItem> cboRole;
    private JTable table;
    private PermissionTableModel model;
    private JButton btnSave;
    private JButton btnAddPerm;
    private JButton btnDeletePerm;
    private JLabel lblStatus;

    public RolePermissionPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildTop(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        loadRoles();
    }

    private JComponent buildTop() {
        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);

        JLabel title = new JLabel("Phân quyền theo Role");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(17, 24, 39));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        cboRole = new JComboBox<>();
        cboRole.setPreferredSize(new Dimension(240, 34));
        cboRole.addActionListener(e -> onRoleChanged());

        btnAddPerm = new JButton("+ Thêm quyền");
        btnAddPerm.setPreferredSize(new Dimension(140, 34));
        btnAddPerm.setBackground(new Color(76, 175, 80));
        btnAddPerm.setForeground(Color.WHITE);
        btnAddPerm.setFocusPainted(false);
        btnAddPerm.setBorderPainted(false);
        btnAddPerm.setOpaque(true);
        btnAddPerm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddPerm.addActionListener(e -> onAddPermission());

        btnDeletePerm = new JButton("- Xóa quyền");
        btnDeletePerm.setPreferredSize(new Dimension(140, 34));
        btnDeletePerm.setBackground(new Color(244, 67, 54));
        btnDeletePerm.setForeground(Color.WHITE);
        btnDeletePerm.setFocusPainted(false);
        btnDeletePerm.setBorderPainted(false);
        btnDeletePerm.setOpaque(true);
        btnDeletePerm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeletePerm.addActionListener(e -> onDeletePermission());

        right.add(new JLabel("Chọn role:"));
        right.add(cboRole);
        right.add(btnAddPerm);
        right.add(btnDeletePerm);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    private JComponent buildTable() {
        model = new PermissionTableModel();
        table = new JTable(model);

        table.setRowHeight(40); // cao hơn cho thoáng
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

// Font chữ to hơn
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setForeground(new Color(33, 37, 41));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(232, 245, 255));
        table.setSelectionForeground(new Color(33, 37, 41));

// Kẻ ô rõ ràng
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 224, 230));
        table.setIntercellSpacing(new Dimension(1, 1));

// Bỏ viền focus mặc định nhìn cho sạch
        table.setFocusable(false);

        // Custom checkbox renderer và editor để canh giữa
        JCheckBox checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(JCheckBox.CENTER);

        table.setDefaultRenderer(Boolean.class, new TableCellRenderer() {
            private final JCheckBox cb = new JCheckBox();
            private final JPanel emptyPanel = new JPanel(new GridBagLayout());

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Color bg = isSelected
                        ? table.getSelectionBackground()
                        : Color.WHITE;

                if (value == null) {
                    emptyPanel.setBackground(bg);
                    return emptyPanel;
                }

                cb.setSelected(Boolean.TRUE.equals(value));
                cb.setHorizontalAlignment(JCheckBox.CENTER);
                cb.setBackground(bg);
                cb.setOpaque(true);
                cb.setEnabled(true);
                return cb;
            }
        });

        table.setDefaultEditor(Boolean.class, new DefaultCellEditor(checkBox));

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setBackground(new Color(33, 150, 243));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));

        table.getColumnModel().getColumn(0).setPreferredWidth(500);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col <= 0) return;
                if (!model.isCellEditable(row, col)) return;

                Object value = model.getValueAt(row, col);
                boolean current = Boolean.TRUE.equals(value);
                model.setValueAt(!current, row, col);
            }
        });

        table.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
            private final JLabel label = new JLabel();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                label.setText(value == null ? "" : value.toString());
                label.setOpaque(true);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                label.setForeground(new Color(33, 37, 41));
                return label;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(228, 231, 236)));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel lblSearch = new JLabel("Tìm quyền:");
        JTextField txtSearch = new JTextField(20);
        searchPanel.add(lblSearch, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        // Wrapper table + search
        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(sp, BorderLayout.CENTER);

        // Search functionality
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateFilter(txtSearch.getText()); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateFilter(txtSearch.getText()); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateFilter(txtSearch.getText()); }

            private void updateFilter(String searchText) {
                model.setSearchFilter(searchText.toLowerCase().trim());
            }
        });

        return tablePanel;
    }

    private JComponent buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(new Color(100, 116, 139));

        btnSave = new JButton("Lưu quyền");
        btnSave.setPreferredSize(new Dimension(120, 36));
        btnSave.setBackground(new Color(33, 150, 243));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setOpaque(true);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> onSave());
        btnSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSave.setBackground(new Color(76, 175, 80));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSave.setBackground(new Color(33, 150, 243));
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnSave);

        bottom.add(lblStatus, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);

        return bottom;
    }

    private void loadRoles() {
        Map<Integer, String> roles = service.getAllRoles();
        DefaultComboBoxModel<RoleItem> cbModel = new DefaultComboBoxModel<>();
        for (Map.Entry<Integer, String> e : roles.entrySet()) {
            cbModel.addElement(new RoleItem(e.getKey(), e.getValue()));
        }
        cboRole.setModel(cbModel);

        if (cbModel.getSize() > 0) {
            cboRole.setSelectedIndex(0);
            onRoleChanged();
        }
    }

    private void onRoleChanged() {
        RoleItem role = (RoleItem) cboRole.getSelectedItem();
        if (role == null) return;

        List<Permission> allPerms = service.getAllPermissions();
        Set<Integer> rolePermIds = service.getPermIdsOfRole(role.roleId);

        // NEW: role context để model quyết định ẩn permission đặc biệt
        model.setEditingRoleName(role.roleName);
        model.setData(allPerms, rolePermIds);

        lblStatus.setText("Đang chỉnh quyền cho role: " + role.roleName + " (ID=" + role.roleId + ")");
    }

    private void onSave() {
        RoleItem role = (RoleItem) cboRole.getSelectedItem();
        if (role == null) return;

        // Commit checkbox edit đang mở trước khi đọc dữ liệu để lưu
        if (table != null && table.isEditing() && table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }

        // Anti privilege escalation (chặn lách):
        // role != ADMIN thì tuyệt đối không được lưu ROLE_PERMISSION_MANAGE
        if (!"ADMIN".equalsIgnoreCase(role.roleName)) {
            model.removeByCode("ROLE_PERMISSION_MANAGE");
        }

        Set<Integer> selectedPermIds = model.getSelectedPermIds();

        try {
            service.saveRolePermissions(role.roleId, selectedPermIds);
            lblStatus.setText(" Đã lưu quyền cho role: " + role.roleName + ". Hãy logout/login để áp dụng.");
            JOptionPane.showMessageDialog(this,
                    "Lưu thành công!\nHãy logout/login để quyền áp dụng vào menu.",
                    "OK",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lưu thất bại: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddPermission() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm quyền mới", true);
        dialog.setSize(380, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblName = new JLabel("Tên quyền:");
        JTextField txtName = new JTextField(25);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        content.add(lblName, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        content.add(txtName, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnOK = new JButton("Thêm");
        JButton btnCancel = new JButton("Hủy");
        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.SOUTHEAST;
        content.add(btnPanel, gbc);

        btnOK.addActionListener(e -> {
            String name = txtName.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập tên quyền", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Tự động sinh mã từ tên: chuyển thành chữ hoa, thay khoảng trắng bằng gạch dưới
                String code = name.toUpperCase()
                        .replaceAll("\\s+", "_")
                        .replaceAll("[^A-Z0-9_]", "");

                Permission newPerm = new Permission(0, code, name);
                service.addPermission(newPerm);
                JOptionPane.showMessageDialog(dialog, "Thêm quyền thành công!\nMã: " + code, "OK", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

                // Reload lại dữ liệu
                RoleItem role = (RoleItem) cboRole.getSelectedItem();
                if (role != null) {
                    List<Permission> allPerms = service.getAllPermissions();
                    Set<Integer> rolePermIds = service.getPermIdsOfRole(role.roleId);
                    model.setEditingRoleName(role.roleName);
                    model.setData(allPerms, rolePermIds);
                    table.repaint();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi thêm quyền: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.add(content);
        dialog.setVisible(true);
    }

    private void onDeletePermission() {
        List<Permission> allPerms = service.getAllPermissions();
        if (allPerms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có quyền nào để xóa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xóa quyền", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm:");
        JTextField txtSearch = new JTextField(20);
        searchPanel.add(lblSearch, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        JLabel lblInfo = new JLabel("Chọn quyền muốn xóa:");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Sử dụng DefaultListModel để có thể update list khi search
        DefaultListModel<Permission> listModel = new DefaultListModel<>();
        for (Permission p : allPerms) {
            listModel.addElement(p);
        }

        JList<Permission> permList = new JList<>(listModel);
        permList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getPermCode() + " - " + value.getPermName());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            if (isSelected) {
                label.setBackground(new Color(33, 150, 243));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);
            }
            return label;
        });
        permList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(permList);

        // Search functionality
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterList(); }

            private void filterList() {
                String searchText = txtSearch.getText().toLowerCase().trim();
                listModel.clear();
                for (Permission p : allPerms) {
                    if (p.getPermCode().toLowerCase().contains(searchText) || 
                        p.getPermName().toLowerCase().contains(searchText)) {
                        listModel.addElement(p);
                    }
                }
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(lblInfo, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnOK = new JButton("Xóa");
        JButton btnCancel = new JButton("Hủy");
        btnOK.setBackground(new Color(244, 67, 54));
        btnOK.setForeground(Color.WHITE);
        btnOK.setFocusPainted(false);
        btnOK.setBorderPainted(false);
        btnOK.setOpaque(true);
        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);

        content.add(topPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        btnOK.addActionListener(e -> {
            Permission selectedPerm = permList.getSelectedValue();
            if (selectedPerm == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn quyền để xóa", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Bạn có chắc muốn xóa quyền:\n" + selectedPerm.getPermCode() + " - " + selectedPerm.getPermName() + "?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                service.deletePermission(selectedPerm.getPermId());
                JOptionPane.showMessageDialog(dialog, "Xóa quyền thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

                // Reload lại dữ liệu
                RoleItem role = (RoleItem) cboRole.getSelectedItem();
                if (role != null) {
                    List<Permission> newPerms = service.getAllPermissions();
                    Set<Integer> rolePermIds = service.getPermIdsOfRole(role.roleId);
                    model.setEditingRoleName(role.roleName);
                    model.setData(newPerms, rolePermIds);
                    table.repaint();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi xóa quyền: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.add(content);
        dialog.setVisible(true);
    }

    // ===== helper classes =====
    private static class RoleItem {
        final int roleId;
        final String roleName;
        RoleItem(int id, String name) { this.roleId = id; this.roleName = name; }
        @Override public String toString() { return roleName + " (ID " + roleId + ")"; }
    }

    private static class PermissionTableModel extends AbstractTableModel {
        private static final int COL_PERMISSION = 0;
        private static final int COL_VIEW = 1;
        private static final int COL_ADD = 2;
        private static final int COL_EDIT = 3;
        private static final int COL_DELETE = 4;

        private final String[] cols = {"Quyền", "Xem", "Thêm", "Sửa", "Xóa"};

        private List<Permission> allPerms = new ArrayList<>();
        private Set<Integer> selectedIds = new HashSet<>();
        private List<PermissionRow> rows = new ArrayList<>();
        private List<PermissionRow> filteredRows = new ArrayList<>();
        private String searchFilter = "";

        private String editingRoleName = "";

        private static class PermissionRow {
            String resourceKey;
            String displayName;
            Set<Integer> viewIds = new HashSet<>();
            Set<Integer> addIds = new HashSet<>();
            Set<Integer> editIds = new HashSet<>();
            Set<Integer> deleteIds = new HashSet<>();
            Set<Integer> searchIds = new HashSet<>();
        }

        public void setEditingRoleName(String roleName) {
            this.editingRoleName = (roleName == null) ? "" : roleName.toUpperCase();
        }

        public void setData(List<Permission> perms, Set<Integer> selectedIds) {
            this.allPerms = (perms == null) ? new ArrayList<>() : perms;
            this.selectedIds = (selectedIds == null) ? new HashSet<>() : new HashSet<>(selectedIds);
            this.searchFilter = "";

            rebuildView();
            fireTableDataChanged();
        }

        private void rebuildView() {
            rows = new ArrayList<>();

            boolean isAdminRole = "ADMIN".equalsIgnoreCase(editingRoleName);
            Map<String, PermissionRow> rowMap = new LinkedHashMap<>();

            for (Permission p : allPerms) {
                if (p == null || p.getPermCode() == null) continue;

                String code = p.getPermCode().toUpperCase(Locale.ROOT);

                // Role khác ADMIN: không được giữ quyền phân quyền
                if (!isAdminRole && "ROLE_PERMISSION_MANAGE".equalsIgnoreCase(code)) {
                    selectedIds.remove(p.getPermId());
                }

                String resourceKey = extractResourceKey(code);

                // ADMIN: ẩn dòng "Phân quyền" khỏi giao diện
                if (isAdminRole && "ROLE_PERMISSION".equalsIgnoreCase(resourceKey)) {
                    continue;
                }

                ActionBucket action = mapAction(code, resourceKey);

                PermissionRow row = rowMap.computeIfAbsent(resourceKey, key -> {
                    PermissionRow r = new PermissionRow();
                    r.resourceKey = key;
                    r.displayName = toDisplayName(key);
                    return r;
                });

                switch (action) {
                    case VIEW -> row.viewIds.add(p.getPermId());
                    case ADD -> row.addIds.add(p.getPermId());
                    case EDIT -> row.editIds.add(p.getPermId());
                    case DELETE -> row.deleteIds.add(p.getPermId());
                    case SEARCH -> row.searchIds.add(p.getPermId());
                }
            }

            rows.addAll(rowMap.values());
            applyFilter();
        }

        public void setSearchFilter(String searchText) {
            this.searchFilter = (searchText == null) ? "" : searchText.toLowerCase().trim();
            applyFilter();
            fireTableDataChanged();
        }

        private void applyFilter() {
            filteredRows = new ArrayList<>();
            if (searchFilter.isEmpty()) {
                filteredRows.addAll(rows);
            } else {
                for (PermissionRow row : rows) {
                    if (row.displayName.toLowerCase().contains(searchFilter) || 
                        row.resourceKey.toLowerCase().contains(searchFilter)) {
                        filteredRows.add(row);
                    }
                }
            }
        }

        private enum ActionBucket {
            VIEW, ADD, EDIT, DELETE, SEARCH
        }

        private String extractResourceKey(String code) {
            int idx = code.lastIndexOf('_');
            if (idx <= 0) return code;
            return code.substring(0, idx);
        }

        private ActionBucket mapAction(String code, String resourceKey) {
            if (code == null) return ActionBucket.EDIT;

            String upper = code.toUpperCase(Locale.ROOT).trim();

            // Kiểm kho hiện dùng ADJUSTMENT_APPROVE thay cho quyền thao tác xóa/hủy
            if ("ADJUSTMENT".equals(resourceKey) && upper.endsWith("_APPROVE")) {
                return ActionBucket.DELETE;
            }

            if (upper.endsWith("_VIEW")) return ActionBucket.VIEW;
            if (upper.endsWith("_ADD") || upper.endsWith("_CREATE")) return ActionBucket.ADD;
            if (upper.endsWith("_EDIT") || upper.endsWith("_UPDATE")) return ActionBucket.EDIT;
            if (upper.endsWith("_DELETE") || upper.endsWith("_REMOVE")) return ActionBucket.DELETE;
            if (upper.endsWith("_SEARCH")) return ActionBucket.SEARCH;

            return ActionBucket.EDIT;
        }

        private String toDisplayName(String key) {
            return switch (key) {
                case "CATEGORY" -> "Danh mục";
                case "CUSTOMER" -> "Khách hàng";
                case "PRODUCT" -> "Sản phẩm";
                case "SUPPLIER" -> "Nhà cung cấp";
                case "DASHBOARD" -> "Dashboard";
                case "LOT" -> "Tồn theo lô";
                case "PAYMENT" -> "Thanh toán";
                case "LOYALTY" -> "Điểm tích lũy";
                case "POS" -> "Bán hàng POS";
                case "PROMOTION" -> "Khuyến mãi";
                case "RECEIPT" -> "Phiếu nhập";
                case "REPORT" -> "Báo cáo";
                case "INVOICE" -> "Hóa đơn";
                case "USER" -> "Người dùng";
                case "AUDIT" -> "Lịch sử";
                case "ADJUSTMENT" -> "Kiểm kho";
                case "ROLE_PERMISSION" -> "Phân quyền";
                default -> key.replace('_', ' ');
            };
        }


        private Set<Integer> getCellPermIds(PermissionRow r, int col) {
            if (r == null) return Collections.emptySet();

            switch (col) {
                case COL_VIEW:
                    return r.viewIds != null ? r.viewIds : Collections.emptySet();
                case COL_ADD:
                    return r.addIds != null ? r.addIds : Collections.emptySet();
                case COL_EDIT:
                    return r.editIds != null ? r.editIds : Collections.emptySet();
                case COL_DELETE:
                    return r.deleteIds != null ? r.deleteIds : Collections.emptySet();
                default:
                    return Collections.emptySet();
            }
        }

        private boolean isChecked(Set<Integer> permIds) {
            if (permIds == null || permIds.isEmpty()) return false;
            for (Integer id : permIds) {
                if (selectedIds.contains(id)) return true;
            }
            return false;
        }

        public Set<Integer> getSelectedPermIds() {
            return new HashSet<>(selectedIds);
        }

        public void removeByCode(String permCode) {
            if (permCode == null) return;
            for (Permission p : allPerms) {
                if (permCode.equalsIgnoreCase(p.getPermCode())) {
                    selectedIds.remove(p.getPermId());
                    break;
                }
            }
            // nếu đang hiển thị role != ADMIN thì nó vốn đã bị ẩn; rebuild cho chắc
            rebuildView();
            fireTableDataChanged();
        }

        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override public int getRowCount() { return filteredRows.size(); }

        @Override public Class<?> getColumnClass(int col) {
            return col == COL_PERMISSION ? String.class : Boolean.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == COL_PERMISSION) return false;

            PermissionRow r = filteredRows.get(row);
            Set<Integer> ids = getCellPermIds(r, col);

            return ids != null && !ids.isEmpty();
        }

        @Override
        public Object getValueAt(int row, int col) {
            PermissionRow r = filteredRows.get(row);

            if (col == COL_PERMISSION) return r.displayName;

            Set<Integer> ids = getCellPermIds(r, col);

            if (ids == null || ids.isEmpty()) {
                return null; // không có chức năng này -> ẩn checkbox
            }

            return isChecked(ids);
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            if (col == COL_PERMISSION) return;

            PermissionRow displayedRow = filteredRows.get(row);
            Set<Integer> ids = getCellPermIds(displayedRow, col);

            if (ids == null || ids.isEmpty()) return;

            boolean checked = Boolean.TRUE.equals(aValue);

            // 1) Nếu tick cột hiện tại
            if (checked) {
                selectedIds.addAll(ids);

                // Nếu tick Thêm / Sửa / Xóa -> tự bật Xem
                if (col == COL_ADD || col == COL_EDIT || col == COL_DELETE) {
                    Set<Integer> viewIds = getCellPermIds(displayedRow, COL_VIEW);
                    if (viewIds != null && !viewIds.isEmpty()) {
                        selectedIds.addAll(viewIds);
                    }
                }
            } else {
                selectedIds.removeAll(ids);

                // 2) Nếu bỏ Xem -> bỏ luôn Thêm / Sửa / Xóa
                if (col == COL_VIEW) {
                    Set<Integer> addIds = getCellPermIds(displayedRow, COL_ADD);
                    Set<Integer> editIds = getCellPermIds(displayedRow, COL_EDIT);
                    Set<Integer> deleteIds = getCellPermIds(displayedRow, COL_DELETE);

                    if (addIds != null) selectedIds.removeAll(addIds);
                    if (editIds != null) selectedIds.removeAll(editIds);
                    if (deleteIds != null) selectedIds.removeAll(deleteIds);
                }
            }

            fireTableRowsUpdated(row, row);
        }
    }
}
