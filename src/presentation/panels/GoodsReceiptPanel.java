package presentation.panels;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import bus.GoodsReceiptService;
import bus.SupplierService;
import bus.AuthService.AuthUser;
import dto.GoodsReceipt;
import dto.Supplier;
import presentation.components.datechooser.DateBetween;
import presentation.components.datechooser.DateChooser;
import presentation.components.datechooser.listener.DateChooserAction;
import presentation.components.datechooser.listener.DateChooserAdapter;
import presentation.dialogs.ReceiptDialog;
import util.DateUtils;
import util.MoneyUtils;

public class GoodsReceiptPanel extends JPanel {

	private static final GoodsReceiptService service = new GoodsReceiptService();
	private final SupplierService supplierService = new SupplierService();

	private AuthUser currentUser;

	private JTable table;
	private GoodsReceiptTableModel model;

	private JComboBox<DateFilterItem> cboDateFilter;
	private DateFilterItem selectedDateFilter;
	private DateChooser dateChooser;
	private JTextField customTimeTF;

	private JComboBox<SupplierFilterItem> cboSupplierFilter;
	private SupplierFilterItem selectedSupplierFilter;

	public GoodsReceiptPanel(AuthUser currentUser) {
		this.currentUser = currentUser;
		setOpaque(false);
		setLayout(new BorderLayout(12, 12));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		add(buildTop(), BorderLayout.NORTH);
		add(buildTable(), BorderLayout.CENTER);
	}

