package presentation.panels;

import bus.CustomerService;
import dto.Customer;
import presentation.dialogs.CustomerDialog;
import util.DialogUtils;
import util.RolePermission;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends JPanel {
    
    private final CustomerService customerService = new CustomerService();
    
    private JTextField txtSearch;
    private JTable table;
    private CustomerTableModel tableModel;
    private JButton btnCreate, btnRefresh;
    
    private List<Customer> customerList = new ArrayList<>();

    public CustomerPanel() {
        initComponents();
        loadData();
        applyPermissions();
    }

    private void refreshDashboard() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof presentation.MainFrame) {
            ((presentation.MainFrame) owner).refreshDashboard();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Top Panel - Search and Filter
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 40));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc SĐT...");
        txtSearch.addActionListener(e -> searchCustomers());

        JButton btnSearch = createStyledButton("Tìm kiếm", new Color(33, 150, 243), Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(120, 40));
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.addActionListener(e -> searchCustomers());

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnCreate = createStyledButton("+ Tạo khách hàng", new Color(33, 150, 243), Color.WHITE);
        btnCreate.setPreferredSize(new Dimension(180, 40));
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreate.addActionListener(e -> createCustomer());

        btnRefresh = createStyledButton("Làm mới", new Color(76, 175, 80), Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.addActionListener(e -> loadData());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnCreate);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
    }

    private JScrollPane buildTable() {
        tableModel = new CustomerTableModel();
        table = new JTable(tableModel);
        
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(33, 150, 243));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(250);  // Tên KH
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // SĐT
        table.getColumnModel().getColumn(3).setPreferredWidth(300);  // Địa chỉ
        table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Điểm
        table.getColumnModel().getColumn(5).setPreferredWidth(170);  // Thao tác (đủ chỗ cho 2 nút)
        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Center align some columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Add double-click listener
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        editCustomer();
                    }
                }
            }
        });

        // Action column uses buttons
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        return scrollPane;
    }

    private void loadData() {
        try {
            customerList = customerService.getAll();
            tableModel.setData(customerList);
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    public void refreshData() {
        loadData();
    }

    private void searchCustomers() {
        try {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                loadData();
            } else {
                customerList = customerService.search(keyword);
                tableModel.setData(customerList);
            }
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void createCustomer() {
        CustomerDialog dialog = new CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
            refreshDashboard();
        }
    }

    private void editCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtils.showWarning(this, "Vui lòng chọn khách hàng cần sửa");
            return;
        }
        
        Customer customer = tableModel.getCustomer(selectedRow);
        CustomerDialog dialog = new CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), customer);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
            refreshDashboard();
        }
    }

    private void deleteCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtils.showWarning(this, "Vui lòng chọn khách hàng cần xóa");
            return;
        }

        Customer customer = tableModel.getCustomer(selectedRow);
        
        if (DialogUtils.confirm(this, "Bạn có chắc chắn muốn xóa khách hàng \"" + customer.getCustomerName() + "\" không?")) {
            try {
                customerService.delete(customer.getCustomerId());
                DialogUtils.showInfo(this, "Xóa khách hàng thành công!");
                loadData();
                refreshDashboard();
            } catch (Exception e) {
                DialogUtils.showError(this, "Lỗi xóa khách hàng: " + e.getMessage());
            }
        }
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    private ImageIcon loadIconAny(int size, String... names) {
        for (String name : names) {
            try {
                ImageIcon raw = new ImageIcon("resources/images/" + name);
                if (raw.getIconWidth() > 0) {
                    Image img = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                }
            } catch (Exception ignore) {}
        }
        return null;
    }

    private void applyPermissions() {
        boolean canCreate = RolePermission.has("CUSTOMER_CREATE");
        btnCreate.setEnabled(true); // Tạm thời luôn enable để test
        // btnCreate.setEnabled(canCreate);
    }

    // Table Model
    private static class CustomerTableModel extends AbstractTableModel {
        private final String[] columnNames = {"STT", "Tên khách hàng", "Số điện thoại", "Địa chỉ", "Điểm", "Thao tác"};
        private List<Customer> data = new ArrayList<>();

        public void setData(List<Customer> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        public Customer getCustomer(int row) {
            return data.get(row);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Customer c = data.get(row);
            return switch (col) {
                case 0 -> row + 1;
                case 1 -> c.getCustomerName();
                case 2 -> c.getPhone();
                case 3 -> c.getAddress();
                case 4 -> c.getPoints();
                case 5 -> ""; // Để renderer hiển thị các nút
                default -> null;
            };
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == 0 || col == 4) return Integer.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 5; // Chỉ cột Thao tác có thể click
        }
    }

    // Renderer for action buttons
    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnEdit = createStyledButton("Sửa", new Color(33, 150, 243), Color.WHITE);
        private final JButton btnDelete = createStyledButton("Xóa", new Color(244, 67, 54), Color.WHITE);

        public ActionCellRenderer() {
            setOpaque(true);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            btnEdit.setFocusable(false);
            btnDelete.setFocusable(false);
            Dimension d = new Dimension(45, 24);
            btnEdit.setPreferredSize(d);
            btnDelete.setPreferredSize(d);
            add(btnEdit);
            add(Box.createRigidArea(new Dimension(6, 0)));
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    // Editor for action buttons
    private class ActionCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel();
        private final JButton btnEdit = createStyledButton("Sửa", new Color(33, 150, 243), Color.WHITE);
        private final JButton btnDelete = createStyledButton("Xóa", new Color(244, 67, 54), Color.WHITE);
        private int editingRow = -1;

        public ActionCellEditor() {
            panel.setOpaque(true);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            Dimension d = new Dimension(45, 24);
            btnEdit.setPreferredSize(d);
            btnDelete.setPreferredSize(d);
            panel.add(btnEdit);
            panel.add(Box.createRigidArea(new Dimension(6, 0)));
            panel.add(btnDelete);

            btnEdit.addActionListener(e -> {
                if (editingRow >= 0) {
                    table.setRowSelectionInterval(editingRow, editingRow);
                    editCustomer();
                }
                fireEditingStopped();
            });

            btnDelete.addActionListener(e -> {
                if (editingRow >= 0) {
                    table.setRowSelectionInterval(editingRow, editingRow);
                    deleteCustomer();
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return null; }
    }
}
