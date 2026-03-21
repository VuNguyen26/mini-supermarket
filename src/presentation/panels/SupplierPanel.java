package presentation.panels;

import bus.SupplierService;
import dto.Supplier;
import presentation.dialogs.SupplierDialog;
import util.DialogUtils;
import util.ExcelUtils;
import util.RolePermission;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SupplierPanel extends JPanel {

    private final SupplierService supplierService = new SupplierService();

    private JTextField txtSearch;
    private JTable table;
    private SupplierTableModel tableModel;
    private JButton btnCreate, btnRefresh, btnSearch, btnImport, btnExport;

    private boolean canView = false;
    private boolean canCreate = false;
    private boolean canUpdate = false;
    private boolean canDelete = false;

    private List<Supplier> supplierList = new ArrayList<>();

    public SupplierPanel() {
        initPermissions();
        initComponents();
        loadData();
        applyPermissions();
    }

    private void initPermissions() {
        canView = RolePermission.has("SUPPLIER_VIEW");
        canCreate = RolePermission.has("SUPPLIER_CREATE");
        canUpdate = RolePermission.has("SUPPLIER_EDIT");
        canDelete = RolePermission.has("SUPPLIER_DELETE");
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

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 40));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã, tên hoặc SĐT...");
        txtSearch.addActionListener(e -> searchSuppliers());

        btnSearch = createStyledButton("Tìm kiếm", new Color(33, 150, 243), Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(120, 40));
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.addActionListener(e -> searchSuppliers());

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnCreate = createStyledButton("+ Tạo nhà cung cấp", new Color(33, 150, 243), Color.WHITE);
        btnCreate.setPreferredSize(new Dimension(180, 40));
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreate.addActionListener(e -> createSupplier());

        btnRefresh = createStyledButton("Làm mới", new Color(76, 175, 80), Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.addActionListener(e -> loadData());

        btnImport = createStyledButton("Import Excel", new Color(255, 152, 0), Color.WHITE);
        btnImport.setPreferredSize(new Dimension(140, 40));
        btnImport.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnImport.addActionListener(e -> importSuppliersFromExcel());

        btnExport = createStyledButton("Export Excel", new Color(0, 150, 136), Color.WHITE);
        btnExport.setPreferredSize(new Dimension(140, 40));
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExport.addActionListener(e -> exportSuppliersToExcel());

        buttonPanel.add(btnExport);
        buttonPanel.add(btnImport);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnCreate);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
    }

    private JScrollPane buildTable() {
        tableModel = new SupplierTableModel();
        table = new JTable(tableModel);

        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(33, 150, 243));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(110);  // Mã NCC
        table.getColumnModel().getColumn(2).setPreferredWidth(220);  // Tên nhà cung cấp
        table.getColumnModel().getColumn(3).setPreferredWidth(130);  // SĐT
        table.getColumnModel().getColumn(4).setPreferredWidth(180);  // Email
        table.getColumnModel().getColumn(5).setPreferredWidth(280);  // Địa chỉ
        table.getColumnModel().getColumn(6).setPreferredWidth(170);  // Thao tác

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        if (!canUpdate) {
                            DialogUtils.showWarning(SupplierPanel.this, "Bạn không có quyền sửa nhà cung cấp");
                            return;
                        }
                        editSupplier();
                    }
                }
            }
        });

        table.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        return scrollPane;
    }

    private void loadData() {
        if (!canView) {
            return;
        }

        try {
            supplierList = supplierService.getAll();
            tableModel.setData(supplierList);
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void searchSuppliers() {
        if (!canView) {
            DialogUtils.showWarning(this, "Bạn không có quyền xem nhà cung cấp");
            return;
        }

        try {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                loadData();
            } else {
                supplierList = supplierService.search(keyword);
                tableModel.setData(supplierList);
            }
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void createSupplier() {
        if (!canCreate) {
            DialogUtils.showWarning(this, "Bạn không có quyền thêm nhà cung cấp");
            return;
        }

        SupplierDialog dialog = new SupplierDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadData();
            refreshDashboard();
        }
    }

    private void importSuppliersFromExcel() {
        if (!canCreate) {
            DialogUtils.showWarning(this, "Bạn không có quyền import nhà cung cấp");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel nhà cung cấp");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        int successCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String code = formatter.formatCellValue(row.getCell(0)).trim();
                String name = formatter.formatCellValue(row.getCell(1)).trim();
                String phone = formatter.formatCellValue(row.getCell(2)).trim();
                String email = formatter.formatCellValue(row.getCell(3)).trim();
                String address = formatter.formatCellValue(row.getCell(4)).trim();

                if (code.isEmpty() && name.isEmpty() && phone.isEmpty() && email.isEmpty() && address.isEmpty()) {
                    continue;
                }

                try {
                    Supplier s = new Supplier();
                    s.setSupplierCode(code);
                    s.setSupplierName(name);
                    s.setPhone(phone);
                    s.setEmail(email);
                    s.setAddress(address);

                    supplierService.create(s);
                    successCount++;
                } catch (Exception ex) {
                    failedCount++;
                    errors.add("Dòng " + (i + 1) + ": " + ex.getMessage());
                }
            }

            loadData();
            refreshDashboard();

            StringBuilder msg = new StringBuilder();
            msg.append("Import hoàn tất. Thành công: ").append(successCount)
                    .append(", thất bại: ").append(failedCount);

            if (!errors.isEmpty()) {
                msg.append("\n\nLỗi mẫu:\n");
                int max = Math.min(5, errors.size());
                for (int i = 0; i < max; i++) {
                    msg.append("- ").append(errors.get(i)).append("\n");
                }
            }

            JOptionPane.showMessageDialog(this, msg.toString(), "Kết quả import", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi import Excel: " + e.getMessage());
        }
    }

    private void exportSuppliersToExcel() {
        if (!canView) {
            DialogUtils.showWarning(this, "Bạn không có quyền xem nhà cung cấp để export");
            return;
        }

        try {
            List<Supplier> dataToExport = supplierList != null ? supplierList : new ArrayList<>();
            if (dataToExport.isEmpty()) {
                DialogUtils.showWarning(this, "Không có dữ liệu nhà cung cấp để export");
                return;
            }

            File file = ExcelUtils.chooseSaveXlsxFile(this, "suppliers_export.xlsx");
            if (file == null) return;

            List<String> headers = Arrays.asList("Mã NCC", "Tên nhà cung cấp", "Số điện thoại", "Email", "Địa chỉ");
            List<List<Object>> rows = new ArrayList<>();

            for (Supplier s : dataToExport) {
                rows.add(Arrays.asList(
                        s.getSupplierCode(),
                        s.getSupplierName(),
                        s.getPhone(),
                        s.getEmail(),
                        s.getAddress()
                ));
            }

            ExcelUtils.exportXlsx(file, "Suppliers", headers, rows);
            DialogUtils.showInfo(this, "Export Excel thành công: " + file.getAbsolutePath());
        } catch (Exception e) {
            DialogUtils.showError(this, "Lỗi export Excel: " + e.getMessage());
        }
    }

    private void editSupplier() {
        if (!canUpdate) {
            DialogUtils.showWarning(this, "Bạn không có quyền sửa nhà cung cấp");
            return;
        }

        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtils.showWarning(this, "Vui lòng chọn nhà cung cấp cần sửa");
            return;
        }

        Supplier supplier = tableModel.getSupplier(selectedRow);
        SupplierDialog dialog = new SupplierDialog((Frame) SwingUtilities.getWindowAncestor(this), supplier);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadData();
            refreshDashboard();
        }
    }

    private void deleteSupplier() {
        if (!canDelete) {
            DialogUtils.showWarning(this, "Bạn không có quyền xóa nhà cung cấp");
            return;
        }

        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtils.showWarning(this, "Vui lòng chọn nhà cung cấp cần xóa");
            return;
        }

        Supplier supplier = tableModel.getSupplier(selectedRow);

        if (DialogUtils.confirm(this, "Bạn có chắc chắn muốn xóa nhà cung cấp \"" + supplier.getSupplierName() + "\" không?")) {
            try {
                supplierService.delete(supplier.getSupplierId());
                DialogUtils.showInfo(this, "Xóa nhà cung cấp thành công!");
                loadData();
                refreshDashboard();
            } catch (Exception e) {
                DialogUtils.showError(this, "Lỗi xóa nhà cung cấp: " + e.getMessage());
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

    private void applyPermissions() {
        if (!canView) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bạn không có quyền xem Nhà cung cấp.",
                    "Từ chối truy cập",
                    JOptionPane.WARNING_MESSAGE
            );
            setVisible(false);
            return;
        }

        btnSearch.setEnabled(true);
        btnRefresh.setEnabled(true);

        btnCreate.setVisible(canCreate);
        btnCreate.setEnabled(canCreate);

        if (btnImport != null) {
            btnImport.setVisible(canCreate);
            btnImport.setEnabled(canCreate);
        }

        if (btnExport != null) {
            btnExport.setVisible(canView);
            btnExport.setEnabled(canView);
        }

        if (table != null) {
            table.repaint();
            table.revalidate();
        }
    }

    private static class SupplierTableModel extends AbstractTableModel {
        private final String[] columnNames = {"STT", "Mã NCC", "Tên nhà cung cấp", "Số điện thoại", "Email", "Địa chỉ", "Thao tác"};
        private List<Supplier> data = new ArrayList<>();

        public void setData(List<Supplier> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        public Supplier getSupplier(int row) {
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
            Supplier s = data.get(row);
            return switch (col) {
                case 0 -> row + 1;
                case 1 -> s.getSupplierCode();
                case 2 -> s.getSupplierName();
                case 3 -> s.getPhone();
                case 4 -> s.getEmail();
                case 5 -> s.getAddress();
                case 6 -> "";
                default -> null;
            };
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == 0) return Integer.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 6;
        }
    }

    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnEdit = createStyledButton("Sửa", new Color(33, 150, 243), Color.WHITE);
        private final JButton btnDelete = createStyledButton("Xóa", new Color(244, 67, 54), Color.WHITE);

        public ActionCellRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
            btnEdit.setFocusable(false);
            btnDelete.setFocusable(false);
            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (canUpdate) {
                add(btnEdit);
            }

            if (canDelete) {
                add(btnDelete);
            }

            revalidate();
            repaint();
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        private final JButton btnEdit = createStyledButton("Sửa", new Color(33, 150, 243), Color.WHITE);
        private final JButton btnDelete = createStyledButton("Xóa", new Color(244, 67, 54), Color.WHITE);
        private int editingRow = -1;

        public ActionCellEditor() {
            btnEdit.addActionListener(e -> {
                if (editingRow >= 0 && canUpdate) {
                    table.setRowSelectionInterval(editingRow, editingRow);
                    editSupplier();
                }
                fireEditingStopped();
            });

            btnDelete.addActionListener(e -> {
                if (editingRow >= 0 && canDelete) {
                    table.setRowSelectionInterval(editingRow, editingRow);
                    deleteSupplier();
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            panel.removeAll();
            panel.setOpaque(true);
            panel.setBackground(table.getSelectionBackground());

            if (canUpdate) {
                panel.add(btnEdit);
            }

            if (canDelete) {
                panel.add(btnDelete);
            }

            panel.revalidate();
            panel.repaint();
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}