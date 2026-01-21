package presentation.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import dto.InventoryLot;
import dto.Product;
import bus.InventoryLotService;
import bus.ProductService;
import bus.AuthService.AuthUser;

public class InventoryLotDialog extends JDialog {
	private final InventoryLotService lotService = new InventoryLotService();
	private final ProductService productService = new ProductService();
	private final InventoryLot lot;
	private final AuthUser currentUser;
	private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public InventoryLotDialog(Window owner, AuthUser currentUser, InventoryLot lot) {
		super(owner, "Chi tiết lô hàng", ModalityType.APPLICATION_MODAL);
		this.lot = lot;
		this.currentUser = currentUser;

		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout());
		setSize(600, 500);
		setLocationRelativeTo(getOwner());

		JPanel content = new JPanel(new GridBagLayout());
		content.setBorder(new EmptyBorder(25, 25, 25, 25));
		content.setBackground(Color.WHITE);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 15, 5);

		Product product = productService.getById(lot.getProductId());

		addFormField(content, gbc, 0, 0, 6, "Mã sản phẩm", String.valueOf(product.getProductId()));

		addFormField(content, gbc, 0, 1, 3, "Mã lô", lot.getLotCode() != null ? lot.getLotCode() : "N/A");
		addFormField(content, gbc, 3, 1, 3, "Trạng thái", lot.getStatus().name());

		addFormField(content, gbc, 0, 2, 3, "Ngày nhập", lot.getReceivedDate().format(df));
		addFormField(content, gbc, 3, 2, 3, "Hạn sử dụng", lot.getExpiry() != null ? lot.getExpiry().format(df) : "Không có");

		addFormField(content, gbc, 0, 3, 2, "Tồn", String.valueOf(lot.getQtyRemaining()));
		addFormField(content, gbc, 2, 3, 2, "Nhập", String.valueOf(lot.getQtyIn()));
		addFormField(content, gbc, 4, 3, 2, "Xuất", String.valueOf(lot.getQtyOut()));

		add(content, BorderLayout.CENTER);
		add(buildFooter(), BorderLayout.SOUTH);
	}

	private void addFormField(JPanel panel, GridBagConstraints gbc, int x, int y, int width, String labelText, String value) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.weightx = (width == 2) ? 1.0 : 0.5;

		JPanel fieldPanel = new JPanel(new BorderLayout(0, 5));
		fieldPanel.setOpaque(false);

		JLabel label = new JLabel(labelText);
		label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		JTextField textField = new JTextField(value);
		textField.setEditable(false);
		textField.setPreferredSize(new Dimension(0, 35));
		textField.setBackground(new Color(245, 245, 245));

		fieldPanel.add(label, BorderLayout.NORTH);
		fieldPanel.add(textField, BorderLayout.CENTER);
		panel.add(fieldPanel, gbc);
	}

	private JPanel buildFooter() {
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
		footer.setBackground(Color.WHITE);

		JButton closeBtn = new JButton("Quay lại");
		closeBtn.setPreferredSize(new Dimension(120, 40));
		closeBtn.setBackground(new Color(158, 158, 158));
		closeBtn.setForeground(Color.WHITE);
		closeBtn.addActionListener(e -> dispose());

		JButton expireBtn = new JButton("Đánh dấu hết hạn");
		expireBtn.setPreferredSize(new Dimension(160, 40));
		expireBtn.setBackground(new Color(244, 67, 54));
		expireBtn.setForeground(Color.WHITE);
		expireBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

		expireBtn.setVisible(lot.getExpiry() != null && lot.getStatus() == InventoryLot.Status.AVAILABLE);
		expireBtn.addActionListener(e -> handleMarkExpired());

		footer.add(closeBtn);
		footer.add(expireBtn);
		return footer;
	}

	private void handleMarkExpired() {
		// TODO: Check for perm
		int confirm = JOptionPane.showConfirmDialog(this,
			"Xác nhận đánh dấu lô hàng này đã hết hạn?",
			"Xác nhận", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			boolean success = lotService.markAsExpired(lot.getLotId());
			if (success) {
				JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!");
				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
