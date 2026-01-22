package presentation.dialogs;

import bus.StockAdjustmentService;
import dto.ProductOption;
import dto.LotOption;
import dto.StockAdjustmentDetail;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StockAdjustmentDetailDialog extends JDialog {

    private final StockAdjustmentService service = new StockAdjustmentService();
    private final boolean isEdit;
    private boolean saved = false;

    private JComboBox<ProductOption> cbProduct;
    private JComboBox<LotOption> cbLot;
    private JTextField txtSystemQty;
    private JTextField txtCountedQty;
    private JTextArea txtNote;

    private StockAdjustmentDetail detail;
    private int saId;

    /* ================= CONSTRUCTORS ================= */

    // THÊM
    public StockAdjustmentDetailDialog(Window owner, int saId) {
        super(owner, "Thêm chi tiết kiểm kho", ModalityType.APPLICATION_MODAL);
        this.saId = saId;
        this.isEdit = false;
        initUI();
        loadProducts();
    }

    // SỬA
    public StockAdjustmentDetailDialog(Window owner, StockAdjustmentDetail detail) {
        super(owner, "Sửa chi tiết kiểm kho", ModalityType.APPLICATION_MODAL);
        this.detail = detail;
        this.saId = detail.getSaId();
        this.isEdit = true;
        initUI();
        loadProducts();
        fillData();
        lockFieldsWhenEdit();
    }

    /* ================= UI ================= */

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(15, 20, 15, 20));
        add(main, BorderLayout.CENTER);

        main.add(buildForm(), BorderLayout.CENTER);
        main.add(buildButtons(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(480, 380));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        cbProduct = new JComboBox<>();
        cbLot = new JComboBox<>();
        txtSystemQty = new JTextField();
        txtSystemQty.setEditable(false);

        txtCountedQty = new JTextField();
        txtNote = new JTextArea(4, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        int row = 0;

        // Sản phẩm
        addRow(panel, gbc, row++, "Sản phẩm", cbProduct);

        // Lô
        addRow(panel, gbc, row++, "Lô", cbLot);

        // SL hệ thống
        addRow(panel, gbc, row++, "SL hệ thống", txtSystemQty);

        // SL kiểm
        addRow(panel, gbc, row++, "SL kiểm", txtCountedQty);

        // Ghi chú
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Ghi chú"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(txtNote), gbc);

        // events
        cbProduct.addActionListener(e -> loadLots());
        cbLot.addActionListener(e -> fillSystemQty());

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String label, JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.setPreferredSize(new Dimension(90, 32));
        btnCancel.setPreferredSize(new Dimension(90, 32));

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        panel.add(btnCancel);
        panel.add(btnSave);

        return panel;
    }

    /* ================= DATA ================= */

    private void loadProducts() {
        List<ProductOption> list = service.getProductsForCombobox();
        for (ProductOption p : list) {
            cbProduct.addItem(p);
        }
    }

    private void loadLots() {
        cbLot.removeAllItems();
        ProductOption p = (ProductOption) cbProduct.getSelectedItem();
        if (p == null) return;

        List<LotOption> lots = service.getLotsByProduct(p.getProductId());
        for (LotOption l : lots) {
            cbLot.addItem(l);
        }
    }

    private void fillSystemQty() {
        LotOption lot = (LotOption) cbLot.getSelectedItem();
        if (lot == null) {
            txtSystemQty.setText("");
            return;
        }
        txtSystemQty.setText(String.valueOf(lot.getStockQty()));
    }

    private void fillData() {
        txtCountedQty.setText(String.valueOf(detail.getCountedQty()));
        txtNote.setText(detail.getNote());

        // product
        for (int i = 0; i < cbProduct.getItemCount(); i++) {
            if (cbProduct.getItemAt(i).getProductId() == detail.getProductId()) {
                cbProduct.setSelectedIndex(i);
                break;
            }
        }

        loadLots();

        // lot
        for (int i = 0; i < cbLot.getItemCount(); i++) {
            if (cbLot.getItemAt(i).getLotId() == detail.getLotId()) {
                cbLot.setSelectedIndex(i);
                break;
            }
        }

        fillSystemQty();
    }

    private void lockFieldsWhenEdit() {
        cbProduct.setEnabled(false);
        cbLot.setEnabled(false);
    }

    /* ================= SAVE ================= */

    private void onSave() {
        try {
            ProductOption p = (ProductOption) cbProduct.getSelectedItem();
            LotOption l = (LotOption) cbLot.getSelectedItem();
            int counted = Integer.parseInt(txtCountedQty.getText());

            if (p == null || l == null) {
                throw new IllegalArgumentException("Chưa chọn sản phẩm hoặc lô");
            }

            if (isEdit) {
                detail.setProductId(p.getProductId());
                detail.setLotId((long) l.getLotId());
                detail.setSystemQty(l.getStockQty());
                detail.setCountedQty(counted);
                detail.setNote(txtNote.getText());

                service.updateDetail(detail);
            } else {
                StockAdjustmentDetail d = new StockAdjustmentDetail();
                d.setSaId(saId);
                d.setProductId(p.getProductId());
                d.setLotId((long) l.getLotId());
                d.setSystemQty(l.getStockQty());
                d.setCountedQty(counted);
                d.setNote(txtNote.getText());

                service.addDetail(d);
            }

            saved = true;
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Số lượng kiểm phải là số",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
