package presentation.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;

public class PaymentDialog extends JDialog {

    private static final Color COLOR_PRIMARY = new Color(39, 174, 96); // Green
    private static final Color COLOR_BG = new Color(255, 255, 255); // White    
    private static final Color COLOR_TEXT_MAIN = new Color(44, 62, 80); // Dark Blue
    private static final Color COLOR_TEXT_SUB = new Color(127, 140, 141); // Gray
    private static final Color COLOR_ERROR = new Color(192, 57, 43); // Red

    private final DecimalFormat moneyFmt = new DecimalFormat("#,###");

    public PaymentDialog(JFrame parent, double total, double change, int invoiceId) {
        super(parent, "Giao dịch thành công", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true); 
        JPanel contentPane = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(new Color(230, 230, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        contentPane.setOpaque(false);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5)); 

        setContentPane(contentPane);
        JPanel pnlHeader = new JPanel();
        pnlHeader.setOpaque(false);
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBorder(new EmptyBorder(30, 0, 20, 0));

        JLabel lblIcon = new JLabel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(70, 70);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(COLOR_PRIMARY);
                g2.fill(new Ellipse2D.Double(0, 0, 70, 70));
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawLine(20, 36, 32, 48);
                g2.drawLine(32, 48, 50, 24);
            }
        };
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Thanh toán thành công!");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        pnlHeader.add(lblIcon);
        pnlHeader.add(Box.createVerticalStrut(15));
        pnlHeader.add(lblTitle);

        JPanel pnlBody = new JPanel(new GridLayout(3, 1, 0, 5));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(10, 40, 20, 40));

        pnlBody.add(createDetailRow("Tổng thanh toán", formatMoney(total), true));
        
        String changeLabel = change >= 0 ? "Tiền thừa trả khách" : "Khách còn thiếu";
        Color changeColor = change >= 0 ? COLOR_TEXT_MAIN : COLOR_ERROR;
        pnlBody.add(createDetailRow(changeLabel, formatMoney(Math.abs(change)), false, changeColor));

        pnlBody.add(createDetailRow("Mã hóa đơn", "#" + invoiceId, false, COLOR_TEXT_SUB));

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(new EmptyBorder(0, 0, 25, 0));

        JButton btnConfirm = new JButton("HOÀN TẤT & IN (Enter)");
        styleButton(btnConfirm);
        btnConfirm.addActionListener(e -> dispose());
        
        pnlFooter.add(btnConfirm);

        add(pnlHeader, BorderLayout.NORTH);
        add(pnlBody, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);

        installKeyBindings(btnConfirm);

        pack();
        setSize(400, 450); 
        setLocationRelativeTo(parent);
    }

    private JPanel createDetailRow(String title, String value, boolean isBig) {
        return createDetailRow(title, value, isBig, COLOR_TEXT_MAIN);
    }

    private JPanel createDetailRow(String title, String value, boolean isBig, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(COLOR_TEXT_SUB);

        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(valueColor);
        
        if (isBig) {
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        } else {
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        }

        p.add(lblTitle, BorderLayout.WEST);
        p.add(lblValue, BorderLayout.EAST);
        
        if (isBig) {
            p.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0,0,10,0),
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240,240,240))
            ));
        }

        return p;
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(250, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_PRIMARY.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_PRIMARY);
            }
        });
    }

    private String formatMoney(double v) {
        return moneyFmt.format(Math.round(v)) + " đ";
    }

    private void installKeyBindings(JButton defaultButton) {
        JRootPane rp = getRootPane();
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        rp.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "enter");
        rp.getActionMap().put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultButton.doClick();
            }
        });
    }
}