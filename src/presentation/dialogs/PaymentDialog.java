package presentation.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

public class PaymentDialog extends JDialog {

    private final DecimalFormat moneyFmt = new DecimalFormat("#,###");

    public PaymentDialog(JFrame parent, double total, double change, int invoiceId) {
        super(parent, "Thanh toán thành công", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(content);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Giao dịch hoàn tất");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));

        JLabel lblSub = new JLabel("Mã hóa đơn: #" + invoiceId);
        lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 13f));
        lblSub.setForeground(new Color(90, 90, 90));

        headerText.add(lblTitle);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(lblSub);

        header.add(headerText, BorderLayout.CENTER);
        content.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 40), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.anchor = GridBagConstraints.WEST;

        JLabel lblTotalTitle = new JLabel("Tổng thanh toán:");
        JLabel lblTotalValue = new JLabel(formatMoney(total));
        lblTotalValue.setFont(lblTotalValue.getFont().deriveFont(Font.BOLD, 16f));

        addRow(card, gc, lblTotalTitle, lblTotalValue);

        JLabel lblChangeTitle;
        JLabel lblChangeValue;
        if (change >= 0) {
            lblChangeTitle = new JLabel("Tiền thừa trả khách:");
            lblChangeValue = new JLabel(formatMoney(change));
            lblChangeValue.setForeground(new Color(0, 102, 0));
        } else {
            lblChangeTitle = new JLabel("Khách còn thiếu:");
            lblChangeValue = new JLabel(formatMoney(Math.abs(change)));
            lblChangeValue.setForeground(new Color(176, 0, 32));
        }
        lblChangeValue.setFont(lblChangeValue.getFont().deriveFont(Font.BOLD, 15f));

        addRow(card, gc, lblChangeTitle, lblChangeValue);

        JLabel lblHint = new JLabel("Nhấn Enter để tiếp tục xem hóa đơn.");
        lblHint.setFont(lblHint.getFont().deriveFont(Font.PLAIN, 12f));
        lblHint.setForeground(new Color(110, 110, 110));

        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.WEST;
        card.add(lblHint, gc);

        content.add(card, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);

        JButton btnContinue = new JButton("Tiếp tục (In hóa đơn)");
        btnContinue.setPreferredSize(new Dimension(170, 34));

        btnContinue.addActionListener(e -> dispose());

        footer.add(btnContinue);
        content.add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnContinue);
        installKeyBindings(btnContinue);

        pack();
        setMinimumSize(new Dimension(420, getPreferredSize().height));
        setLocationRelativeTo(parent);
    }

    private void addRow(JPanel panel, GridBagConstraints gc, JComponent left, JComponent right) {
        GridBagConstraints c1 = (GridBagConstraints) gc.clone();
        c1.gridx = 0;
        c1.gridwidth = 1;
        c1.weightx = 0;
        c1.fill = GridBagConstraints.NONE;
        panel.add(left, c1);

        GridBagConstraints c2 = (GridBagConstraints) gc.clone();
        c2.gridx = 1;
        c2.weightx = 1;
        c2.anchor = GridBagConstraints.EAST;
        panel.add(right, c2);

        gc.gridy++;
    }

    private String formatMoney(double v) {
        return moneyFmt.format(Math.round(v)) + " đ";
    }

    private void installKeyBindings(JButton defaultButton) {
        Action close = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };

        JRootPane rp = getRootPane();
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        rp.getActionMap().put("close", close);

        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ENTER"), "enter");
        rp.getActionMap().put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultButton.doClick();
            }
        });
    }
}
