package presentation.dialogs;

import bus.SalesService;
import dto.SalesInvoice;
import dto.SalesInvoiceDetail;
import util.MoneyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class SalesInvoiceDetailDialog extends JDialog {
    private int invId;
    private SalesService salesService;
    
    private JLabel lblInvId, lblDate, lblCustomer, lblStaff, lblTotal, lblPayment;
    private JTable detailTable;
    private DefaultTableModel tableModel;

    public SalesInvoiceDetailDialog(JFrame parent, int invId) {
        super(parent, "Chi tiết Hóa Đơn #" + invId, true);
        this.invId = invId;
        this.salesService = new SalesService();
        
        initComponents();
        loadData();
        
        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chung"));
        
        lblInvId = new JLabel("Mã HĐ: ");
        lblDate = new JLabel("Ngày tạo: ");
        lblCustomer = new JLabel("Khách hàng: ");
        lblStaff = new JLabel("Thu ngân: ");
        lblTotal = new JLabel("Tổng tiền: ");
        lblPayment = new JLabel("Phương thức: ");
        
        infoPanel.add(lblInvId);
        infoPanel.add(lblDate);
        infoPanel.add(lblCustomer);
        infoPanel.add(lblStaff);
        infoPanel.add(lblPayment);
        infoPanel.add(lblTotal);
        
        add(infoPanel, BorderLayout.NORTH);

        String[] cols = {"Mã SP", "Tên Sản Phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        detailTable = new JTable(tableModel);
        detailTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(detailTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Chi tiết mặt hàng"));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            SalesInvoice invoice = salesService.getInvoiceById(invId);
            List<SalesInvoiceDetail> details = salesService.getInvoiceDetailsByInvId(invId);
            
            if (invoice != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                lblInvId.setText("Mã HĐ: " + invoice.getInvId());
                lblDate.setText("Ngày tạo: " + sdf.format(invoice.getCreatedAt()));
                lblCustomer.setText("Khách hàng: " + (invoice.getCustomerName() != null ? invoice.getCustomerName() : "Khách lẻ"));
                lblStaff.setText("Thu ngân: " + invoice.getCreatedByName());
                lblPayment.setText("Phương thức: " + invoice.getPaymentMethod());
                
                lblTotal.setText("Tổng tiền: " + MoneyUtils.format(invoice.getGrandTotal()));
                lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
                lblTotal.setForeground(Color.RED);
            }
            
            for (SalesInvoiceDetail d : details) {
                Object[] row = {
                    d.getProductId(),
                    d.getProductName(),
                    d.getQty(),
                    MoneyUtils.format(d.getUnitPrice()),
                    MoneyUtils.format(d.getLineTotal())
                };
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết hóa đơn: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}