	private JComponent buildTop() {
		// West side of top panel: Filter label, Date and supplier filter combo box, sorting button
		DefaultComboBoxModel<DateFilterItem> cboDateFilterModel = new DefaultComboBoxModel<>();
		for (DateFilterItem i : DateFilterItem.values()) { cboDateFilterModel.addElement(i); }
		cboDateFilter = new JComboBox<>(cboDateFilterModel);
		cboDateFilter.setPreferredSize(new Dimension(120, 34));
		cboDateFilter.setSize(50, 10);
		cboDateFilter.addActionListener(e -> onDateFilterChange());

		loadSuppliersFilter();

		JPanel west = new JPanel();
		west.setOpaque(false);
		west.add(new JLabel("Lọc theo:"));
		west.add(cboDateFilter);
		west.add(cboSupplierFilter);

		// East side of top panel: Refresh and Create new receipt button
		JButton refreshBtn = new JButton("Làm mới");
    refreshBtn.setBackground(new Color(33, 150, 243));
		refreshBtn.setForeground(Color.WHITE);
		refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		refreshBtn.setPreferredSize(new Dimension(140, 40));
		refreshBtn.addActionListener(e -> {model.loadData();});

		JButton addBtn = new JButton("Tạo phiếu mới");
    addBtn.setBackground(new Color(76, 175, 80));
		addBtn.setForeground(Color.WHITE);
		addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addBtn.setPreferredSize(new Dimension(140, 40));
		addBtn.addActionListener(e -> openReceiptDialog());

		JPanel east = new JPanel();
		east.setOpaque(false);
		east.add(refreshBtn);
		east.add(addBtn);

		// South side of top panel: Custom date filter (hide on default)
		customTimeTF = new JTextField(16);
		customTimeTF.setVisible(false);
		customTimeTF.setOpaque(false);

		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		south.setOpaque(false);
		south.add(customTimeTF);

		// Top panel
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

		table.getColumnModel().getColumn(0).setMaxWidth(50);

		table.setRowHeight(40);
		table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleViewDetail(e);
			}
		});

		table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
		table.getTableHeader().setBackground(new Color(33, 150, 243));
		table.getTableHeader().setForeground(Color.WHITE);
		table.getTableHeader().setPreferredSize(new Dimension(0, 45));
		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleSortColumn(e);
			}
		});
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(228, 231, 236)));
		return scrollPane;
	}

	private void openViewDetailDialog(GoodsReceipt receipt) {
		Window owner = SwingUtilities.getWindowAncestor(this);
		ReceiptDialog dialog = new ReceiptDialog(owner, currentUser, receipt);
		dialog.setVisible(true);
	}
	private void handleViewDetail(MouseEvent e) {
		// On double click
		if (e.getClickCount() == 2) {
			int row = table.getSelectedRow();
			if (row != -1) {
				GoodsReceipt selected = model.getReceiptAt(row);
				openViewDetailDialog(selected);
			}
		}
	}


	private void handleSortColumn(MouseEvent e) {
		int col = table.columnAtPoint(e.getPoint());
		GoodsReceiptSort newSort = switch (col) {
			case 1 -> GoodsReceiptSort.SUPPLIER;
			case 2 -> GoodsReceiptSort.USER;
			case 3 -> GoodsReceiptSort.CREATED_AT;
			case 4 -> GoodsReceiptSort.TOTAL_AMOUNT;
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

	// Setting up date chooser
	private void initDateChooser() {
		dateChooser = new DateChooser();
		dateChooser.setTextField(customTimeTF);
		dateChooser.setDateSelectionMode(DateChooser.DateSelectionMode.BETWEEN_DATE_SELECTED);
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

	// Button callback functions
	private void openReceiptDialog() {
		Window owner = SwingUtilities.getWindowAncestor(this);
		ReceiptDialog dialog = new ReceiptDialog(owner, currentUser, null);
		dialog.setVisible(true);
	}

	// ComboBox callback functions
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

	private class GoodsReceiptTableModel extends AbstractTableModel {
		private final String[] cols = { "STT", "Nhà cung cấp", "Tên nhân viên", "Thời gian nhập", "Tổng tiền", "Ghi chú" };
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
			receipts = service.getReceiptsList(supplierId, from, to, sortBy.column(), isAscending);
		}

		public void loadData() {
			this.receipts = service.getReceiptsList(supplierId, from, to, sortBy.column(), isAscending);
			fireTableDataChanged();
		}

		public GoodsReceipt getReceiptAt(int row) {
			return receipts.get(row);
		}

		@Override public int getColumnCount() { return cols.length; }
		@Override public String getColumnName(int col) { return cols[col]; }
		@Override public int getRowCount() { return receipts.size(); }
		@Override
		public Object getValueAt(int row, int col) {
			GoodsReceipt r = receipts.get(row);
			return switch (col) {
        case 0 -> row + 1;
				case 1 -> supplierService.getById(r.getSupplierId());
				case 2 -> service.getUserNameById(r.getCreatedBy());
				case 3 -> DateUtils.formatDateTime(r.getCreatedAt());
				case 4 -> MoneyUtils.format(r.getTotalAmount());
				case 5 -> r.getNote();
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

	// Use to sort column
	public enum GoodsReceiptSort {
		SUPPLIER("gr.supplier_id"),
		USER("gr.created_by"),
		CREATED_AT("gr.created_at"),
		TOTAL_AMOUNT("gr.total_amount");
		private final String column;
		GoodsReceiptSort(String column) { this.column = column; }
		public String column() { return column; } }

	// Helper for ComboBox
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

	private void loadSuppliersFilter() {
		List<Supplier> suppliers = supplierService.getAll();
		DefaultComboBoxModel<SupplierFilterItem> cboSupplierModel = new DefaultComboBoxModel<>();
		cboSupplierModel.addElement(new SupplierFilterItem(null, "Tất cả nhà cung cấp"));
		for (Supplier s : suppliers) {
			cboSupplierModel.addElement(new SupplierFilterItem(s.getSupplierId(), s.getSupplierName()));
		}
		cboSupplierFilter = new JComboBox<>(cboSupplierModel);
		cboSupplierFilter.setPreferredSize(new Dimension(240, 34));
		cboSupplierFilter.addActionListener(e -> onSupplierFilterChange());
	}
}
