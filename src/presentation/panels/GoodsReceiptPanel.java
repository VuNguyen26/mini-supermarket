package presentation.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import bus.GoodsReceiptService;
import dto.GoodsReceipt;
import dto.Supplier;
import presentation.components.datechooser.DateBetween;
import presentation.components.datechooser.DateChooser;
import presentation.components.datechooser.listener.DateChooserAction;
import presentation.components.datechooser.listener.DateChooserAdapter;

public class GoodsReceiptPanel extends JPanel {

	private static final GoodsReceiptService service = new GoodsReceiptService();

	private JTable table;
	private GoodsReceiptTableModel model;

	private JComboBox<DateFilterItem> cboDateFilter;
	private DateFilterItem selectedDateFilter;
	private DateChooser dateChooser;
	private JTextField customTimeTF;

	private JComboBox<SupplierFilterItem> cboSupplierFilter;
	private SupplierFilterItem selectedSupplierFilter;

	public GoodsReceiptPanel() {
		setOpaque(false);
		setLayout(new BorderLayout(12, 12));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		add(buildTop(), BorderLayout.NORTH);
		add(buildTable(), BorderLayout.CENTER);
	}

	// =======================
	// Main components builder
	// =======================
	private JComponent buildTop() {
		// ----------------------------------------------------------------------------------------
		// West side of top panel: Filter label, Date and supplier filter combo box, sorting button
		// ----------------------------------------------------------------------------------------
		JPanel west = new JPanel();

		DefaultComboBoxModel<DateFilterItem> cboDateFilterModel = new DefaultComboBoxModel<>();
		for (DateFilterItem i : DateFilterItem.values()) { cboDateFilterModel.addElement(i); }
		cboDateFilter = new JComboBox<>(cboDateFilterModel);
		cboDateFilter.setPreferredSize(new Dimension(120, 34));
		cboDateFilter.setSize(50, 10);
		cboDateFilter.addActionListener(e -> onDateFilterChange());

		DefaultComboBoxModel<SupplierFilterItem> cboSupplierModel = new DefaultComboBoxModel<>();
		cboSupplierModel.addElement(new SupplierFilterItem(null, "Tất cả nhà cung cấp"));
		List<Supplier> suppliers = service.getAllSuppilers();
		for (Supplier s : suppliers) {
			cboSupplierModel.addElement(new SupplierFilterItem(s.getSupplierId(), s.getSupplierName()));
		}
		cboSupplierFilter = new JComboBox<>(cboSupplierModel);
		cboSupplierFilter.setPreferredSize(new Dimension(240, 34));
		cboSupplierFilter.addActionListener(e -> onSupplierFilterChange());

		west.setOpaque(false);
		west.add(new JLabel("Lọc theo:"));
		west.add(cboDateFilter);
		west.add(cboSupplierFilter);

		// -------------------------------------------------
		// East side of top panel: Create new receipt button
		// -------------------------------------------------
		JPanel east = new JPanel();
		JButton addBtn = new JButton("Tạo phiếu mới");
		addBtn.setPreferredSize(new Dimension(120, 34));

		east.setOpaque(false);
		east.add(addBtn);

		// -------------------------------------------------============
		// South side of top panel: Custom date filter (hide on default)
		// -------------------------------------------------============
		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		customTimeTF = new JTextField(16);
		customTimeTF.setVisible(false);
		customTimeTF.setOpaque(false);

		south.setOpaque(false);
		south.add(customTimeTF);

		// ---------
		// Top panel
		// ---------
		JPanel top = new JPanel(new BorderLayout(10, 10));
		initDateChooser();

		top.setOpaque(false);
		top.add(south, BorderLayout.SOUTH);
		top.add(west, BorderLayout.WEST);
		top.add(east, BorderLayout.EAST);
		return top;
	}

