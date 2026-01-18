package presentation.dialogs;

import bus.CustomerService;
import dto.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerDialog extends JDialog {

    private final CustomerService service = new CustomerService();
    private Customer customer;
    private boolean isEditMode;
    private boolean saved = false;

    private JTextField txtName;
    private JTextField txtPhone;
    private JTextArea txtAddress;
    private JTextField txtPoints;
    private JButton btnSave;
    private JButton btnCancel;

    public CustomerDialog(Frame parent, Customer customer) {
        super(parent, customer == null ? "Thêm khách hàng" : "Sửa khách hàng", true);
        this.customer = customer;
        this.isEditMode = (customer != null);

        initComponents();
        loadData();
        
        setSize(550, 400);
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

        // Customer Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Tên khách hàng: *"), gbc);
        
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

        // Points
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Điểm tích lũy:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPoints = new JTextField(20);
        txtPoints.setText("0");
        if (isEditMode) {
            txtPoints.setEnabled(false); // Không cho sửa điểm trực tiếp
        }
        formPanel.add(txtPoints, gbc);

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
        btnSave.addActionListener(e -> saveCustomer());
        
        btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadData() {
        if (isEditMode && customer != null) {
            txtName.setText(customer.getCustomerName());
            txtPhone.setText(customer.getPhone());
            txtAddress.setText(customer.getAddress());
            txtPoints.setText(String.valueOf(customer.getPoints()));
        }
    }

    private void saveCustomer() {
        try {
            // Collect data
            if (customer == null) {
                customer = new Customer();
            }

            customer.setCustomerName(txtName.getText().trim());
            customer.setPhone(txtPhone.getText().trim());
            customer.setAddress(txtAddress.getText().trim());
            
            if (!isEditMode) {
                customer.setPoints(0); // Khách mới có 0 điểm
            }

            // Save
            if (isEditMode) {
                service.update(customer);
                JOptionPane.showMessageDialog(this,
                        "Cập nhật khách hàng thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                int id = service.create(customer);
                customer.setCustomerId(id);
                JOptionPane.showMessageDialog(this,
                        "Thêm khách hàng thành công!",
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

    public Customer getCustomer() {
        return customer;
    }
}
