package presentation.dialogs;

import bus.SalesService;
import dto.Customer;
import dto.SalesInvoice;
import dto.SalesInvoiceDetail;
import presentation.dialogs.InvoicePrintPreviewDialog;
import util.MoneyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class SalesInvoiceDetailDialog extends JDialog {
    private int invId;
    private SalesService salesService;
    private SalesInvoice currentInvoice;
    private List<SalesInvoiceDetail> currentDetails;
    
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
        detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailTable.setSelectionBackground(new Color(33, 150, 243));
        detailTable.setSelectionForeground(Color.WHITE);
        detailTable.getTableHeader().setReorderingAllowed(false);
        detailTable.getTableHeader().setResizingAllowed(false);
        detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableCellRenderer selectedRowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (tbl.isRowSelected(row)) {
                    c.setBackground(new Color(33, 150, 243));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        for (int i = 0; i < detailTable.getColumnCount(); i++) {
            detailTable.getColumnModel().getColumn(i).setCellRenderer(selectedRowRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(detailTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Chi tiết mặt hàng"));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrint = new JButton("In hóa đơn");
        JButton btnClose = new JButton("Đóng");

        btnPrint.setBackground(new Color(33, 150, 243));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setOpaque(true);
        btnPrint.setBorderPainted(false);
        btnPrint.setFocusPainted(false);
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnClose.setBackground(new Color(120, 120, 120));
        btnClose.setForeground(Color.WHITE);
        btnClose.setOpaque(true);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnPrint.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnPrint.setBackground(new Color(76, 175, 80));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnPrint.setBackground(new Color(33, 150, 243));
            }
        });

        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setBackground(new Color(244, 67, 54));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setBackground(new Color(120, 120, 120));
            }
        });

        btnPrint.addActionListener(e -> onPrintInvoice());
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnPrint);
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            currentInvoice = salesService.getInvoiceById(invId);
            currentDetails = salesService.getInvoiceDetailsByInvId(invId);
            
            if (currentInvoice != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                lblInvId.setText("Mã HĐ: " + currentInvoice.getInvId());
                lblDate.setText("Ngày tạo: " + sdf.format(currentInvoice.getCreatedAt()));
                lblCustomer.setText("Khách hàng: " + (currentInvoice.getCustomerName() != null ? currentInvoice.getCustomerName() : "Khách lẻ"));
                lblStaff.setText("Thu ngân: " + currentInvoice.getCreatedByName());
                lblPayment.setText("Phương thức: " + currentInvoice.getPaymentMethod());
                
                lblTotal.setText("Tổng tiền: " + MoneyUtils.format(currentInvoice.getGrandTotal()));
                lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
                lblTotal.setForeground(Color.RED);
            }
            
            for (SalesInvoiceDetail d : currentDetails) {
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

    private void onPrintInvoice() {
        if (currentInvoice == null || currentDetails == null || currentDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu hóa đơn để in.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String customerName = (currentInvoice.getCustomerName() == null || currentInvoice.getCustomerName().trim().isEmpty())
                ? "Khách lẻ"
                : currentInvoice.getCustomerName().trim();
        Customer customerForPrint = new Customer(currentInvoice.getCustomerId(), customerName, "");

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        new InvoicePrintPreviewDialog(parentFrame, currentInvoice, currentDetails, customerForPrint,
                currentInvoice.getGrandTotal(), 0).setVisible(true);
    }
}