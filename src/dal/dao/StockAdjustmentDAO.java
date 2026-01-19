package dal.dao;

import dal.DBConnection;
import dto.StockAdjustment;
import dto.StockAdjustmentReason;
import dto.StockAdjustmentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StockAdjustmentDAO {

    // ====== load danh sách phiếu kiểm kho ======
    public List<StockAdjustment> findAll() {
        String sql =
                "SELECT sa_id, sa_code, created_by, created_at, reason, status, note " +
                "FROM stock_adjustment " +
                "ORDER BY created_at DESC";

        List<StockAdjustment> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Load stock adjustments failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ====== lấy 1 phiếu theo id ======
    public StockAdjustment findById(int saId) {
        String sql =
                "SELECT sa_id, sa_code, created_by, created_at, reason, status, note " +
                "FROM stock_adjustment WHERE sa_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, saId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Find stock adjustment failed: " + e.getMessage(), e);
        }
        return null;
    }

    // ====== tạo phiếu kiểm kho ======
    public int insert(StockAdjustment sa) {
        String sql =
                "INSERT INTO stock_adjustment " +
                "(sa_code, created_by, reason, status, note) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, sa.getSaCode());
            ps.setInt(2, sa.getCreatedBy());
            ps.setString(3, sa.getReason().name());
            ps.setString(4, "DRAFT");
            ps.setString(5, sa.getNote());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Insert stock adjustment failed: " + e.getMessage(), e);
        }
        return -1;
    }

    // ====== cập nhật trạng thái phiếu ======
    public void updateStatus(int saId, String status) {
        String sql = "UPDATE stock_adjustment SET status = ? WHERE sa_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, saId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Update stock adjustment status failed: " + e.getMessage(), e);
        }
    }

    public void updateDraftInfo(StockAdjustment sa) {
        String sql =
            "UPDATE stock_adjustment " +
            "SET sa_code = ?, reason = ?, note = ? " +
            "WHERE sa_id = ?";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sa.getSaCode());
            ps.setString(2, sa.getReason().name());
            ps.setString(3, sa.getNote());
            ps.setInt(4, sa.getSaId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(
                "Update stock adjustment info failed: " + e.getMessage(), e
            );
        }
    }


    // ====== map ResultSet -> DTO ======
    private StockAdjustment map(ResultSet rs) throws Exception {
        StockAdjustment sa = new StockAdjustment();
        sa.setSaId(rs.getInt("sa_id"));
        sa.setSaCode(rs.getString("sa_code"));
        sa.setCreatedBy(rs.getInt("created_by"));
        sa.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        sa.setReason(StockAdjustmentReason.valueOf(rs.getString("reason")));
        sa.setStatus(StockAdjustmentStatus.valueOf(rs.getString("status")));
        sa.setNote(rs.getString("note"));
        return sa;
    }
}
