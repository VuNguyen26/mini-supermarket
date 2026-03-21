package presentation.panels;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import dto.*;
import util.*;
import bus.*;
import bus.AuthService.AuthUser;
import presentation.components.datechooser.*;
import presentation.components.datechooser.listener.*;
import presentation.dialogs.ReceiptDialog;

public class GoodsReceiptPanel extends JPanel {

    private final GoodsReceiptService grService = new GoodsReceiptService();
    private final SupplierService supplierService = new SupplierService();
    private final Runnable inventoryUpdatedCallback;

    private AuthUser currentUser;

    private JTable grTable;
    private GoodsReceiptTableModel grTableModel;

    private JComboBox<SupplierFilterItem> supplierFilterCbo;
    private JComboBox<DateFilterItem> dateFilterCbo;
    private DateChooser dateFitlerDc;
    private JTextField dateFilterTxt;

    private boolean canView;
    private boolean canCreate;
    private boolean canUpdate;
    private boolean canDelete;

    private JButton addBtn;

    public GoodsReceiptPanel(AuthUser currentUser, Runnable inventoryUpdatedCallback) {
        this.currentUser = currentUser;
        this.inventoryUpdatedCallback = inventoryUpdatedCallback;

        initPermissions();

        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        add(buildTop(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        applyPermissions();
    }

    private boolean hasPermission(String code) {
        return RolePermission.has(code);
    }

    private void initPermissions() {
        canView   = hasPermission("RECEIPT_VIEW");
        canCreate = hasPermission("RECEIPT_CREATE");
        canUpdate = hasPermission("RECEIPT_UPDATE");
        canDelete = hasPermission("RECEIPT_DELETE");
    }

    private void applyPermissions() {
        if (addBtn != null) {
            addBtn.setVisible(canCreate);
            addBtn.setEnabled(canCreate);
        }
    }

    // Open the dialog for GR detail or create new GR
    private void openViewDetailDialog(GoodsReceipt receipt) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ReceiptDialog dialog = new ReceiptDialog(owner, currentUser, receipt, inventoryUpdatedCallback);
        dialog.setVisible(true);
    }
    // ==============================================

    private JComponent buildTop() {
        // West side of top panel: Filter label, Date and supplier filter combo box
        dateFilterCbo = getDateFilterCbo();
        supplierFilterCbo = getSupplierFilterCbo();

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        west.setOpaque(false);
        west.add(new JLabel("Lọc theo:"));
        west.add(dateFilterCbo);
        west.add(supplierFilterCbo);
        // ========================================================================

        // East side of top panel: Refresh and Create new receipt button
        JButton refreshBtn = new JButton("Làm mới");
        refreshBtn.setBackground(new Color(33, 150, 243));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(140, 40));
        refreshBtn.addActionListener(e -> {
            dateFilterCbo.setSelectedIndex(0);
            supplierFilterCbo.setSelectedIndex(0);
            grTableModel.loadData();
        });

        addBtn = new JButton("Tạo phiếu mới");
        addBtn.setBackground(new Color(76, 175, 80));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(140, 40));
        addBtn.addActionListener(e -> {
            if (!canCreate) {
                JOptionPane.showMessageDialog(
                        GoodsReceiptPanel.this,
                        "Bạn không có quyền tạo phiếu nhập!",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            openViewDetailDialog(null);
        });

        JPanel east = new JPanel();
        east.setOpaque(false);
        east.add(refreshBtn);
        east.add(addBtn);
        // ==============================================================

        // South side of top panel: Custom date filter (hide on default)
        dateFilterTxt = new JTextField(16);
        dateFilterTxt.setVisible(false);
        dateFilterTxt.setOpaque(false);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        south.add(dateFilterTxt);
        // =============================================================

        // Top panel
        dateFitlerDc = new DateChooser();
        dateFitlerDc.setTextField(dateFilterTxt);
        dateFitlerDc.setDateSelectionMode(DateChooser.DateSelectionMode.BETWEEN_DATE_SELECTED);
        dateFitlerDc.last28Days();
        dateFitlerDc.addActionDateChooserListener(new DateChooserAdapter() {
            @Override
            public void dateBetweenChanged(DateBetween date, DateChooserAction action) {
                grTableModel.from = date.getFromLocalDateTime();
                grTableModel.to = date.getToLocalDateTime();
                grTableModel.loadData();
            }
        });

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);
        top.add(south, BorderLayout.SOUTH);
        top.add(west, BorderLayout.WEST);
        top.add(east, BorderLayout.EAST);
        // =========
        return top;
    }

