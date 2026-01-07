package presentation.panels;

import bus.PermissionService;
import dto.Permission;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class RolePermissionPanel extends JPanel {

    private final PermissionService service = new PermissionService();

    private JComboBox<RoleItem> cboRole;
    private JTable table;
    private PermissionTableModel model;
    private JButton btnSave;
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

        right.add(new JLabel("Chọn role:"));
        right.add(cboRole);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    private JComponent buildTable() {
        model = new PermissionTableModel();
        table = new JTable(model);

        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setMaxWidth(70);       // checkbox
        table.getColumnModel().getColumn(1).setPreferredWidth(220); // code
        table.getColumnModel().getColumn(2).setPreferredWidth(420); // name

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(228, 231, 236)));
        return sp;
    }

    private JComponent buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(new Color(100, 116, 139));

        btnSave = new JButton("Lưu quyền");
        btnSave.setPreferredSize(new Dimension(120, 36));
        btnSave.addActionListener(e -> onSave());

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

    // ===== helper classes =====
    private static class RoleItem {
        final int roleId;
        final String roleName;
        RoleItem(int id, String name) { this.roleId = id; this.roleName = name; }
        @Override public String toString() { return roleName + " (ID " + roleId + ")"; }
    }

    private static class PermissionTableModel extends AbstractTableModel {
        private final String[] cols = {"Allow", "Code", "Name"};

        // dữ liệu gốc
        private List<Permission> allPerms = new ArrayList<>();
        private Set<Integer> selectedIds = new HashSet<>();

        // dữ liệu hiển thị sau khi filter
        private List<Permission> viewPerms = new ArrayList<>();

        private String editingRoleName = "";

        public void setEditingRoleName(String roleName) {
            this.editingRoleName = (roleName == null) ? "" : roleName.toUpperCase();
        }

        public void setData(List<Permission> perms, Set<Integer> selectedIds) {
            this.allPerms = (perms == null) ? new ArrayList<>() : perms;
            this.selectedIds = (selectedIds == null) ? new HashSet<>() : new HashSet<>(selectedIds);

            rebuildView();
            fireTableDataChanged();
        }

        private void rebuildView() {
            viewPerms = new ArrayList<>();

            boolean isAdminRole = "ADMIN".equalsIgnoreCase(editingRoleName);

            for (Permission p : allPerms) {
                // NEW: ẩn hẳn permission ROLE_PERMISSION_MANAGE nếu role đang chỉnh != ADMIN
                if (!isAdminRole && "ROLE_PERMISSION_MANAGE".equalsIgnoreCase(p.getPermCode())) {
                    // đồng thời xóa khỏi selected để chắc chắn không lưu
                    selectedIds.remove(p.getPermId());
                    continue;
                }
                viewPerms.add(p);
            }
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

        @Override public int getRowCount() { return viewPerms.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override public Class<?> getColumnClass(int col) {
            return col == 0 ? Boolean.class : String.class;
        }

        @Override public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        @Override public Object getValueAt(int row, int col) {
            Permission p = viewPerms.get(row);
            return switch (col) {
                case 0 -> selectedIds.contains(p.getPermId());
                case 1 -> p.getPermCode();
                case 2 -> p.getPermName();
                default -> "";
            };
        }

        @Override public void setValueAt(Object aValue, int row, int col) {
            if (col != 0) return;

            Permission p = viewPerms.get(row);
            boolean checked = Boolean.TRUE.equals(aValue);

            if (checked) selectedIds.add(p.getPermId());
            else selectedIds.remove(p.getPermId());

            fireTableCellUpdated(row, col);
        }
    }
}
