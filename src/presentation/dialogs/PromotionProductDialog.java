package presentation.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import bus.PromotionService;
import bus.StockAdjustmentService;
import dto.ProductOption;
import dto.PromotionProduct;


import dto.Promotion;

import java.awt.*;
import java.util.List;

public class PromotionProductDialog extends JDialog {
    private final PromotionService service = new PromotionService();
    private final StockAdjustmentService saService = new StockAdjustmentService();
    private final boolean isEdit;
    private boolean saved = false;

    private JTextField txtPromotionName;
    private JComboBox<ProductOption> cbProduct;

    private PromotionProduct pp;
    private Promotion promotion;


    // Thêm
    public PromotionProductDialog(Window owner, Promotion promotion){
        super(owner, "Thêm sản phẩm cho chương trình", ModalityType.APPLICATION_MODAL);
        this.isEdit = false;
        this.promotion = promotion;
        initUI();
        txtPromotionName.setText(promotion.getPromoName());
        txtPromotionName.setEnabled(false);
        loadProducts();
    }

    // Sửa
    public PromotionProductDialog(Window owner, Promotion promotion, PromotionProduct pp){
        super(owner, "Thêm sản phẩm cho chương trình", ModalityType.APPLICATION_MODAL);
        this.isEdit = true;
        this.promotion = promotion;
        this.pp = pp;
        initUI();
        loadProducts();
        fillData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(15, 20, 15, 20));
        add(main, BorderLayout.CENTER);

        main.add(buildForm(), BorderLayout.CENTER);
        main.add(buildButtons(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(520, 420));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPromotionName = new JTextField();
        cbProduct = new JComboBox<ProductOption>();

        int row = 0;
        addRow(panel, gbc, row++, "Tên chương trình KM", txtPromotionName);
        addRow(panel, gbc, row++, "Sản phẩm", cbProduct);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String label, JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
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

    private void loadProducts() {
        List<ProductOption> list = saService.getProductsForCombobox();
        for (ProductOption p : list) {
            cbProduct.addItem(p);
        }
    }

    private void fillData(){
        txtPromotionName.setText(promotion.getPromoName());
        txtPromotionName.setEnabled(false);

        for (int i = 0; i < cbProduct.getItemCount(); i++) {
            if (cbProduct.getItemAt(i).getProductId() == pp.getProductId()) {
                cbProduct.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onSave() {
        try {
            ProductOption p = (ProductOption) cbProduct.getSelectedItem();

            if (p == null) {
                throw new IllegalArgumentException("Chưa chọn sản phẩm");
            }

            if (isEdit) {

                pp.setProductId(p.getProductId());
                pp.setProductName(p.toString());

                service.updateProduct(pp);
                
            } else {
                PromotionProduct tmpPP = new PromotionProduct();

                tmpPP.setProductId(p.getProductId());
                tmpPP.setProductName(p.toString());
                tmpPP.setPromoId(promotion.getPromoId());
                service.addProduct(tmpPP);
            }

            saved = true;
            dispose();

        }catch (Exception ex) {
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
