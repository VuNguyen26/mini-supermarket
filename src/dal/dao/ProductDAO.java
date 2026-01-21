package dal.dao;

import dto.Product;
import dal.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.barcode, p.sale_price, p.unit, "
                + "COALESCE(SUM(l.qty_remaining), 0) AS stock_qty "
                + "FROM product p "
                + "LEFT JOIN inventory_lot l "
                + "  ON p.product_id = l.product_id "
                + " AND l.status = 'AVAILABLE' "
                + " AND l.qty_remaining > 0 "
                + "WHERE p.status = 'ACTIVE' "
                + "GROUP BY p.product_id, p.product_name, p.barcode, p.sale_price, p.unit";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setBarcode(rs.getString("barcode"));
                p.setSalePrice(rs.getDouble("sale_price"));
                p.setStockQty(rs.getInt("stock_qty"));
                p.setUnit(rs.getString("unit"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}