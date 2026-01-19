package dal.dao;

import dal.DBConnection;
import dto.Brand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {

    public List<Brand> findAll() {
        String sql = "SELECT * FROM brand ORDER BY brand_name ASC";
        List<Brand> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("BrandDAO.findAll error: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Brand> search(String keyword) {
        String sql = "SELECT * FROM brand WHERE " +
                     "(brand_id LIKE ? OR brand_name LIKE ?) " +
                     "AND status = 'ACTIVE' " +
                     "ORDER BY brand_name ASC";
        List<Brand> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("BrandDAO.search error: " + e.getMessage(), e);
        }
        return list;
    }

    public Brand findById(int brandId) {
        String sql = "SELECT * FROM brand WHERE brand_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, brandId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("BrandDAO.findById error: " + e.getMessage(), e);
        }
        return null;
    }

    // public Brand findByCode(String brandCode) {
    //     String sql = "SELECT * FROM brand WHERE brand_code = ?";

    //     try (Connection conn = DBConnection.getConnection();
    //          PreparedStatement ps = conn.prepareStatement(sql)) {

    //         ps.setString(1, brandCode);

    //         try (ResultSet rs = ps.executeQuery()) {
    //             if (rs.next()) {
    //                 return mapResultSet(rs);
    //             }
    //         }
    //     } catch (Exception e) {
    //         throw new RuntimeException("BrandDAO.findByCode error: " + e.getMessage(), e);
    //     }
    //     return null;
    // }

    public Brand findByName(String brandName) {
        String sql = "SELECT * FROM brand WHERE brand_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, brandName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("BrandDAO.findByName error: " + e.getMessage(), e);
        }
        return null;
    }

    public int insert(Brand brand) {
        // Compatible with current DB (brand_code/description may not exist)
        String sql = "INSERT INTO brand(brand_name, status) VALUES(?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, brand.getBrandName());
            ps.setString(2, brand.getStatus() != null ? brand.getStatus() : "ACTIVE");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("BrandDAO.insert error: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean update(Brand brand) {
        String sql = "UPDATE brand SET brand_name=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE brand_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, brand.getBrandName());
            ps.setString(2, brand.getStatus());
            ps.setInt(3, brand.getBrandId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("BrandDAO.update error: " + e.getMessage(), e);
        }
    }

    public boolean delete(int brandId) {
        String sql = "DELETE FROM brand WHERE brand_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, brandId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("BrandDAO.delete error: " + e.getMessage(), e);
        }
    }

    private Brand mapResultSet(ResultSet rs) throws SQLException {
        Brand b = new Brand();
        b.setBrandId(rs.getInt("brand_id"));
        if (hasColumn(rs, "brand_code")) {
            b.setBrandCode(rs.getString("brand_code"));
        }
        b.setBrandName(rs.getString("brand_name"));
        if (hasColumn(rs, "description")) {
            b.setDescription(rs.getString("description"));
        }
        b.setStatus(rs.getString("status"));
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) b.setCreatedAt(created.toLocalDateTime());
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) b.setUpdatedAt(updated.toLocalDateTime());
        
        return b;
    }

    private boolean hasColumn(ResultSet rs, String column) {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            for (int i = 1; i <= count; i++) {
                if (column.equalsIgnoreCase(meta.getColumnLabel(i))) {
                    return true;
                }
            }
        } catch (SQLException ignored) {
            // ignore
        }
        return false;
    }
}