	private JComponent buildTable() {
		model = new GoodsReceiptTableModel();
		table = new JTable(model);
		table.setRowHeight(28);
		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int col = table.columnAtPoint(e.getPoint());
				GoodsReceiptSort newSort = switch (col) {
					case 0 -> GoodsReceiptSort.SUPPLIER;
					case 1 -> GoodsReceiptSort.USER;
					case 2 -> GoodsReceiptSort.CREATED_AT;
					case 3 -> GoodsReceiptSort.TOTAL_AMOUNT;
					default -> null;
				};

				if (newSort == null) return;
				if (newSort == model.getSortBy()) {
					model.toggleAscending();
				} else {
					model.setSortBy(newSort);
					model.setAscending(true);
				}
				model.loadData();
			}
		});
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(228, 231, 236)));
		return scrollPane;
	}

	// =======================
	// Setting up date chooser
	// =======================
	private void initDateChooser() {
		dateChooser = new DateChooser();
		dateChooser.setTextField(customTimeTF);
		dateChooser.setDateSelectionMode(DateChooser.DateSelectionMode.BETWEEN_DATE_SELECTED);
		dateChooser.setDateFormat(new SimpleDateFormat("d/M/yyyy"));
		dateChooser.last28Days();
		dateChooser.addActionDateChooserListener(new DateChooserAdapter() {
			@Override
			public void dateBetweenChanged(DateBetween date, DateChooserAction action) {
				model.setFrom(date.getFromLocalDateTime());
				model.setTo(date.getToLocalDateTime());
				model.loadData();
			}
		});
	}

	// ============================
	// JComboBox callback functions
	// ============================
	private void onDateFilterChange() {
		selectedDateFilter = (DateFilterItem) cboDateFilter.getSelectedItem();
		customTimeTF.setVisible(selectedDateFilter == DateFilterItem.CUSTOM);
		switch (selectedDateFilter) {
			case NONE -> model.setNone();
			case TODAY -> model.setToday();
			case THIS_WEEK -> model.setThisWeek();
			case THIS_MONTH -> model.setThisMonth();
			case THIS_YEAR -> model.setThisYear();
			case CUSTOM -> {
				model.setTo(dateChooser.getSelectedDateBetween().getToLocalDateTime());
				model.setFrom(dateChooser.getSelectedDateBetween().getFromLocalDateTime());
			}
		}
		model.loadData();
		revalidate();
	}

	private void onSupplierFilterChange() {
		selectedSupplierFilter = (SupplierFilterItem) cboSupplierFilter.getSelectedItem();
		model.setSupplierId(selectedSupplierFilter.id);
		model.loadData();
	}

	// ============
	// JTable Model
	// ============
	private static class GoodsReceiptTableModel extends AbstractTableModel {
		private final String[] cols = { "Nhà cung cấp", "Tên nhân viên", "Thời gian nhập", "Tổng tiền", "Ghi chú" };
		private List<GoodsReceipt> receipts;
		private Integer supplierId;
		private LocalDateTime from;
		private LocalDateTime to;
		private GoodsReceiptSort sortBy;
		private boolean isAscending;

		public GoodsReceiptTableModel() {
			supplierId = null;
			from = null;
			to = null;
			sortBy = GoodsReceiptSort.CREATED_AT;
			isAscending = true;
			receipts = service.getList(supplierId, from, to, sortBy.column(), isAscending);
		}

		public void loadData() {
			this.receipts = service.getList(supplierId, from, to, sortBy.column(), isAscending);
			fireTableDataChanged();
		}

		@Override public int getColumnCount() { return cols.length; }
		@Override public String getColumnName(int col) { return cols[col]; }
		@Override public int getRowCount() { return receipts.size(); }
		@Override
		public Object getValueAt(int row, int col) {
			GoodsReceipt r = receipts.get(row);
			return switch (col) {
				case 0 -> service.getSupplierName(r.getSupplierId());
				case 1 -> service.getUserName(r.getCreatedBy());
				case 2 -> r.getCreatedAt();
				case 3 -> r.getTotalAmount();
				case 4 -> r.getNote();
				default -> null;
			};
		}

		public void setNone() { this.from = null; this.to = null; }
		public void setToday() { this.from = LocalDate.now().atStartOfDay(); this.to = from.plusDays(1); }
		public void setThisWeek() { this.from = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay(); this.to = from.plusWeeks(1); }
		public void setThisMonth() { this.from = LocalDate.now().withDayOfMonth(1).atStartOfDay(); this.to = from.plusMonths(1); }
		public void setThisYear() { this.from = LocalDate.now().withDayOfYear(1).atStartOfDay(); this.to = from.plusYears(1); }
		public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
		public void setFrom(LocalDateTime from) { this.from = from; }
		public void setTo(LocalDateTime to) { this.to = to; }
		public void setSortBy(GoodsReceiptSort sortBy) { this.sortBy = sortBy; }
		public void setAscending(boolean isAscending) { this.isAscending = isAscending; }
		public GoodsReceiptSort getSortBy() { return this.sortBy; }
		public void toggleAscending() { this.isAscending = !this.isAscending; }
	}

	// =======================
	// Helper for JTable Model
	// =======================
	public enum GoodsReceiptSort {
		SUPPLIER("gr.supplier_id"),
		USER("gr.created_by"),
		CREATED_AT("gr.created_at"),
		TOTAL_AMOUNT("gr.total_amount");
		private final String column;
		GoodsReceiptSort(String column) { this.column = column; }
		public String column() { return column; } }

	// ====================
	// Helper for JComboBox
	// ====================
	private enum DateFilterItem {
		NONE("Mọi thời gian"),
		TODAY("Hôm nay"),
		THIS_WEEK("Tuần này"),
		THIS_MONTH("Tháng này"),
		THIS_YEAR("Năm nay"),
		CUSTOM("Tùy chọn");
		private final String value;
		DateFilterItem(String value) { this.value = value; }
		@Override public String toString() { return value; }
	}

	private class SupplierFilterItem {
		public Integer id;
		public String name;
		public SupplierFilterItem(Integer id, String name) { this.id = id; this.name = name; }
		@Override public String toString() { return id != null ? name + " (" + id + ")" : name; }
	}
}
