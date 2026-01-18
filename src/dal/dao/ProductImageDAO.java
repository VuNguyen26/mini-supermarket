package dal.dao;

import dal.DBConnection;
import dto.ProductImage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductImageDAO {

    public List<ProductImage> findByProductId(int productId) {
        String sql = "SELECT * FROM product_image WHERE product_id = ? ORDER BY is_primary DESC, created_at ASC";
        List<ProductImage> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("ProductImageDAO.findByProductId error: " + e.getMessage(), e);
        }
        return list;
    }

    public ProductImage findPrimaryImage(int productId) {
        String sql = "SELECT * FROM product_image WHERE product_id = ? AND is_primary = 1 LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("ProductImageDAO.findPrimaryImage error: " + e.getMessage(), e);
        }
        return null;
    }

    public int insert(ProductImage image) {
        String sql = "INSERT INTO product_image(product_id, image_path, is_primary) VALUES(?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, image.getProductId());
            ps.setString(2, image.getImagePath());
            ps.setBoolean(3, image.getIsPrimary() != null ? image.getIsPrimary() : false);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("ProductImageDAO.insert error: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean setPrimaryImage(int productId, int imageId) {
        String sql1 = "UPDATE product_image SET is_primary = 0 WHERE product_id = ?";
        String sql2 = "UPDATE product_image SET is_primary = 1 WHERE image_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setInt(1, productId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setInt(1, imageId);
                ps2.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("ProductImageDAO.setPrimaryImage error: " + e.getMessage(), e);
        }
    }

    public boolean delete(int imageId) {
        String sql = "DELETE FROM product_image WHERE image_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, imageId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("ProductImageDAO.delete error: " + e.getMessage(), e);
        }
    }

    public boolean deleteByProductId(int productId) {
        String sql = "DELETE FROM product_image WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("ProductImageDAO.deleteByProductId error: " + e.getMessage(), e);
        }
    }

    private ProductImage mapResultSet(ResultSet rs) throws SQLException {
        ProductImage img = new ProductImage();
        img.setImageId(rs.getInt("image_id"));
        img.setProductId(rs.getInt("product_id"));
        img.setImagePath(rs.getString("image_path"));
        img.setIsPrimary(rs.getBoolean("is_primary"));
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) img.setCreatedAt(created.toLocalDateTime());
        
        return img;
    }
}
