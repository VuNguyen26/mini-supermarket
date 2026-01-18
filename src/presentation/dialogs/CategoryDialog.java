package presentation.dialogs;

import bus.CategoryService;
import dto.Category;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CategoryDialog extends JDialog {

    private final CategoryService service = new CategoryService();
    private Category category;
    private boolean isEditMode;
    private boolean saved = false;

    private JTextField txtCode;
    private JTextField txtName;
    private JTextArea txtDescription;
    private JComboBox<String> cboStatus;
    private JButton btnSave;
    private JButton btnCancel;

    public CategoryDialog(Frame parent, Category category) {
        super(parent, category == null ? "Thêm danh mục" : "Sửa danh mục", true);
        this.category = category;
        this.isEditMode = (category != null);

        initComponents();
        loadData();
        
        setSize(500, 400);
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

        // Category Code
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Mã danh mục: *"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtCode = new JTextField(20);
        formPanel.add(txtCode, gbc);

        // Category Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Tên danh mục: *"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtName = new JTextField(20);
        formPanel.add(txtName, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        formPanel.add(scrollDesc, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        cboStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        formPanel.add(cboStatus, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        btnSave = new JButton("Lưu");
        btnSave.setPreferredSize(new Dimension(100, 35));
        btnSave.addActionListener(e -> saveCategory());
        
        btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadData() {
        if (isEditMode && category != null) {
            txtCode.setText(category.getCategoryCode());
            txtCode.setEnabled(false); // Don't allow editing code
            txtName.setText(category.getCategoryName());
            txtDescription.setText(category.getDescription());
            cboStatus.setSelectedItem(category.getStatus());
        } else {
            cboStatus.setSelectedItem("ACTIVE");
        }
    }

    private void saveCategory() {
        try {
            // Collect data
            if (category == null) {
                category = new Category();
            }

            category.setCategoryCode(txtCode.getText().trim());
            category.setCategoryName(txtName.getText().trim());
            category.setDescription(txtDescription.getText().trim());
            category.setStatus((String) cboStatus.getSelectedItem());

            // Save
            if (isEditMode) {
                service.update(category);
                JOptionPane.showMessageDialog(this,
                        "Cập nhật danh mục thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                int id = service.create(category);
                category.setCategoryId(id);
                JOptionPane.showMessageDialog(this,
                        "Thêm danh mục thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Category getCategory() {
        return category;
    }
}
