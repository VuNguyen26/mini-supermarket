package presentation.dialogs;

import bus.SupplierService;
import dto.Supplier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SupplierDialog extends JDialog {

    private final SupplierService service = new SupplierService();
    private Supplier supplier;
    private boolean isEditMode;
    private boolean saved = false;

    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextArea txtAddress;
    private JButton btnSave;
    private JButton btnCancel;

    public SupplierDialog(Frame parent, Supplier supplier) {
        super(parent, supplier == null ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp", true);
        this.supplier = supplier;
        this.isEditMode = (supplier != null);

        initComponents();
        loadData();
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Supplier Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Tên NCC: *"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtName = new JTextField(20);
        formPanel.add(txtName, gbc);

        row++;

        // Phone
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Số điện thoại: *"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPhone = new JTextField(20);
        formPanel.add(txtPhone, gbc);

        row++;

        // Email
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);

        row++;

        // Address
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Địa chỉ:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane scrollAddr = new JScrollPane(txtAddress);
        formPanel.add(scrollAddr, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        btnSave = new JButton("Lưu");
        btnSave.setPreferredSize(new Dimension(100, 35));
        btnSave.addActionListener(e -> saveSupplier());
        
        btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadData() {
        if (isEditMode && supplier != null) {
            txtName.setText(supplier.getSupplierName());
            txtPhone.setText(supplier.getPhone());
            txtEmail.setText(supplier.getEmail());
            txtAddress.setText(supplier.getAddress());
        }
    }

    private void saveSupplier() {
        try {
            // Collect data
            if (supplier == null) {
                supplier = new Supplier();
            }

            supplier.setSupplierName(txtName.getText().trim());
            supplier.setPhone(txtPhone.getText().trim());
            supplier.setEmail(txtEmail.getText().trim());
            supplier.setAddress(txtAddress.getText().trim());

            // Save
            if (isEditMode) {
                service.update(supplier);
                JOptionPane.showMessageDialog(this,
                        "Cập nhật nhà cung cấp thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                int id = service.create(supplier);
                supplier.setSupplierId(id);
                JOptionPane.showMessageDialog(this,
                        "Thêm nhà cung cấp thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Supplier getSupplier() {
        return supplier;
    }
}
