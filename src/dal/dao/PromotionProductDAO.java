package dal.dao;

import dal.DBConnection;
import dto.PromotionProduct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

public class PromotionProductDAO {

    public PromotionProduct findById(int ppId){
        String sql =
                "SELECT pp_id, promo_id, pp.product_id, p.product_name " +
                "FROM promotion_product pp JOIN product p ON pp.product_id = p.product_id WHERE pp_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ppId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Find promotion_product failed: " + e.getMessage(), e);
        }
        return null;
    }

    public List<PromotionProduct> findProductByPromotionId(int promoId){
        String sql =
                "SELECT pp_id, promo_id, pp.product_id, p.product_name " +
                "FROM promotion_product pp JOIN product p ON pp.product_id = p.product_id " +
                "WHERE pp.promo_id = ?";

        List<PromotionProduct> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, promoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Load promotion_product failed: " + e.getMessage(), e);
        }
        return list;
    }

    public int insert(PromotionProduct pp){
        String sql = "INSERT INTO promotion_product (promo_id, product_id) VALUE (?, ?)";

            try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pp.getPromoId());
            ps.setInt(2, pp.getProductId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Insert promotion_product failed: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean update(PromotionProduct pp) {
        String sql =
            "UPDATE promotion_product SET " +
            "promo_id = ?, " +
            "product_id = ? " +
            "WHERE pp_id = ?";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pp.getPromoId());
            ps.setInt(2, pp.getProductId());
            ps.setInt(3, pp.getPpId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Update promotion_product failed" + e.getMessage(), e);
        }
    }

    public void delete(int ppId) {
        String sql = "DELETE FROM promotion_product WHERE pp_id = ?";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ppId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Delete pp failed: " + e.getMessage(), e);
        }

    }

    private PromotionProduct map(ResultSet rs) throws Exception {
        PromotionProduct pp = new PromotionProduct();

        pp.setPpId(rs.getInt("pp_id"));
        pp.setPromoId(rs.getInt("promo_id"));
        pp.setProductId(rs.getInt("product_id"));
        pp.setProductName(rs.getString("product_name"));

        return pp;
    }

}