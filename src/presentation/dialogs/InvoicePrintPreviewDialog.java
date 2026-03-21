package presentation.dialogs;

import dto.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InvoicePrintPreviewDialog extends JDialog {
    public InvoicePrintPreviewDialog(JFrame parent, SalesInvoice inv, List<SalesInvoiceDetail> details, Customer cus,
            double given, double change) {
        super(parent, "Xem trước hóa đơn", true);
        setSize(400, 600);
        setLocationRelativeTo(parent);
    setLayout(new BorderLayout());

        JTextArea txt = new JTextArea();
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txt.setEditable(false);

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        String customerName = "Khách lẻ";
        if (cus != null) {
            String name = cus.getCustomerName();
            if (name != null && !name.trim().isEmpty()) {
                customerName = name.trim();
            }
        }

        sb.append("      SIÊU THỊ ĐỒ MINI      \n");
        sb.append("       Chi nhánh: HCM      \n");
        sb.append("       Tel: 0967-360-063       \n");
        sb.append("----------------------------\n");
        sb.append("Số HĐ: ").append(inv.getInvId()).append("\n");
        sb.append("Ngày:  ").append(sdf.format(new Date())).append("\n");
        sb.append("Khách: ").append(customerName).append("\n");
        sb.append("----------------------------\n");
        sb.append(String.format("%-15s %3s %9s\n", "Tên SP", "SL", "T.Tiền"));

        for (SalesInvoiceDetail d : details) {
            String nameToShow = d.getProductName() != null ? d.getProductName() : "SP #" + d.getProductId();
            if (nameToShow.length() > 20) {
                nameToShow = nameToShow.substring(0, 17) + "...";
            }
            sb.append(String.format("%-15s %3d %9.0f\n", nameToShow, d.getQty(), d.getLineTotal()));
        }

        sb.append("----------------------------\n");
        sb.append(String.format("TỔNG CỘNG:    %10.0f\n", inv.getGrandTotal()));
        sb.append(String.format("KHÁCH ĐƯA:    %10.0f\n", given));
        sb.append(String.format("TIỀN THỪA:    %10.0f\n", change));
        sb.append("----------------------------\n");
        sb.append("   Cảm ơn quý khách!   ");

        txt.setText(sb.toString());
        add(new JScrollPane(txt), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
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

        btnPrint.addActionListener(e -> {
            try {
                boolean done = txt.print();
                if (done) {
                    JOptionPane.showMessageDialog(this, "Đã gửi lệnh in.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "In thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnClose.addActionListener(e -> dispose());

        actions.add(btnPrint);
        actions.add(btnClose);
        add(actions, BorderLayout.SOUTH);
    }
}