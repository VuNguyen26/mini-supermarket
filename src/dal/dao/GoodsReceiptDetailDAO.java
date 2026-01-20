package dal.dao;

import dal.DBConnection;
import dto.GoodsReceiptDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GoodsReceiptDetailDAO {

	/**
	* Retrieves the goods receipt detail by its ID
	*
	* @param grdId the ID of the goods receipt detail
	* @return the corresponding GoodsReceiptDetail, or null if not found;
	*/
	public GoodsReceiptDetail findById(Integer grdId) {
		String sql = "SELECT * " + "FROM goods_receipt_detail grd WHERE grd.grd_id = ?";
		try (
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql.toString())
		){
			ps.setInt(1, grdId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					GoodsReceiptDetail grd = new GoodsReceiptDetail();
					grd.setGrdId(rs.getInt("grd_id"));
					grd.setGrId(rs.getInt("gr_id"));
					grd.setProductId(rs.getInt("product_id"));
					grd.setQty(rs.getInt("qty"));
					grd.setUnitPrice(rs.getBigDecimal("unit_price"));
					grd.setLineTotal(rs.getBigDecimal("line_total"));
					grd.setLotCode(rs.getString("lot_code"));
					grd.setMfgDate(rs.getObject("mfg_date", LocalDate.class));
					grd.setExpiry(rs.getObject("expiry_date", LocalDate.class));
					grd.setReceivedAt(rs.getObject("received_at", LocalDateTime.class));
					return grd;
				}
			}
		} catch (Exception e) {
				e.printStackTrace();
        throw new RuntimeException("Failed to retrieve goods receipt detail", e);
		}
		return null;
	}

	/**
	* Retrieves the a list of goods receipt details of the same grId
	*
	* @param grId the ID of the goods receipt
	* @return a list of GoodsReceiptDetail that matches the requirement
	*/
	public List<GoodsReceiptDetail> findByGrId(int grId) {
		String sql = "SELECT * " + "FROM goods_receipt_detail grd WHERE grd.gr_id = ?";
		List<GoodsReceiptDetail> grds = new ArrayList<>();
		try (
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql.toString())
		){
			ps.setInt(1, grId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					GoodsReceiptDetail grd = new GoodsReceiptDetail();
					grd.setGrdId(rs.getInt("grd_id"));
					grd.setGrId(rs.getInt("gr_id"));
					grd.setProductId(rs.getInt("product_id"));
					grd.setQty(rs.getInt("qty"));
					grd.setUnitPrice(rs.getBigDecimal("unit_price"));
					grd.setLineTotal(rs.getBigDecimal("line_total"));
					grd.setLotCode(rs.getString("lot_code"));
					grd.setMfgDate(rs.getObject("mfg_date", LocalDate.class));
					grd.setExpiry(rs.getObject("expiry_date", LocalDate.class));
					grd.setReceivedAt(rs.getObject("received_at", LocalDateTime.class));
					grds.add(grd);
				}
			}
		} catch (Exception e) {
				e.printStackTrace();
        throw new RuntimeException("Failed to retrieve goods receipt detail", e);
		}
		return grds;
	}

	/**
 * Inserts a new goods_receipt_detail into the database and retrieves the generated ID.
 *
 * @param con the Connection to use
 * @param grd the GoodsReceiptDetail to be saved
 * @return the generated grd_id if successful, or -1 if the insertion failed
 * @throws SQLException if a database access error occurs
 */
	public int insert(Connection con, GoodsReceiptDetail grd) throws SQLException {
		String sql =
		"INSERT INTO goods_receipt_detail (" +
		"gr_id, product_id, qty, unit_price, line_total, " +
		"lot_code, mfg_date, expiry_date, received_at" +
		") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try ( PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, grd.getGrId());
			ps.setInt(2, grd.getProductId());
			ps.setInt(3, grd.getQty());
			ps.setBigDecimal(4, grd.getUnitPrice());
			ps.setBigDecimal(5, grd.getLineTotal());
			ps.setString(6, grd.getLotCode());
			ps.setObject(7, grd.getMfgDate());
			ps.setObject(8, grd.getExpiry());
			ps.setObject(9, grd.getReceivedAt());
			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			return rs.next() ? rs.getInt(1) : -1;
		}
	}
}


