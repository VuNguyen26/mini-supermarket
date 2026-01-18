package dal.dao;

import dal.DBConnection;
import dto.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO - CRUD operations for category table
 * DB Schema: category_id, category_name (simple - no status/timestamps)
 */
public class CategoryDAO {

    /**
     * Get all categories ordered by name
     */
    public List<Category> findAll() {
        String sql = "SELECT category_id, category_name FROM category ORDER BY category_name ASC";
        List<Category> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("CategoryDAO.findAll error: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Search categories by name keyword
     */
    public List<Category> search(String keyword) {
        String sql = "SELECT category_id, category_name FROM category WHERE category_name LIKE ? ORDER BY category_name ASC";
        List<Category> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CategoryDAO.search error: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Find category by ID
     */
    public Category findById(int categoryId) {
        String sql = "SELECT category_id, category_name FROM category WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CategoryDAO.findById error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Find category by exact name (for checking duplicates)
     */
    public Category findByName(String categoryName) {
        String sql = "SELECT category_id, category_name FROM category WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoryName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CategoryDAO.findByName error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Insert new category
     */
    public int insert(Category category) {
        String sql = "INSERT INTO category(category_name) VALUES(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getCategoryName());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CategoryDAO.insert error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Update category
     */
    public boolean update(Category category) {
        String sql = "UPDATE category SET category_name=? WHERE category_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getCategoryName());
            ps.setInt(2, category.getCategoryId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("CategoryDAO.update error: " + e.getMessage(), e);
        }
    }

    /**
     * Delete category
     */
    public boolean delete(int categoryId) {
        String sql = "DELETE FROM category WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("CategoryDAO.delete error: " + e.getMessage(), e);
        }
    }

    /**
     * Map ResultSet to Category object
     */
    private Category mapResultSet(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("category_id"));
        c.setCategoryName(rs.getString("category_name"));
        return c;
    }
}
