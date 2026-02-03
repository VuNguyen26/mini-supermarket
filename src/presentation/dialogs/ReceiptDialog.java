package presentation.dialogs;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import bus.*;
import dto.*;
import bus.AuthService.AuthUser;
import presentation.components.datechooser.*;

import java.awt.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReceiptDialog extends JDialog {

	private ProductService productService = new ProductService();
	private SupplierService supplierService = new SupplierService();
	private GoodsReceiptService grService = new GoodsReceiptService();
	private final Runnable inventoryUpdatedCallback;

	private AuthUser currentUser;

	private GoodsReceipt receiptToShow;

	private List<ProductRow> productRows = new ArrayList<>();

	private List<Product> allProducts;
	private List<Supplier> allSuppliers;

	private JComboBox<Supplier> cboSupplier;
	private JPanel productList;
	private JScrollPane productListPane;
	private JButton createBtn, addProductBtn;
	private JTextField txtTotalAmount, txtNote;

	public ReceiptDialog(Window owner, AuthUser currentUser, GoodsReceipt receiptToShow, Runnable inventoryUpdatedCallback) {
		super(owner,
			receiptToShow == null ? "Tạo phiếu nhập" : "Chi tiết phiếu nhập",
			ModalityType.APPLICATION_MODAL
		);
		this.currentUser = currentUser;
		this.receiptToShow = receiptToShow;
		this.inventoryUpdatedCallback = inventoryUpdatedCallback;
		allProducts = productService.getAll();
		allSuppliers = supplierService.getAll();
		initUI();
		if (receiptToShow != null) {
			loadReceiptData();
		}
	}

	private void initUI() {
		setLayout(new BorderLayout(10, 10));
		setResizable(true);
		setSize(new Dimension(1120, 780));
		// Header
		JLabel headerLabel = new JLabel(
			receiptToShow == null ? "Tạo phiếu nhập" : "Chi tiết phiếu nhập",
			SwingConstants.CENTER
		);
		headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
		headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

		// Body
		JPanel body = buildBody();
		body.setOpaque(false);

		// Footer
		JButton closeBtn = new JButton("Quay lại");
    closeBtn.setBackground(new Color(100, 100, 100));
		closeBtn.setForeground(Color.WHITE);
		closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		closeBtn.setPreferredSize(new Dimension(140, 40));
		closeBtn.addActionListener(e -> dispose());

		createBtn = new JButton("Xác nhận");
    createBtn.setBackground(new Color(76, 175, 80));
		createBtn.setForeground(Color.WHITE);
		createBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		createBtn.setPreferredSize(new Dimension(140, 40));
		createBtn.addActionListener(e -> handleConfirm());

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
		footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		footer.add(closeBtn);
		footer.add(createBtn);

		add(headerLabel, BorderLayout.NORTH);
		add(body, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);

		if (receiptToShow != null) {
			createBtn.setVisible(false);
			addProductBtn.setVisible(false);
			cboSupplier.setEnabled(false);
			txtNote.setEditable(false);
		}
		setLocationRelativeTo(getOwner());
	}

	private JPanel buildBody() {
		// TOP
		JLabel label = new JLabel("Nhà cung cấp *");
		label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		addProductBtn = new JButton("Thêm sản phẩm");
    addProductBtn.setBackground(new Color(33, 150, 243));
		addProductBtn.setForeground(Color.WHITE);
		addProductBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		addProductBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addProductBtn.setPreferredSize(new Dimension(140, 40));
		addProductBtn.addActionListener(e -> addProductRow());

		loadSuppliers();
		cboSupplier.setCursor(new Cursor(Cursor.HAND_CURSOR));
		JPanel componentWrapper = new JPanel(new BorderLayout());
		componentWrapper.add(cboSupplier, BorderLayout.WEST);
		componentWrapper.add(addProductBtn, BorderLayout.EAST);

		JPanel topWrapper = new JPanel(new BorderLayout(0, 5));
		topWrapper.add(label, BorderLayout.NORTH);
		topWrapper.add(componentWrapper, BorderLayout.SOUTH);

		JPanel top = new JPanel(new BorderLayout(10, 10));
		top.add(topWrapper, BorderLayout.NORTH);

		// CENTER
		productList = new JPanel();
		productList.setLayout(new BoxLayout(productList, BoxLayout.Y_AXIS));
		productList.setOpaque(true);
		productList.setBackground(Color.WHITE);
		if (receiptToShow == null) {
			addProductRow();
		}

		productListPane = new JScrollPane(productList);
		productListPane.setBorder(BorderFactory.createLineBorder(new Color(228, 231, 236)));
		productListPane.getVerticalScrollBar().setUnitIncrement(16);
		productListPane.setOpaque(true);
		productListPane.setBackground(Color.WHITE);

		// BOTTOM
		txtNote = new JTextField(40);
		txtNote.setPreferredSize(new Dimension(300, 34));

		txtTotalAmount = new JTextField("0 đ", 15);
		txtTotalAmount.setEditable(false);

		JLabel labelNote = new JLabel("Ghi chú");
		labelNote.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		JLabel labelTotalAmount = new JLabel("Tổng tiền: ");
		labelTotalAmount.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		JPanel noteWrapper = new JPanel(new BorderLayout(0, 5));
		noteWrapper.add(labelNote, BorderLayout.NORTH);
		noteWrapper.add(txtNote, BorderLayout.SOUTH);

		JPanel totalAmountWrapper = new JPanel(new BorderLayout(0, 5));
		totalAmountWrapper.add(labelTotalAmount, BorderLayout.NORTH);
		totalAmountWrapper.add(txtTotalAmount, BorderLayout.SOUTH);

		JPanel bottom = new JPanel(new BorderLayout());
		bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		bottom.add(noteWrapper, BorderLayout.WEST);
		bottom.add(totalAmountWrapper, BorderLayout.EAST);

		// MAIN
		JPanel main = new JPanel(new BorderLayout(10, 10));
		main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		main.add(top, BorderLayout.NORTH);
		main.add(productListPane, BorderLayout.CENTER);
		main.add(bottom, BorderLayout.SOUTH);
		return main;
	}

	private void addProductRow() {
		ProductRow row = new ProductRow(productRows.size() + 1);
		productRows.add(row);

		row.panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
				BorderFactory.createEmptyBorder(10, 5, 10, 5)));

		productList.add(row.panel);

		revalidate();
		repaint();

		// Scroll to the bottom after UI update
		SwingUtilities.invokeLater(() -> {
			JScrollBar vertical = productListPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMaximum());
		});
	}
	private void addProductRowWithData(GoodsReceiptDetail detail) {
		ProductRow row = new ProductRow(productRows.size() + 1);
		for (int i = 0; i < row.cboProduct.getItemCount(); i++) {
			Product p = row.cboProduct.getItemAt(i);
			if (p != null && p.getProductId() == detail.getProductId()) {
				row.cboProduct.setSelectedIndex(i);
				break;
			}
		}

		row.txtQty.setText(String.valueOf(detail.getQty()));
		row.txtPrice.setText(detail.getUnitPrice().toString());
		row.txtLot.setText(detail.getLotCode());

		if (detail.getMfgDate() != null) row.dcMfg.setSelectedDate(detail.getMfgDate());
		if (detail.getExpiry() != null) row.dcExpiry.setSelectedDate(detail.getExpiry());
		row.cboProduct.setEnabled(false);
		row.txtQty.setEditable(false);
		row.txtPrice.setEditable(false);
		row.txtLot.setEditable(false);
		row.txtMfg.setEnabled(false);
		row.txtExpiry.setEnabled(false);
		row.btnDelete.setVisible(false);
		productRows.add(row);
		productList.add(row.panel);
	}

	private class ProductRow {
		JPanel panel;
		JLabel labelIndex;
		JComboBox<Product> cboProduct;
		JTextField txtQty, txtPrice, txtLot, txtMfg, txtExpiry, txtLineTotal;
		JButton btnDelete;
		DateChooser dcMfg, dcExpiry;

		public ProductRow(int index) {
			panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

			labelIndex = new JLabel(String.valueOf(index));
			labelIndex.setFont(new Font("Segoe UI", Font.BOLD, 14));
			labelIndex.setForeground(new Color(150, 150, 150));
			labelIndex.setPreferredSize(new Dimension(30, 40));

			cboProduct = createProductComboBox();
			cboProduct.setCursor(new Cursor(Cursor.HAND_CURSOR));

			txtQty = new JTextField(5);
			txtPrice = new JTextField(7);
			// Strict input
			((AbstractDocument) txtQty.getDocument()).setDocumentFilter(new NumberOnlyFilter());
			((AbstractDocument) txtPrice.getDocument()).setDocumentFilter(new DecimalFilter());
			txtLot = new JTextField(12);

			txtMfg = new JTextField(6);
			txtMfg.setCursor(new Cursor(Cursor.HAND_CURSOR));
			dcMfg = new DateChooser();
			dcMfg.setTextField(txtMfg);
			dcMfg.setSelectedDate();

			txtExpiry = new JTextField(6);
			txtExpiry.setCursor(new Cursor(Cursor.HAND_CURSOR));
			dcExpiry = new DateChooser();
			dcExpiry.setTextField(txtExpiry);
			dcExpiry.setSelectedDate();

			txtLineTotal = new JTextField("0 đ", 13);
			txtLineTotal.setEditable(false);
			txtLineTotal.setBackground(new Color(245, 245, 245));
			txtLineTotal.setHorizontalAlignment(JTextField.RIGHT);

			txtQty.getDocument().addDocumentListener(new TotalWatcher());
			txtPrice.getDocument().addDocumentListener(new TotalWatcher());

			panel.add(wrapField("#", labelIndex));
			panel.add(wrapField("Sản phẩm *", cboProduct));
			panel.add(wrapField("Số lượng *", txtQty));
			panel.add(wrapField("Đơn giá *", txtPrice));
			panel.add(wrapField("Mã lô", txtLot));
			panel.add(wrapField("NXS", txtMfg));
			panel.add(wrapField("HSD", txtExpiry));
			panel.add(wrapField("Thành tiền", txtLineTotal));

			btnDelete = new JButton("X");
			btnDelete.setForeground(Color.RED);
			btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnDelete.addActionListener(e -> removeRow(this));
			JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			btnWrapper.add(wrapField(" ", btnDelete));
			btnWrapper.setOpaque(true);
			btnWrapper.setBackground(Color.WHITE);
			panel.add(btnWrapper);
			panel.setOpaque(true);
			panel.setBackground(Color.WHITE);
		}

		private void calculateLineTotal() {
			SwingUtilities.invokeLater(() -> {
				try {
					String qtyStr = txtQty.getText().trim();
					String priceStr = txtPrice.getText().trim();
					if (qtyStr.isEmpty() || priceStr.isEmpty()) {
						txtLineTotal.setText("0 đ");
						updateGrandTotal();
						return;
					}
					double qty = Double.parseDouble(qtyStr);
					double price = Double.parseDouble(priceStr.replace(',', '.'));
					double total = qty * price;
					txtLineTotal.setText(util.MoneyUtils.format(total));
					updateGrandTotal();

				} catch (NumberFormatException e) {
					txtLineTotal.setText("0 đ");
				}
			});
		}

		private class TotalWatcher implements DocumentListener {
			public void insertUpdate(DocumentEvent e) { calculateLineTotal(); }
			public void removeUpdate(DocumentEvent e) { calculateLineTotal(); }
			public void changedUpdate(DocumentEvent e) { calculateLineTotal(); }
		}
	}
	private void loadReceiptData() {
		for (int i = 0; i < cboSupplier.getItemCount(); i++) {
			Supplier s = cboSupplier.getItemAt(i);
			if (s != null && s.getSupplierId() == receiptToShow.getSupplierId()) {
				cboSupplier.setSelectedIndex(i);
				break;
			}
		}
		txtNote.setText(receiptToShow.getNote());
		List<GoodsReceiptDetail> details = grService.getDetailsByReceiptId(receiptToShow.getGrId());
		productRows.clear();
		productList.removeAll();
		for (GoodsReceiptDetail detail : details) {
			addProductRowWithData(detail);
		}
		updateGrandTotal();
	}

	private void handleConfirm() {
		if (cboSupplier.getSelectedItem() == null) {
			JOptionPane.showMessageDialog(this,
				"Vui lòng chọn nhà cung cấp",
				"Cảnh báo", JOptionPane.WARNING_MESSAGE
			);
			return;
		}
		List<GoodsReceiptDetail> details = new ArrayList<>();
		for (int i = 0; i < productRows.size(); i++) {
			ProductRow row = productRows.get(i);
			Product selectedProduct = (Product) row.cboProduct.getSelectedItem();

			if (selectedProduct == null) {
				JOptionPane.showMessageDialog(this,
					"Vui lòng chọn sản phẩm ở dòng " + (i + 1),
					"Cảnh báo", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String qtyStr = row.txtQty.getText();
			int qty;
			if (qtyStr.isEmpty()) {
				JOptionPane.showMessageDialog(this,
					"Số lượng sản phẩm không được để trống! (Dòng " + (i + 1) + ")",
					"Cảnh báo", JOptionPane.WARNING_MESSAGE);
				row.txtQty.requestFocus();
				return;
			} else {
				qty = Integer.parseInt(qtyStr);
				if (qty < 0) {
					JOptionPane.showMessageDialog(this,
						"Số lượng sản phẩm phải lớn hơn 0! (Dòng " + (i + 1) + ")",
						"Cảnh báo", JOptionPane.WARNING_MESSAGE);
					row.txtQty.requestFocus();
					return;
				}
			}

			String priceStr = row.txtPrice.getText();
			if (priceStr.isEmpty()) {
				JOptionPane.showMessageDialog(this,
					"Đơn giá không được để trống! (Dòng " + (i + 1) + ")",
					"Cảnh báo", JOptionPane.WARNING_MESSAGE);
				row.txtPrice.requestFocus();
				return;
			}

			BigDecimal unitPrice = new BigDecimal(priceStr.replace(',', '.'));

			GoodsReceiptDetail grd = new GoodsReceiptDetail();
			grd.setProductId(selectedProduct.getProductId());
			grd.setQty(qty);
			grd.setUnitPrice(unitPrice);
			grd.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(qty)));
			grd.setLotCode(row.txtLot.getText());
			RDate mfgDate = row.dcMfg.getSelectedRDate();
			if (mfgDate != null) {
				if (mfgDate.toLocalDate().isAfter(LocalDate.now())) {
					JOptionPane.showMessageDialog(this,
						"NSX không hợp lệ! (Dòng " + (i + 1) + ")",
						"Cảnh báo", JOptionPane.WARNING_MESSAGE);
					return;
				}
				grd.setMfgDate(mfgDate.toLocalDate());
			} else {
				grd.setMfgDate(null);
			}
			RDate expiry = row.dcExpiry.getSelectedRDate();
			if (expiry != null) {
				if (expiry.toLocalDate().isBefore(LocalDate.now())) {
					JOptionPane.showMessageDialog(this,
						"Sản phẩm đã hết hạn! (Dòng " + (i + 1) + ")",
						"Cảnh báo", JOptionPane.WARNING_MESSAGE);
					return;
				}
				grd.setExpiry(expiry.toLocalDate());
			} else {
				grd.setExpiry(null);
			}
			details.addLast(grd);
		}
		if (details.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Vui lòng thêm ít nhất một sản phẩm.",
				"Cảnh báo", JOptionPane.WARNING_MESSAGE
			);
			return;
		}
		GoodsReceipt gr = new GoodsReceipt();
		Supplier supplier = (Supplier) cboSupplier.getSelectedItem();
		gr.setSupplierId(supplier.getSupplierId());
		gr.setCreatedBy(currentUser.userId);
		gr.setCreatedAt(LocalDateTime.now());
		gr.setTotalAmount(updateGrandTotal());
		gr.setNote(txtNote.getText());

		System.out.println("gr.totalAmount: " + gr.getTotalAmount());
		System.out.println("detials.size(): " + details.size());

		if (grService.createFullReceipt(gr, details)) {
			JOptionPane.showMessageDialog(this,
				"Tạo phiếu nhập hàng thành công!",
				"Thông báo", JOptionPane.INFORMATION_MESSAGE);
			if (inventoryUpdatedCallback != null) {
				inventoryUpdatedCallback.run();
			}
			dispose();
		} else {
			JOptionPane.showMessageDialog(this,
				"Tạo phiếu nhập hàng thất bại. Vui lòng thử lại!",
				"Thông báo", JOptionPane.ERROR_MESSAGE);
		}
	}

	private BigDecimal updateGrandTotal() {
		BigDecimal grandTotal = new BigDecimal(0);
		for (ProductRow row : productRows) {
			try {
				String q = row.txtQty.getText().trim();
				String p = row.txtPrice.getText().trim();
				if (!q.isEmpty() && !p.isEmpty()) {
					BigDecimal qty   = new BigDecimal(q);
					BigDecimal price = new BigDecimal(p.replace(',', '.'));

					grandTotal = grandTotal.add(price.multiply(qty));
				}
			} catch (NumberFormatException ignored) {}
		}
		txtTotalAmount.setText(util.MoneyUtils.format(grandTotal));
		return grandTotal;
	}

	private JPanel wrapField(String labelText, JComponent component) {
		JPanel wrapper = new JPanel(new BorderLayout(0, 5));
		JLabel label = new JLabel(labelText);
		label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		wrapper.add(label, BorderLayout.NORTH);
		wrapper.add(component, BorderLayout.SOUTH);
		wrapper.setOpaque(true);
		wrapper.setBackground(Color.WHITE);
		return wrapper;
	}

	private void removeRow(ProductRow row) {
		if (productRows.size() > 1) {
			productRows.remove(row);
			productList.remove(row.panel);
			for (int i = 0; i < productRows.size(); i++) {
				productRows.get(i).labelIndex.setText(String.valueOf(i + 1));
			}
			updateGrandTotal();
			revalidate();
			repaint();
		}
	}

	private void loadSuppliers() {
		DefaultComboBoxModel<Supplier> cboSupplierModel = new DefaultComboBoxModel<>();
		cboSupplierModel.addElement(null);
		for (Supplier s : allSuppliers) {
			cboSupplierModel.addElement(s);
		}
		cboSupplier = new JComboBox<>(cboSupplierModel);
		cboSupplier.setPreferredSize(new Dimension(240, 34));
	}

	private JComboBox<Product> createProductComboBox() {
		DefaultComboBoxModel<Product> model = new DefaultComboBoxModel<>();
		model.addElement(null);
		for (Product p : allProducts) {
			model.addElement(p);
		}
		JComboBox<Product> comboBox = new JComboBox<>(model);
		comboBox.setPreferredSize(new Dimension(240, 34));
		return comboBox;
	}

	public class NumberOnlyFilter extends DocumentFilter {
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			if (string.matches("\\d*")) super.insertString(fb, offset, string, attr);
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			if (text.matches("\\d*")) super.replace(fb, offset, length, text, attrs);
		}
	}
	public class DecimalFilter extends DocumentFilter {
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
		throws BadLocationException {
			StringBuilder builder = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
			builder.insert(offset, string);
			if (isValid(builder.toString())) {
				super.insertString(fb, offset, string, attr);
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
		throws BadLocationException {
			StringBuilder builder = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
			builder.replace(offset, offset + length, text);
			if (isValid(builder.toString())) {
				super.replace(fb, offset, length, text, attrs);
			}
		}

		private boolean isValid(String text) {
			if (text.isEmpty()) return true;
			// Allows digits and optionally one dot followed by more digits
			return text.matches("\\d*([.,]\\d*)?");
		}
	}
}
