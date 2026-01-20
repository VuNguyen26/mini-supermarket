package dal.dao;

import dal.DBConnection;
import dto.PromotionProduct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PromotionProductDAO {

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

    private PromotionProduct map(ResultSet rs) throws Exception {
        PromotionProduct pp = new PromotionProduct();

        pp.setPpId(rs.getInt("pp_id"));
        pp.setPromoId(rs.getInt("promo_id"));
        pp.setProductId(rs.getInt("product_id"));
        pp.setProductName(rs.getString("product_name"));

        return pp;
    }

}