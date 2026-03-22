package dal.dao;

import dal.DBConnection;
import dto.Promotion;
import dto.PromotionType;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO {

    public List<Promotion> findAll(String keyword) {
        String sql =
                "SELECT promo_id, promo_code, promo_name, start_at, end_at, type, value, min_order_amount, created_by, status, created_at " +
                "FROM promotion ";

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if(hasKeyword){
            sql += "WHERE promo_code LIKE ? OR promo_name LIKE ? ";
        }

        sql += "ORDER BY created_at DESC";

        List<Promotion> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            if (hasKeyword) {
                ps.setString(1, "%" + keyword.trim() + "%");
                ps.setString(2, "%" + keyword.trim() + "%");
            }

            ResultSet rs = ps.executeQuery();
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

    public boolean insertList(List<Promotion> promotions) {
        String sql =
                "INSERT INTO promotion " +
                "(`promo_code`, `promo_name`, `start_at`, `end_at`, `type`, `value`, `min_order_amount`, `status`, `created_by`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                int batchSize = 100;
                int count = 0;

                for (Promotion km : promotions) {
                    ps.setString(1, km.getPromoCode());
                    ps.setString(2, km.getPromoName());
                    ps.setTimestamp(3, Timestamp.valueOf(km.getStartAt()));
                    ps.setTimestamp(4, Timestamp.valueOf(km.getEndAt()));
                    ps.setString(5, km.getType().name());
                    ps.setBigDecimal(6, km.getValue());
                    ps.setBigDecimal(7, km.getMinOrderAmount());
                    ps.setString(8, km.getStatus());
                    ps.setInt(9, km.getCreatedBy());

                    ps.addBatch();

                    if (++count % batchSize == 0) {
                        ps.executeBatch();
                    }
                }

                ps.executeBatch(); // phần còn lại
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback(); // 💥 tránh insert dở
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
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