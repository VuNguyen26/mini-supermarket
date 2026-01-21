package dal.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dal.DBConnection;
import dto.InventoryLot;

public class InventoryLotDAO {

/**
 * Retrieves all inventory lot with optional filters and sorting.
 *
 * @param productId   the product ID to filter by, or null for all products
 * @param from        filter lots which expired at or after this time, or null
 * @param to					filter lots which expired before this time, or null
 * @param status			filter lots which has this status, or null
 * @param sortBy      the database column used for sorting
 * @param isAscending true for ascending order, false for descending
 * @return a filtered list of inventory lot
 */
	public List<InventoryLot> findFiltered(
		Integer productId,
		LocalDate from,
		LocalDate to,
		String status,
		String sortBy,
		boolean isAscending
	) {
		StringBuilder sql = new StringBuilder(
			"SELECT il.* " +
			"FROM inventory_lot il " +
			"JOIN product p ON il.product_id = p.product_id " +
			"WHERE 1 = 1 "
		);

		if (productId != null) {
			sql.append("AND il.product_id = ? ");
		}

		if (from != null) {
			sql.append("AND il.expiry_date >= ? ");
		}

		if (to != null) {
			sql.append("AND il.expiry_date < ? ");
		}

		if (status != null) {
			sql.append("AND il.status = ? ");
		}

		sql.append("ORDER BY ")
			.append(sortBy)
			.append(isAscending ? " ASC " : " DESC ");

		List<InventoryLot> list = new ArrayList<>();


		try ( Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql.toString())
		) {
			int paramIndex = 1;
			if (productId != null) { ps.setInt(paramIndex++, productId); }
			if (from != null) { ps.setObject(paramIndex++, from); }
			if (to != null) { ps.setObject(paramIndex++, to); }
			if (status != null) { ps.setString(paramIndex++, status); }

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(map(rs));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to retrieve inventory lot list: " + e.getMessage(), e);
		}

		return list;
	}

	public boolean updateStatus(Connection con, int lotId, InventoryLot.Status status) {
		String sql = "UPDATE inventory_lot SET status = ? WHERE lot_id = ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, status.name());
			ps.setInt(2, lotId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update status of the inventory lot: " + e.getMessage(), e);
		}
	}

	/**
 * Inserts a new inventory_lot into the database
 *
 * @param con the Connection to use
 * @param lot the InventoryLot to be saved
 * @return true if the insert was successful, false otherwise
 */
	public boolean insert(Connection con, InventoryLot lot) {
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
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to insert new inventory lot: " + e.getMessage(), e);
		}
	}
	private InventoryLot map(ResultSet rs) throws SQLException {
		InventoryLot lot = new InventoryLot();
		lot.setLotId(rs.getInt("lot_id"));
		lot.setProductId(rs.getInt("product_id"));
		lot.setGrdId(rs.getInt("grd_id"));
		lot.setLotCode(rs.getString("lot_code"));
		lot.setReceivedDate(rs.getObject("received_date", LocalDate.class));
		lot.setExpiry(rs.getObject("expiry_date", LocalDate.class));
		lot.setQtyIn(rs.getInt("qty_in"));
		lot.setQtyOut(rs.getInt("qty_out"));
		lot.setQtyRemaining(rs.getInt("qty_remaining"));
		lot.setOutOfStockAt(rs.getObject("out_of_stock_at", LocalDateTime.class));
		lot.setStatus(InventoryLot.Status.valueOf(rs.getString("status")));
		return lot;
	}
}
