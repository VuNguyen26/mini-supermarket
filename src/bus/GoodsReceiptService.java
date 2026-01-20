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

	private final GoodsReceiptDAO grDAO = new GoodsReceiptDAO();
	private final GoodsReceiptDetailDAO grdDAO = new GoodsReceiptDetailDAO();
	private final InventoryLotDAO lotDAO = new InventoryLotDAO();

	public String getUserNameById(int id) { return grDAO.getUserFullName(id) + " ("+ id + ")"; }

	public List<GoodsReceipt> getReceiptsList(
		Integer supplierId,
		LocalDateTime from,
		LocalDateTime to,
		String sortBy,
		boolean isAscending
	) {
		return grDAO.findAll(supplierId, from, to, sortBy, isAscending);
	}

	public GoodsReceiptDetail getDetailById(int grdId) {
		return grdDAO.findById(grdId);
	}
	public List<GoodsReceiptDetail> getDetailsByReceiptId(int grId) {
		return grdDAO.findByGrId(grId);
	}

	// DATABASE TRANSACTION
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

				lotDAO.insert(con, lot);
			}

			con.commit();
			return true;
		} catch (Exception e) {
			if (con != null) {
				try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
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
