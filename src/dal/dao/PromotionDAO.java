package dal.dao;

import dal.DBConnection;
import dto.Promotion;
import dto.PromotionType;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO {

    public List<Promotion> findAll() {
        String sql =
                "SELECT promo_id, promo_code, promo_name, start_at, end_at, type, value, min_order_amount, created_by, status, created_at " +
                "FROM promotion " +
                "ORDER BY created_at DESC";

        List<Promotion> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Load promotion failed: " + e.getMessage(), e);
        }
        return list;
    }

    public Promotion findById(int promoId) {
        String sql =
                "SELECT promo_id, promo_code, promo_name, start_at, end_at, type, value, min_order_amount, created_by, status, created_at " +
                "FROM promotion WHERE promo_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, promoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Find promotion failed: " + e.getMessage(), e);
        }
        return null;
    }

    public int insert(Promotion promo) {
        String sql =
                "INSERT INTO promotion " +
                "(`promo_code`, `promo_name`, `start_at`, `end_at`, `type`, `value`, `min_order_amount`, `status`, `created_by`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, promo.getPromoCode());
            ps.setString(2, promo.getPromoName());
            ps.setTimestamp(3, Timestamp.valueOf(promo.getStartAt()));
            ps.setTimestamp(4, Timestamp.valueOf(promo.getEndAt()));
            ps.setString(5, promo.getType().name());
            ps.setBigDecimal(6, promo.getValue());
            ps.setBigDecimal(7, promo.getMinOrderAmount());
            ps.setString(8, promo.getStatus());
            ps.setInt(9, promo.getCreatedBy());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Insert promotion failed: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean update(Promotion promo) {
        String sql =
            "UPDATE promotion SET " +
            "promo_code = ?, " +
            "promo_name = ?, " +
            "start_at = ?, " +
            "end_at = ?, " +
            "type = ?, " +
            "value = ?, " +
            "min_order_amount = ?, " +
            "status = ? " +
            "WHERE promo_id = ?";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, promo.getPromoCode());
            ps.setString(2, promo.getPromoName());
            ps.setTimestamp(3, Timestamp.valueOf(promo.getStartAt()));
            ps.setTimestamp(4, Timestamp.valueOf(promo.getEndAt()));
            ps.setString(5, promo.getType().name());
            ps.setBigDecimal(6, promo.getValue());
            ps.setBigDecimal(7, promo.getMinOrderAmount());
            ps.setString(8, promo.getStatus());
            ps.setInt(9, promo.getPromoId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Update promotion failed", e);
        }
    }

    public void delete(int promoId) {
        String sql = "DELETE FROM promotion WHERE promo_id = ?";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, promoId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Delete promotion failed: " + e.getMessage(), e);
        }

    }

    private Promotion map(ResultSet rs) throws Exception {
        Promotion p = new Promotion();

        p.setPromoId(rs.getInt("promo_id"));
        p.setPromoCode(rs.getString("promo_code"));
        p.setPromoName(rs.getString("promo_name"));

        Timestamp start = rs.getTimestamp("start_at");
        p.setStartAt(start != null ? start.toLocalDateTime() : null);

        Timestamp end = rs.getTimestamp("end_at");
        p.setEndAt(end != null ? end.toLocalDateTime() : null);

        String type = rs.getString("type");
        p.setType(type != null ? PromotionType.valueOf(type) : null);

        p.setValue(rs.getBigDecimal("value"));
        p.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        p.setStatus(rs.getString("status"));

        p.setCreatedBy(rs.getInt("created_by"));

        Timestamp created = rs.getTimestamp("created_at");
        p.setCreatedAt(created != null ? created.toLocalDateTime() : null);

        return p;
    }

}