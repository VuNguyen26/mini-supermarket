package presentation.dialogs;

import dto.*;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InvoicePrintPreviewDialog extends JDialog {
    public InvoicePrintPreviewDialog(JFrame parent, SalesInvoice inv, List<SalesInvoiceDetail> details, Customer cus,
            double given, double change) {
        super(parent, "Xem trước hóa đơn", true);
        setSize(400, 600);
        setLocationRelativeTo(parent);

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
        add(new JScrollPane(txt));
    }
}