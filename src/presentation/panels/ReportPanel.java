package presentation.panels;

import bus.AuditLogService;
import bus.ReportService;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class ReportPanel extends JPanel {

    public ReportPanel(Set<String> permissions) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setOpaque(true);

        ReportService reportService = new ReportService();
        AuditLogService auditLogService = new AuditLogService();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);
        tabs.setOpaque(true);

        // FlatLaf 3.7: KHÔNG có tabAreaBackground
        tabs.putClientProperty("FlatLaf.style",
                "background:#FFFFFF;" +
                        "contentAreaColor:#FFFFFF;"
        );

        if (permissions.contains("REPORT_VIEW")) {
            tabs.addTab("Doanh thu", new RevenueReportPanel(reportService));
            tabs.addTab("Top sản phẩm", new TopProductReportPanel(reportService));
            tabs.addTab("Tồn kho - HSD", new ExpiryStockReportPanel(reportService));
        }

        if (permissions.contains("AUDIT_LOG_VIEW")) {
            tabs.addTab("Audit log", new AuditLogPanel(auditLogService));
        }

        add(tabs, BorderLayout.CENTER);
    }
}
