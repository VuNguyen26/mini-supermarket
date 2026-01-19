package dal.dao;

import dal.DBConnection;
import dto.StockAdjustmentDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StockAdjustmentDetailDAO {

    // ====== load danh sách chi tiết theo phiếu ======
    public List<StockAdjustmentDetail> findByAdjustmentId(int saId) {
        String sql =
                "SELECT sad_id, sa_id, product_id, lot_id, " +
                "system_qty, counted_qty, diff_qty, note " +
                "FROM stock_adjustment_detail " +
                "WHERE sa_id = ?";

        List<StockAdjustmentDetail> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, saId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Load stock adjustment details failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ====== thêm 1 dòng kiểm kho ======
    public void insert(StockAdjustmentDetail d) {
        String sql =
                "INSERT INTO stock_adjustment_detail " +
                "(sa_id, product_id, lot_id, system_qty, counted_qty, diff_qty, note) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getSaId());
            ps.setInt(2, d.getProductId());
            ps.setObject(3, d.getLotId());
            ps.setInt(4, d.getSystemQty());
            ps.setInt(5, d.getCountedQty());
            ps.setInt(6, d.getDiffQty());
            ps.setString(7, d.getNote());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Insert stock adjustment detail failed: " + e.getMessage(), e);
        }
    }

    // ====== cập nhật 1 dòng kiểm kho ======
    public void update(StockAdjustmentDetail d) {
        String sql =
                "UPDATE stock_adjustment_detail " +
                "SET counted_qty = ?, diff_qty = ?, note = ? " +
                "WHERE sad_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getCountedQty());
            ps.setInt(2, d.getDiffQty());
            ps.setString(3, d.getNote());
            ps.setInt(4, d.getSadId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Update stock adjustment detail failed: " + e.getMessage(), e);
        }
    }

    // ====== xóa 1 dòng kiểm kho ======
    public void delete(int sadId) {
        String sql = "DELETE FROM stock_adjustment_detail WHERE sad_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sadId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Delete stock adjustment detail failed: " + e.getMessage(), e);
        }
    }

    // ====== map ResultSet -> DTO ======
    private StockAdjustmentDetail map(ResultSet rs) throws Exception {
        StockAdjustmentDetail d = new StockAdjustmentDetail();
        d.setSadId(rs.getInt("sad_id"));
        d.setSaId(rs.getInt("sa_id"));
        d.setProductId(rs.getInt("product_id"));
        d.setLotId(rs.getObject("lot_id", Long.class));
        d.setSystemQty(rs.getInt("system_qty"));
        d.setCountedQty(rs.getInt("counted_qty"));
        d.setDiffQty(rs.getInt("diff_qty"));
        d.setNote(rs.getString("note"));
        return d;
    }
}
