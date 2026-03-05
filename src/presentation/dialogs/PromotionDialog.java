package presentation.dialogs;

import bus.PromotionService;
import bus.AuthService.AuthUser;
import dto.Promotion;
import dto.PromotionType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import com.toedter.calendar.JDateChooser;
public class PromotionDialog extends JDialog {

    private final PromotionService service = new PromotionService();
    private final AuthUser currentUser;
    private final String Mode;
    private boolean saved = false;

    private JTextField txtCode;
    private JTextField txtName;
    private JTextField txtValue;
    private JTextField txtMinOrder;
    private JComboBox<PromotionType> cbType;
    private JComboBox<String> cbStatus;
    private JDateChooser dcStartAt;
    private JDateChooser dcEndAt;

    private Promotion promo;

    /* ================= CONSTRUCTORS ================= */

    // VIEW
    public PromotionDialog(Window owner, Promotion promo) {
        super(owner, "Chi tiết khuyến mãi", ModalityType.APPLICATION_MODAL);
        this.Mode = "VIEW";
        this.promo = promo;
        this.currentUser = null;
        initUI();
        fillData();
        lockViewMode();
    }

    // ADD
    public PromotionDialog(Window owner, AuthUser currentUser) {
        super(owner, "Thêm khuyến mãi", ModalityType.APPLICATION_MODAL);
        this.Mode = "ADD";
        this.currentUser = currentUser;
        initUI();
    }

    // EDIT
    public PromotionDialog(Window owner, AuthUser currentUser, Promotion promo) {
        super(owner, "Sửa khuyến mãi", ModalityType.APPLICATION_MODAL);
        this.Mode = "EDIT";
        this.currentUser = currentUser;
        this.promo = promo;
        initUI();
        fillData();
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

        txtCode = new JTextField();
        txtName = new JTextField();
        txtValue = new JTextField();
        txtMinOrder = new JTextField();
        dcStartAt = new JDateChooser();
        dcEndAt = new JDateChooser();

        dcStartAt.setDateFormatString("yyyy-MM-dd");
        dcEndAt.setDateFormatString("yyyy-MM-dd");

        JTextField startField = (JTextField) dcStartAt.getDateEditor().getUiComponent();
        JTextField endField = (JTextField) dcEndAt.getDateEditor().getUiComponent();

        startField.setEditable(false);
        endField.setEditable(false);

        cbType = new JComboBox<>(PromotionType.values());
        cbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        int row = 0;
        addRow(panel, gbc, row++, "Mã KM", txtCode);
        addRow(panel, gbc, row++, "Tên KM", txtName);
        addRow(panel, gbc, row++, "Loại", cbType);
        addRow(panel, gbc, row++, "Giá trị", txtValue);
        addRow(panel, gbc, row++, "Đơn tối thiểu", txtMinOrder);
        addRow(panel, gbc, row++, "Bắt đầu", wrap(dcStartAt));
        addRow(panel, gbc, row++, "Kết thúc", wrap(dcEndAt));
        addRow(panel, gbc, row++, "Trạng thái", cbStatus);

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
        JButton btnOK = new JButton("Đóng");

        btnSave.setPreferredSize(new Dimension(90, 32));
        btnCancel.setPreferredSize(new Dimension(90, 32));
        btnOK.setPreferredSize(new Dimension(90, 32));

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        btnOK.addActionListener(e -> dispose());

        if (this.Mode.compareTo("VIEW") == 0){
            panel.add(btnOK);
        } else {
            panel.add(btnCancel);
            panel.add(btnSave);
        }
        return panel;
    }

    /* ================= DATA ================= */

    private void fillData() {
        txtCode.setText(promo.getPromoCode());
        txtName.setText(promo.getPromoName());
        txtValue.setText(promo.getValue().toString());
        txtMinOrder.setText(promo.getMinOrderAmount().toString());
        cbType.setSelectedItem(promo.getType());
        cbStatus.setSelectedItem(promo.getStatus());

        if (promo.getStartAt() != null){
            dcStartAt.setDate(
                java.sql.Timestamp.valueOf(promo.getStartAt())
            );
        }
        if (promo.getEndAt() != null){
            dcEndAt.setDate(
                java.sql.Timestamp.valueOf(promo.getEndAt())
            );
        }
    }

    private void lockViewMode() {

        // ===== TextField =====
        JTextField[] textFields = {
            txtCode,
            txtName,
            txtValue,
            txtMinOrder
        };

        for (JTextField tf : textFields) {
            tf.setEditable(false);
            tf.setFocusable(false);
            tf.setBackground(new Color(245, 245, 245)); // xám nhẹ
        }

        // ===== ComboBox =====
        JComboBox<?>[] comboBoxes = {
            cbType,
            cbStatus
        };

        for (JComboBox<?> cb : comboBoxes) {
            cb.setEnabled(false);
            cb.setBackground(new Color(245, 245, 245));
        }

        dcStartAt.setEnabled(false);
        dcEndAt.setEnabled(false);

    }

    /* ================= SAVE ================= */

    private void onSave() {
        try {
            Promotion p = (this.Mode.compareTo("EDIT") == 0) ? promo : new Promotion();

            p.setPromoCode(txtCode.getText().trim());
            p.setPromoName(txtName.getText().trim());
            p.setType((PromotionType) cbType.getSelectedItem());
            p.setValue(new BigDecimal(txtValue.getText()));
            p.setMinOrderAmount(new BigDecimal(txtMinOrder.getText()));
            p.setStatus((String) cbStatus.getSelectedItem());
            p.setCreatedBy(currentUser.userId);

            if (dcStartAt.getDate() != null) {
                p.setStartAt(
                    dcStartAt.getDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
                );
            }

            if (dcEndAt.getDate() != null) {
                p.setEndAt(
                    dcEndAt.getDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
                );
            }

            if (this.Mode.compareTo("EDIT") == 0) {
                service.update(p);
            } else {
                service.add(p);
            }

            saved = true;
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Giá trị và đơn tối thiểu phải là số",
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

    private JPanel wrap(JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.add(comp);
        return p;
    }

}
