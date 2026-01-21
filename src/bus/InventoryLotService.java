package bus;

import dto.InventoryLot;

import java.time.LocalDate;
import java.util.List;

import dal.DBConnection;
import dal.dao.InventoryLotDAO;

public class InventoryLotService {
	private final InventoryLotDAO lotDAO = new InventoryLotDAO();

	public List<InventoryLot> getFilteredLots(
		Integer productId,
		LocalDate from,
		LocalDate to,
		InventoryLot.Status status,
		String sortBy,
		boolean isAscending
	) {
		if (status != null) {
			return lotDAO.findFiltered(productId, from, to, status.name(), sortBy, isAscending);
		} else {
			return lotDAO.findFiltered(productId, from, to, null, sortBy, isAscending);
		}
	}

	public boolean markAsExpired(int lotId) {
		try (java.sql.Connection con = DBConnection.getConnection()) {
			return lotDAO.updateStatus(con, lotId, InventoryLot.Status.EXPIRED);
		} catch (Exception e) {
			return false;
		}
	}
}
