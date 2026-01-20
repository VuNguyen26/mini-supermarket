package bus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import dal.DBConnection;
import dal.dao.GoodsReceiptDAO;
import dal.dao.GoodsReceiptDetailDAO;
import dal.dao.InventoryLotDAO;
import dto.GoodsReceipt;
import dto.GoodsReceiptDetail;
import dto.InventoryLot;

public class GoodsReceiptService {

	private final InventoryLotDAO lotDAO = new InventoryLotDAO();
	private final GoodsReceiptDAO grDAO = new GoodsReceiptDAO();
	private final GoodsReceiptDetailDAO grdDAO = new GoodsReceiptDetailDAO();

	public List<GoodsReceipt> getFilteredReceiptsList(
		Integer supplierId,
		LocalDateTime from,
		LocalDateTime to,
		String sortBy,
		boolean isAscending
	) {
		return grDAO.findFiltered(supplierId, from, to, sortBy, isAscending);
	}

	public String getUserNameById(int id) { return grDAO.getUserFullName(id) + " ("+ id + ")"; }
	public GoodsReceiptDetail getDetailById(int grdId) { return grdDAO.findById(grdId); }
	public List<GoodsReceiptDetail> getDetailsByReceiptId(int grId) { return grdDAO.findByGrId(grId); }

/**
 * Creates a complete goods receipt transaction, including:
 * - Inserting the goods receipt header
 * - Inserting all associated goods receipt details
 * - Creating inventory lots for each receipt detail
 * If any step fails, the entire transaction is rolled back to ensure data consistency
 *
 * @param gr the goods receipt header to be created
 * @param details the list of goods receipt details associated with the receipt
 * @return true if the transaction completes successfully, false if otherwise
 */
	public boolean createFullReceipt(GoodsReceipt gr, List<GoodsReceiptDetail> details) {
		Connection con = null;
		try {
			con = DBConnection.getConnection();
			con.setAutoCommit(false);

			int grId = grDAO.insert(con, gr);
			if (grId == -1) throw new SQLException("Failed to save Receipt Header");

			for (GoodsReceiptDetail detail : details) {
				detail.setGrId(grId);
				detail.setReceivedAt(gr.getCreatedAt());

				int grdId = grdDAO.insert(con, detail);
				if (grdId == -1) throw new SQLException("Failed to save Detail for Product " + detail.getProductId());

				InventoryLot lot = new InventoryLot();
				lot.setProductId(detail.getProductId());
				lot.setGrdId(grdId);
				lot.setLotCode(detail.getLotCode());
				lot.setReceivedDate(detail.getReceivedAt().toLocalDate());
				lot.setExpiry(detail.getExpiry());
				lot.setQtyIn(detail.getQty());
				lot.setQtyOut(0);
				lot.setQtyRemaining(detail.getQty());
				lot.setStatus(InventoryLot.Status.AVAILABLE);
				lot.setCreatedAt(LocalDateTime.now());

				boolean success = lotDAO.insert(con, lot);
				if (!success) {
					throw new SQLException("Failed to save inventory lot for grdId: " + grdId);
				}
			}
			con.commit();
			return true;
		} catch (Exception e) {
			if (con != null) {
				try { con.rollback(); } catch (SQLException ex) { throw new RuntimeException("Failed to rollback"); }
			}
			e.printStackTrace();
			return false;
		} finally {
			if (con != null) {
				try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
		}
	}
}
