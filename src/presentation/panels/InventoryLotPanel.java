package presentation.panels;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import dto.*;
import util.*;
import bus.*;
import bus.AuthService.AuthUser;
import presentation.components.datechooser.*;
import presentation.components.datechooser.listener.*;
import presentation.dialogs.InventoryLotDialog;

public class InventoryLotPanel extends JPanel {

	private final InventoryLotService lotService = new InventoryLotService();
	private final ProductService productService = new ProductService();

	private AuthUser currentUser;

	private JTable lotTable;
	private InventoryLotTableModel lotTableModel;

	private JComboBox<ProductFilterItem> productFilterCbo;
	private Product selectedProduct;
	private JComboBox<StatusFilterItem> statusFilter;

	private DateChooser fromDateFilterDc;
	private DateChooser toDateFilterDc;

	private JSpinner expiryWarnThresholdSp;


	public InventoryLotPanel(AuthUser currentUser, Product product) {
		this.currentUser = currentUser;
		this.selectedProduct = product;
		setOpaque(false);
		setLayout(new BorderLayout(12, 12));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		add(buildTop(), BorderLayout.NORTH);
		add(buildTable(), BorderLayout.CENTER);
	}

// Open the dialog for GR detail or create new GR
	private void openViewDetailDialog(InventoryLot lot) {
		Window owner = SwingUtilities.getWindowAncestor(this);
		InventoryLotDialog dialog = new InventoryLotDialog(owner, currentUser, lot);
		dialog.setVisible(true);
	}
// ==============================================
	private JComponent buildTop() {
	// West side of top panel: Filter label, product, status combo box and expiry range date chooser
		statusFilter = getStatusFilterCbo();
		productFilterCbo = getProductFilterCbo();
		if (selectedProduct != null) {
			productFilterCbo.setSelectedItem(
				new ProductFilterItem(selectedProduct.getProductId(), selectedProduct.getProductName())
			);
		}

		JTextField fromDateFilterTxt = new JTextField(6);
		fromDateFilterTxt.setCursor(new Cursor(Cursor.HAND_CURSOR));

		fromDateFilterDc = new DateChooser();
		fromDateFilterDc.setTextField(fromDateFilterTxt);
		fromDateFilterDc.setSelectedDate();
		fromDateFilterDc.addActionDateChooserListener(new DateChooserAdapter() {
			@Override public void dateChanged(Date date, DateChooserAction action) {
				lotTableModel.setFrom(date);
				lotTableModel.loadData();
			}
		});

		JTextField toDateFilterTxt = new JTextField(6);
		toDateFilterTxt.setCursor(new Cursor(Cursor.HAND_CURSOR));

		toDateFilterDc = new DateChooser();
		toDateFilterDc.setTextField(toDateFilterTxt);
		toDateFilterDc.setSelectedDate();
		toDateFilterDc.addActionDateChooserListener(new DateChooserAdapter() {
			@Override public void dateChanged(Date date, DateChooserAction action) {
				lotTableModel.setTo(date);
				lotTableModel.loadData();
			}
		});

		JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		west.setOpaque(false);
		west.add(new JLabel("Lọc theo:"));
		west.add(statusFilter);
		west.add(productFilterCbo);
		west.add(new JLabel("HSD từ"));
		west.add(fromDateFilterTxt);
		west.add(new JLabel("đến"));
		west.add(toDateFilterTxt);

	// ========================================================================

	// South side of top panel: expiry warning threshold, Refresh button
		expiryWarnThresholdSp = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));

		JButton refreshBtn = new JButton("Làm mới");
    refreshBtn.setBackground(new Color(33, 150, 243));
		refreshBtn.setForeground(Color.WHITE);
		refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		refreshBtn.setPreferredSize(new Dimension(140, 40));
		refreshBtn.addActionListener(e -> {
			statusFilter.setSelectedIndex(0);
			productFilterCbo.setSelectedIndex(0);
			fromDateFilterDc.setSelectedDate();
			lotTableModel.setFrom(null);
			toDateFilterDc.setSelectedDate();
			lotTableModel.setTo(null);
			lotTableModel.loadData();
		});

		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		south.setOpaque(false);
		south.add(new JLabel("Mức cảnh báo hết hạn (ngày) "));
		south.add(expiryWarnThresholdSp);
		south.add(refreshBtn);
	// =============================================================

	// Top panel

		JPanel top = new JPanel(new BorderLayout(10, 10));
		top.setOpaque(false);
		top.add(south, BorderLayout.SOUTH);
		top.add(west, BorderLayout.WEST);
	// =========
		return top;
	}

	private JComponent buildTable() {
	// Initialize the GR table
		if (selectedProduct != null) {
			lotTableModel = new InventoryLotTableModel(selectedProduct.getProductId());
		} else {
			lotTableModel = new InventoryLotTableModel(null);
		}
		lotTable = new JTable(lotTableModel);
	// =======================

	// Set max width for the index column
		lotTable.getColumnModel().getColumn(0).setMaxWidth(50);
	// ==================================

	// Hightlight expiry warning
		InventoryLotRenderer renderer = new InventoryLotRenderer();
    lotTable.setDefaultRenderer(Object.class, renderer);
		expiryWarnThresholdSp.addChangeListener(e -> lotTable.repaint());
	// =========================

	// Styling whole table
		lotTable.setRowHeight(40);
		lotTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// ===================

	// Open GR detail dialog on double click
		lotTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = lotTable.getSelectedRow();
					if (row != -1) {
						InventoryLot selected = lotTableModel.getLotAt(row);
						openViewDetailDialog(selected);
					}
				}
			}
		});
	// =====================================
	// Styling table's header
		lotTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
		lotTable.getTableHeader().setBackground(new Color(33, 150, 243));
		lotTable.getTableHeader().setForeground(Color.WHITE);
		lotTable.getTableHeader().setCursor(new Cursor(Cursor.HAND_CURSOR));
		lotTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
	// ======================

	// Toggle sorting order of the corresponding column on click
		lotTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int col = lotTable.columnAtPoint(e.getPoint());
				InventoryLotSort newSort = switch (col) {
					case 1 -> InventoryLotSort.PRODUCT;
					case 4 -> InventoryLotSort.EXPIRY;
					default -> null;
				};

				if (newSort == null) return;
				if (newSort == lotTableModel.sortBy) {
					lotTableModel.toggleAscending();
				} else {
					lotTableModel.sortBy = newSort;
					lotTableModel.isAscending = true;
				}
				lotTableModel.loadData();
			}
		});
	// =========================================================

	// The scrollable pane containing the IL table
		JScrollPane scrollPane = new JScrollPane(lotTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(228, 231, 236)));
		return scrollPane;
	// ===========================================
	}

