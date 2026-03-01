package presentation.panels;

import bus.SalesService;
import dto.SalesInvoice;
import presentation.dialogs.SalesInvoiceDetailDialog;
import util.MoneyUtils;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SalesInvoicePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private SalesService salesService;

    // search controls
    private JTextField txtInvId;
    private JTextField txtCustomerName;
    private JDateChooser dcFromDate;
    private JDateChooser dcToDate;

    public SalesInvoicePanel() {
        salesService = new SalesService();
        initComponents();
        loadDataToTable();
    }

    public void refreshData() {
        loadDataToTable();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // find panel
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        JLabel title = new JLabel("Quản lý Hóa Đơn Bán Hàng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        topPanel.add(title, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        txtInvId = new JTextField(6);
        txtCustomerName = new JTextField(12);
        dcFromDate = new JDateChooser();
        dcFromDate.setDateFormatString("dd/MM/yyyy");
        dcFromDate.setPreferredSize(new Dimension(120, 28));

        dcToDate = new JDateChooser();
        dcToDate.setDateFormatString("dd/MM/yyyy");
        dcToDate.setPreferredSize(new Dimension(120, 28));

        JButton btnSearch = new JButton("Tìm");
        JButton btnReset = new JButton("Reset");

        filterPanel.add(new JLabel("Mã HĐ:"));
        filterPanel.add(txtInvId);
        filterPanel.add(new JLabel("Khách hàng:"));
        filterPanel.add(txtCustomerName);
        filterPanel.add(new JLabel("Từ ngày:"));
        filterPanel.add(dcFromDate);
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(dcToDate);
        filterPanel.add(btnSearch);
        filterPanel.add(btnReset);

        // actions
        btnSearch.addActionListener(e -> doSearch());
        btnReset.addActionListener(e -> {
            clearSearch();
            loadDataToTable();
        });
        txtInvId.addActionListener(e -> doSearch());
        txtCustomerName.addActionListener(e -> doSearch());
        // auto search when date changes
        dcFromDate.getDateEditor().addPropertyChangeListener("date", evt -> doSearch());
        dcToDate.getDateEditor().addPropertyChangeListener("date", evt -> doSearch());

        topPanel.add(filterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // data table
        String[] columns = { "Mã HĐ", "Ngày Tạo", "Khách Hàng", "Nhân Viên", "Tổng Tiền", "Phương Thức" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);

        // click row for details
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int selectedRow = table.getSelectedRow();
                    int invId = (int) table.getValueAt(selectedRow, 0);
                    openDetailDialog(invId);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadDataToTable() {
        try {
            List<SalesInvoice> invoices = salesService.getAllInvoices();
            fillTable(invoices);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu hóa đơn: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void fillTable(List<SalesInvoice> invoices) {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (SalesInvoice inv : invoices) {
            Object[] row = {
                    inv.getInvId(),
                    sdf.format(inv.getCreatedAt()),
                    inv.getCustomerName(),
                    inv.getCreatedByName(),
                    MoneyUtils.format(inv.getGrandTotal()),
                    inv.getPaymentMethod()
            };
            tableModel.addRow(row);
        }
    }

    private void doSearch() {
        try {
            Integer invId = null;
            String invIdText = txtInvId.getText().trim();
            if (!invIdText.isEmpty()) {
                invId = Integer.parseInt(invIdText);
            }

            String customerName = txtCustomerName.getText().trim();
            if (customerName.isEmpty()) {
                customerName = null;
            }

            Timestamp from = toStartOfDay(dcFromDate.getDate());
            Timestamp to = toEndOfDay(dcToDate.getDate());

            // If user typed something but parsing failed, JDateChooser may return null.
            String fromText = ((JTextField) dcFromDate.getDateEditor().getUiComponent()).getText().trim();
            String toText = ((JTextField) dcToDate.getDateEditor().getUiComponent()).getText().trim();
            if (!fromText.isEmpty() && from == null) {
                JOptionPane.showMessageDialog(this,
                        "Ngày 'Từ ngày' không hợp lệ. Vui lòng chọn lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!toText.isEmpty() && to == null) {
                JOptionPane.showMessageDialog(this,
                        "Ngày 'Đến ngày' không hợp lệ. Vui lòng chọn lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (from != null && to != null && from.after(to)) {
                JOptionPane.showMessageDialog(this,
                        "Khoảng ngày không hợp lệ: 'Từ ngày' phải <= 'Đến ngày'",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<SalesInvoice> result = salesService.searchInvoices(invId, customerName, from, to);
            fillTable(result);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Mã HĐ phải là số nguyên.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tìm kiếm hóa đơn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearSearch() {
        txtInvId.setText("");
        txtCustomerName.setText("");
        dcFromDate.setDate(null);
        dcToDate.setDate(null);
    }

    private Timestamp toStartOfDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
    }

    private Timestamp toEndOfDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return new Timestamp(cal.getTimeInMillis());
    }


    private void openDetailDialog(int invId) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        SalesInvoiceDetailDialog dialog = new SalesInvoiceDetailDialog(parentFrame, invId);
        dialog.setVisible(true);
    }
}