package presentation.dialogs;

import bus.StockAdjustmentService;
import dto.StockAdjustment;
import dto.StockAdjustmentStatus;

import javax.swing.*;
import java.awt.*;

public class StockAdjustmentDetailDialog extends JDialog {

    private final StockAdjustmentService service = new StockAdjustmentService();
    private final StockAdjustment sa;
    private final boolean editable;

    private boolean saved = false;

    // UI
    private JTextField txtCode;
    private JTextField txtReason;
    private JComboBox<StockAdjustmentStatus> cbStatus;
    private JTextArea txtNote;

    public StockAdjustmentDetailDialog(
            Window owner,
            StockAdjustment sa,
            boolean editable
    ) {
        super(owner, "Chi tiết phiếu kiểm kho", ModalityType.APPLICATION_MODAL);
        this.sa = sa;
        this.editable = editable;

        initUI();
        fillData();
        applyMode();

        setSize(480, 380);
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    // ================= UI =================
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;

        // Mã phiếu
        g.gridx = 0;
        form.add(new JLabel("Mã phiếu"), g);
        g.gridx = 1;
        txtCode = new JTextField();
        form.add(txtCode, g);

        // Lý do
        g.gridy++;
        g.gridx = 0;
        form.add(new JLabel("Lý do"), g);
        g.gridx = 1;
        txtReason = new JTextField();
        form.add(txtReason, g);

        // Trạng thái
        g.gridy++;
        g.gridx = 0;
        form.add(new JLabel("Trạng thái"), g);
        g.gridx = 1;
        cbStatus = new JComboBox<>(StockAdjustmentStatus.values());
        form.add(cbStatus, g);

        // Ghi chú
        g.gridy++;
        g.gridx = 0;
        g.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel("Ghi chú"), g);

        g.gridx = 1;
        txtNote = new JTextArea(4, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        form.add(new JScrollPane(txtNote), g);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if (editable) {
            JButton btnSave = new JButton("Lưu");
            JButton btnCancel = new JButton("Hủy");

            btnSave.addActionListener(e -> onSave());
            btnCancel.addActionListener(e -> dispose());

            buttons.add(btnCancel);
            buttons.add(btnSave);
        } else {
            JButton btnClose = new JButton("Đóng");
            btnClose.addActionListener(e -> dispose());
            buttons.add(btnClose);
        }

        add(buttons, BorderLayout.SOUTH);
    }

    // ================= DATA =================
    private void fillData() {
        txtCode.setText(sa.getSaCode());
        txtReason.setText(sa.getReason().name());
        cbStatus.setSelectedItem(sa.getStatus());
        txtNote.setText(sa.getNote());
    }

    private void applyMode() {
        // Luôn readonly
        txtCode.setEnabled(false);
        txtReason.setEnabled(false);

        if (!editable) {
            cbStatus.setEnabled(false);
            txtNote.setEditable(false);
        } else {
            // Editable chỉ cho đổi status nếu đang DRAFT
            if (sa.getStatus() != StockAdjustmentStatus.DRAFT) {
                cbStatus.setEnabled(false);
            }
        }
    }

    // ================= ACTION =================
    private void onSave() {
        try {
            // Update note
            sa.setNote(txtNote.getText());

            // Update status nếu có đổi
            StockAdjustmentStatus newStatus =
                    (StockAdjustmentStatus) cbStatus.getSelectedItem();

            if (newStatus != sa.getStatus()) {
                service.updateStatus(sa.getSaId(), newStatus);
                sa.setStatus(newStatus);
            }

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