// The table model for the IL table
	private class InventoryLotTableModel extends AbstractTableModel {
		private final String[] cols = { "STT", "Mã sản phẩm", "Mã lô", "Ngày nhập", "HSD", "Tồn kho", "Trạng thái" };
		public List<InventoryLot> lots;
		public Integer productId;
		public LocalDate from;
		public LocalDate to;
		public InventoryLot.Status status;
		public InventoryLotSort sortBy;
		public boolean isAscending;

		public InventoryLotTableModel(Integer selectedProductId) {
			productId = selectedProductId;
			from = null;
			to = null;
			sortBy = InventoryLotSort.PRODUCT;
			isAscending = true;
			loadData();
		}

		@Override public int getRowCount() { return lots.size(); }
		@Override public int getColumnCount() { return cols.length; }
		@Override public String getColumnName(int col) { return cols[col]; }
		@Override
		public Object getValueAt(int row, int col) {
			InventoryLot il = lots.get(row);
			return switch (col) {
        case 0 -> row + 1;
				case 1 -> il.getProductId();
				case 2 -> il.getLotCode();
				case 3 -> DateUtils.formatDate(il.getReceivedDate());
				case 4 -> DateUtils.formatDate(il.getExpiry());
				case 5 -> il.getQtyRemaining();
				case 6 -> il.getStatus();
				default -> null;
			};
		}

		public void loadData() {
			lots = lotService.getFilteredLots(productId, from, to, status, sortBy.column(), isAscending);
			fireTableDataChanged();
			revalidate();
		}

		public InventoryLot getLotAt(int row) { return lots.get(row); }

		public void toggleAscending() { isAscending = !isAscending; }
		public void setFrom(Date date) {
			if (date == null) { from = null; }
			else { from = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); }
		}
		public void setTo(Date date) {
			if (date == null) { to = null; }
			else { to = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); }
		}
	}
    // ================================

    // The table renderer for IL table
    private class InventoryLotRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row, int col
        ) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            // Set highlight color
            InventoryLot lot = lotTableModel.getLotAt(row);
            int threshold = (Integer) expiryWarnThresholdSp.getValue();

            InventoryLot.Status st = (lot != null) ? lot.getStatusEnum() : null;

            if (!isSelected) {
                if (lot != null && lot.getExpiry() != null && st == InventoryLot.Status.AVAILABLE) {
                    long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), lot.getExpiry());
                    if (daysUntilExpiry < 0) {
                        c.setBackground(new Color(255, 235, 238));
                    } else if (daysUntilExpiry <= threshold) {
                        c.setBackground(new Color(255, 248, 225));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            // ====================

            // Set badge color
            if (col == 6) {
                setHorizontalAlignment(JLabel.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));

                if (st == null) {
                    setForeground(Color.BLACK);
                } else {
                    switch (st) {
                        case AVAILABLE -> setForeground(new Color(46, 125, 50));
                        case EXPIRED -> setForeground(Color.RED);
                        case DEPLETED -> setForeground(Color.GRAY);
                    }
                }
            } else {
                setForeground(Color.BLACK);
            }
            // ===========

            return c;
        }
    }

