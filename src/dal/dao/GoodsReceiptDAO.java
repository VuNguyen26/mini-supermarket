package dal.dao;

import dal.DBConnection;
import dto.GoodsReceipt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class GoodsReceiptDAO {

	private String querySingleString(String sql, Integer id) {
		if (id == null) return null;
		try (
		Connection con = DBConnection.getConnection();
		PreparedStatement ps = con.prepareStatement(sql)
	) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getString(1) : null;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public String getUserFullName(Integer id) {
		return querySingleString("SELECT full_name FROM user WHERE user_id = ?", id);
	}

	/**
 * Retrieves goods receipts with optional filters and sorting.
 *
 * @param supplierId   the supplier ID to filter by, or {@code null} for all suppliers
 * @param after        include receipts created at or after this time, or {@code null}
 * @param before       include receipts created before this time, or {@code null}
 * @param sortBy       the database column used for sorting
 * @param isAscending  {@code true} for ascending order, {@code false} for descending
 * @return a list of goods receipts matching the given criteria
 */
	public List<GoodsReceipt> findAll(
		Integer supplierId,
		LocalDateTime after,
		LocalDateTime before,
		String sortBy,
		boolean isAscending
	) {
		StringBuilder sql = new StringBuilder(
			"SELECT gr.*, s.supplier_name " +
			"FROM goods_receipt gr " +
			"JOIN supplier s ON gr.supplier_id = s.supplier_id " +
			"WHERE 1 = 1 "
		);

		if (supplierId != null) {
			sql.append("AND s.supplier_id = ? ");
		}

		if (after != null) {
			sql.append("AND gr.created_at >= ? ");
		}

		if (before != null) {
			sql.append("AND gr.created_at < ? ");
		}

		sql.append("ORDER BY ")
			.append(sortBy)
			.append(isAscending ? " ASC " : " DESC ");

		List<GoodsReceipt> list = new LinkedList<>();


		try (
		Connection con = DBConnection.getConnection();
		PreparedStatement ps = con.prepareStatement(sql.toString())
	){
			int paramIndex = 1;
			if (supplierId != null) { ps.setInt(paramIndex++, supplierId); }
			if (after != null) { ps.setObject(paramIndex++, after); }
			if (before != null) { ps.setObject(paramIndex++, before); }

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					GoodsReceipt gr = new GoodsReceipt();
					gr.setGrId(rs.getInt("gr_id"));
					gr.setSupplierId(rs.getInt("supplier_id"));
					gr.setCreatedBy(rs.getInt("created_by"));
					gr.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
					gr.setNote(rs.getString("note"));
					gr.setTotalAmount(rs.getBigDecimal("total_amount"));

					list.add(gr);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to retrieve goods receipts list", e);
		}

		return list;
	}

	/**
 * Inserts a new goods_receipt into the database and retrieves the generated ID.
 *
 * @param con the Connection to use
 * @param gr the GoodsReceipt to be saved
 * @return the generated gr_id if successful, or -1 if the insertion failed
 * @throws SQLException if a database access error occurs
 */
	public int insert(Connection con, GoodsReceipt gr) throws SQLException {
		String sql = "INSERT INTO goods_receipt (supplier_id, created_by, created_at, note, total_amount) VALUES (?, ?, ?, ?, ?)";

		try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, gr.getSupplierId());
			ps.setInt(2, gr.getCreatedBy());
			ps.setObject(3, gr.getCreatedAt());
			ps.setString(4, gr.getNote());
			ps.setBigDecimal(5, gr.getTotalAmount());
			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			return rs.next() ? rs.getInt(1) : -1;
		}
	}
}
