package presentation.dialogs;

import bus.StockAdjustmentService;
import bus.AuthService.AuthUser;
import dto.StockAdjustmentReason;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StockAdjustmentDialog extends JDialog {

    private final StockAdjustmentService service = new StockAdjustmentService();
    private final AuthUser currentUser;

    private boolean saved = false;

    // UI components
    private JTextField txtCode;
    private JComboBox<StockAdjustmentReason> cboReason;
    private JTextArea txtNote;

    public StockAdjustmentDialog(Window owner, AuthUser currentUser) {
        super(owner, "Thêm phiếu kiểm kho", ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;

        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    // ================= UI =================
    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // ===== Mã phiếu =====
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Mã phiếu"), gbc);

        gbc.gridx = 1;
        txtCode = new JTextField(20);
        form.add(txtCode, gbc);

        // ===== Lý do =====
        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Lý do kiểm kho"), gbc);

        gbc.gridx = 1;
        cboReason = new JComboBox<>(StockAdjustmentReason.values());
        form.add(cboReason, gbc);

        // ===== Ghi chú =====
        gbc.gridx = 0; gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Ghi chú"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        txtNote = new JTextArea(4, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(txtNote);
        form.add(sp, gbc);

        add(form, BorderLayout.CENTER);

        // ===== Buttons =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        buttons.add(btnCancel);
        buttons.add(btnSave);

        add(buttons, BorderLayout.SOUTH);
    }

    // ================= ACTION =================
    private void onSave() {
        try {
            String saCode = txtCode.getText().trim();
            StockAdjustmentReason reason =
                    (StockAdjustmentReason) cboReason.getSelectedItem();
            String note = txtNote.getText().trim();

            service.createDraft(
                    currentUser.userId,
                    reason,
                    saCode,
                    note
            );

            saved = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