// Use for column sorting
	public enum InventoryLotSort {
		PRODUCT("il.product_id"),
		EXPIRY("il.expiry_date");
		private final String column;
		InventoryLotSort(String column) { this.column = column; }
		public String column() { return column; }
	}
// ======================

// Status filter helper
	private class StatusFilterItem {
		private final InventoryLot.Status status;
		private final String label;
		StatusFilterItem(InventoryLot.Status status) {
			this.status = status;
			this.label = status.name();
		}
		StatusFilterItem(InventoryLot.Status status, String label) {
			this.status = status;
			this.label = label;
		}
		@Override public String toString() { return label; }
	}

	private JComboBox<StatusFilterItem> getStatusFilterCbo() {
		JComboBox<StatusFilterItem> cbo;
		DefaultComboBoxModel<StatusFilterItem> statusFilterCboModel = new DefaultComboBoxModel<>();
		statusFilterCboModel.addElement(new StatusFilterItem(null, "Tất cả trạng thái"));
		for (InventoryLot.Status s : InventoryLot.Status.values()) {
			statusFilterCboModel.addElement(new StatusFilterItem(s));
		}
		cbo = new JComboBox<>(statusFilterCboModel);
		cbo.setSize(50, 10);
		cbo.setPreferredSize(new Dimension(180, 34));
		cbo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		cbo.addActionListener(e -> {
			StatusFilterItem selectedStatusFilter = (StatusFilterItem) statusFilter.getSelectedItem();
			lotTableModel.status = selectedStatusFilter.status;
			lotTableModel.loadData();
		});
		return cbo;
	}
// =========================

// Product filter helper
	private class ProductFilterItem {
		public Integer id;
		public String name;
		public ProductFilterItem(Integer id, String name) { this.id = id; this.name = name; }
		@Override public String toString() { return name + ((id != null) ? " (" + id + ")" : ""); }
	}
	private JComboBox<ProductFilterItem> getProductFilterCbo() {
		JComboBox<ProductFilterItem> cbo;
		List<Product> products = productService.getAll();
		DefaultComboBoxModel<ProductFilterItem> productFilterCboModel = new DefaultComboBoxModel<>();
		productFilterCboModel.addElement(new ProductFilterItem(null, "Tất cả sản phẩm"));
		for (Product p : products) {
			productFilterCboModel.addElement(new ProductFilterItem(p.getProductId(), p.getProductName()));
		}
		cbo = new JComboBox<>(productFilterCboModel);
		cbo.setPreferredSize(new Dimension(180, 34));
		cbo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		cbo.addActionListener(e -> {
			ProductFilterItem selectedProductFilter = (ProductFilterItem) productFilterCbo.getSelectedItem();
			lotTableModel.productId = selectedProductFilter.id;
			lotTableModel.loadData();
		});
		return cbo;
	}
// =======================
}
