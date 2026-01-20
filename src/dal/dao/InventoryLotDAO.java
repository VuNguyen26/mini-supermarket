package dal.dao;

import dto.InventoryLot;
import java.sql.*;

public class InventoryLotDAO {

	/**
 * Inserts a new inventory_lot into the database
 *
 * @param con the Connection to use
 * @param lot the InventoryLot to be saved
 * @return {@code true} if the insert was successful, {@code false} otherwise
 */
	public boolean insert(Connection con, InventoryLot lot) throws SQLException {
		String sql = "INSERT INTO inventory_lot (product_id, grd_id, lot_code, received_date, expiry_date, qty_in, qty_out, qty_remaining, out_of_stock_at, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, lot.getProductId());
			ps.setInt(2, lot.getGrdId());
			ps.setString(3, lot.getLotCode());
			ps.setObject(4, lot.getReceivedDate());
			ps.setObject(5, lot.getExpiry());
			ps.setInt(6, lot.getQtyIn());
			ps.setInt(7, lot.getQtyOut());
			ps.setInt(8, lot.getQtyRemaining());
			ps.setObject(9, lot.getOutOfStockAt());
			ps.setString(10, lot.getStatus().name());
			ps.setObject(11, lot.getCreatedAt());
			return ps.executeUpdate() > 1;
		}
	}
}