    private JComponent buildTable() {
        // Initialize the GR table
        grTableModel = new GoodsReceiptTableModel();
        grTable = new JTable(grTableModel);
        // =======================

        // Set max width for the index column
        grTable.getColumnModel().getColumn(0).setMaxWidth(50);
        // ==================================

        // Styling whole table
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        grTable.setDefaultRenderer(Object.class, center);
        grTable.setRowHeight(40);
        grTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        grTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // ===================

        // Open GR detail dialog on double click
        grTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (!canView) {
                        JOptionPane.showMessageDialog(
                                GoodsReceiptPanel.this,
                                "Bạn không có quyền xem phiếu nhập!",
                                "Thông báo",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    int row = grTable.getSelectedRow();
                    if (row != -1) {
                        GoodsReceipt selected = grTableModel.getReceiptAt(row);
                        openViewDetailDialog(selected);
                    }
                }
            }
        });
        // =====================================

        // Styling table's header
        grTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        grTable.getTableHeader().setBackground(new Color(33, 150, 243));
        grTable.getTableHeader().setForeground(Color.WHITE);
        grTable.getTableHeader().setCursor(new Cursor(Cursor.HAND_CURSOR));
        grTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        // ======================

        // Toggle sorting order of the corresponding column on click
        grTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = grTable.columnAtPoint(e.getPoint());
                GoodsReceiptSort newSort = switch (col) {
                    case 1 -> GoodsReceiptSort.SUPPLIER;
                    case 2 -> GoodsReceiptSort.USER;
                    case 3 -> GoodsReceiptSort.CREATED_AT;
                    case 4 -> GoodsReceiptSort.TOTAL_AMOUNT;
                    default -> null;
                };

                if (newSort == null) return;
                if (newSort == grTableModel.sortBy) {
                    grTableModel.toggleAscending();
                } else {
                    grTableModel.sortBy = newSort;
                    grTableModel.isAscending = true;
                }
                grTableModel.loadData();
            }
        });
        // =========================================================

        // The scrollable pane containing the GR table
        JScrollPane scrollPane = new JScrollPane(grTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(228, 231, 236)));
        return scrollPane;
        // ===========================================
    }

    // The table model for the GR table
    private class GoodsReceiptTableModel extends AbstractTableModel {
        private final String[] cols = { "STT", "Nhà cung cấp", "Người tạo", "Ngày nhập", "Tổng tiền", "Ghi chú" };
        public List<GoodsReceipt> receipts;
        public Integer supplierId;
        public LocalDateTime from;
        public LocalDateTime to;
        public GoodsReceiptSort sortBy;
        public boolean isAscending;

        public GoodsReceiptTableModel() {
            supplierId = null;
            from = null;
            to = null;
            sortBy = GoodsReceiptSort.CREATED_AT;
            isAscending = true;
            receipts = grService.getFilteredReceiptsList(supplierId, from, to, sortBy.column(), isAscending);
        }

        @Override
        public int getRowCount() {
            return receipts.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int col) {
            return cols[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            GoodsReceipt r = receipts.get(row);
            return switch (col) {
                case 0 -> row + 1;
                case 1 -> supplierService.getById(r.getSupplierId());
                case 2 -> grService.getUserNameById(r.getCreatedBy());
                case 3 -> DateUtils.formatDateTime(r.getCreatedAt());
                case 4 -> MoneyUtils.format(r.getTotalAmount());
                case 5 -> r.getNote();
                default -> null;
            };
        }

        public void loadData() {
            receipts = grService.getFilteredReceiptsList(supplierId, from, to, sortBy.column(), isAscending);
            fireTableDataChanged();
        }

        public GoodsReceipt getReceiptAt(int row) {
            return receipts.get(row);
        }

        public void setNone() {
            from = null;
            to = null;
        }

        public void setToday() {
            from = LocalDate.now().atStartOfDay();
            to = from.plusDays(1);
        }

        public void setThisWeek() {
            from = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
            to = from.plusWeeks(1);
        }

        public void setThisMonth() {
            from = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            to = from.plusMonths(1);
        }

        public void setThisYear() {
            from = LocalDate.now().withDayOfYear(1).atStartOfDay();
            to = from.plusYears(1);
        }

        public void toggleAscending() {
            isAscending = !isAscending;
        }
    }
    // ================================

    // Use for column sorting
    public enum GoodsReceiptSort {
        SUPPLIER("gr.supplier_id"),
        USER("gr.created_by"),
        CREATED_AT("gr.created_at"),
        TOTAL_AMOUNT("gr.total_amount");

        private final String column;

        GoodsReceiptSort(String column) {
            this.column = column;
        }

        public String column() {
            return column;
        }
    }
    // ======================

    // Date filter helper
    private enum DateFilterItem {
        NONE("Mọi thời gian"),
        TODAY("Hôm nay"),
        THIS_WEEK("Tuần này"),
        THIS_MONTH("Tháng này"),
        THIS_YEAR("Năm nay"),
        CUSTOM("Tùy chọn");

        private final String value;

        DateFilterItem(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private JComboBox<DateFilterItem> getDateFilterCbo() {
        JComboBox<DateFilterItem> cbo;
        DefaultComboBoxModel<DateFilterItem> dateFilterCboModel = new DefaultComboBoxModel<>();
        for (DateFilterItem i : DateFilterItem.values()) {
            dateFilterCboModel.addElement(i);
        }
        cbo = new JComboBox<>(dateFilterCboModel);
        cbo.setSize(50, 10);
        cbo.setPreferredSize(new Dimension(120, 34));
        cbo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cbo.addActionListener(e -> {
            DateFilterItem selectedDateFilter = (DateFilterItem) dateFilterCbo.getSelectedItem();
            dateFilterTxt.setVisible(selectedDateFilter == DateFilterItem.CUSTOM);
            switch (selectedDateFilter) {
                case NONE -> grTableModel.setNone();
                case TODAY -> grTableModel.setToday();
                case THIS_WEEK -> grTableModel.setThisWeek();
                case THIS_MONTH -> grTableModel.setThisMonth();
                case THIS_YEAR -> grTableModel.setThisYear();
                case CUSTOM -> {
                    grTableModel.to = dateFitlerDc.getSelectedDateBetween().getToLocalDateTime();
                    grTableModel.from = dateFitlerDc.getSelectedDateBetween().getFromLocalDateTime();
                }
            }
            grTableModel.loadData();
            revalidate();
        });
        return cbo;
    }
    // =========================

    // Supplier filter helper
    private class SupplierFilterItem {
        public Integer id;
        public String name;

        public SupplierFilterItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id != null ? name + " (" + id + ")" : name;
        }
    }

    private JComboBox<SupplierFilterItem> getSupplierFilterCbo() {
        JComboBox<SupplierFilterItem> cbo;
        List<Supplier> suppliers = supplierService.getAll();
        DefaultComboBoxModel<SupplierFilterItem> cboSupplierModel = new DefaultComboBoxModel<>();
        cboSupplierModel.addElement(new SupplierFilterItem(null, "Tất cả nhà cung cấp"));
        for (Supplier s : suppliers) {
            cboSupplierModel.addElement(new SupplierFilterItem(s.getSupplierId(), s.getSupplierName()));
        }
        cbo = new JComboBox<>(cboSupplierModel);
        cbo.setPreferredSize(new Dimension(240, 34));
        cbo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cbo.addActionListener(e -> {
            SupplierFilterItem selectedSupplierFilter = (SupplierFilterItem) supplierFilterCbo.getSelectedItem();
            grTableModel.supplierId = selectedSupplierFilter.id;
            grTableModel.loadData();
        });
        return cbo;
    }
    // =======================
}