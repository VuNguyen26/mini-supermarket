package presentation.panels;

import bus.SalesService;
import dto.SalesInvoice;
import presentation.dialogs.SalesInvoiceDetailDialog;
import util.MoneyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class SalesInvoicePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private SalesService salesService;

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
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Quản lý Hóa Đơn Bán Hàng"));
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
        tableModel.setRowCount(0);
        try {
            List<SalesInvoice> invoices = salesService.getAllInvoices();
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu hóa đơn: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openDetailDialog(int invId) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        SalesInvoiceDetailDialog dialog = new SalesInvoiceDetailDialog(parentFrame, invId);
        dialog.setVisible(true);
    }
}