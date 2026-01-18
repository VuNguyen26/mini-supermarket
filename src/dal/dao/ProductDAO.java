package dal.dao;

import dal.DBConnection;
import dto.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public List<Product> findAll() {
        String sql = "SELECT p.product_id, p.barcode, p.product_name, p.unit, p.import_price, p.sale_price, p.stock_qty, p.min_stock, p.category_id, p.brand_id, p.status, p.created_at, p.updated_at, c.category_name, b.brand_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id LEFT JOIN brand b ON p.brand_id = b.brand_id ORDER BY p.product_name ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (Exception e) { throw new RuntimeException("ProductDAO.findAll error: " + e.getMessage(), e); }
        return list;
    }
    
    public List<Product> search(String keyword) {
        String sql = "SELECT p.product_id, p.barcode, p.product_name, p.unit, p.import_price, p.sale_price, p.stock_qty, p.min_stock, p.category_id, p.brand_id, p.status, p.created_at, p.updated_at, c.category_name, b.brand_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id LEFT JOIN brand b ON p.brand_id = b.brand_id WHERE p.barcode LIKE ? OR p.product_name LIKE ? ORDER BY p.product_name ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%"; ps.setString(1, pattern); ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { list.add(mapResultSet(rs)); } }
        } catch (Exception e) { throw new RuntimeException("ProductDAO.search error: " + e.getMessage(), e); }
        return list;
    }
    
    public List<Product> filter(Integer categoryId, Integer brandId, String status) {
        StringBuilder sql = new StringBuilder("SELECT p.product_id, p.barcode, p.product_name, p.unit, p.import_price, p.sale_price, p.stock_qty, p.min_stock, p.category_id, p.brand_id, p.status, p.created_at, p.updated_at, c.category_name, b.brand_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id LEFT JOIN brand b ON p.brand_id = b.brand_id WHERE 1=1");
        if (categoryId != null) sql.append(" AND p.category_id = ?");
        if (brandId != null) sql.append(" AND p.brand_id = ?");
        if (status != null) sql.append(" AND p.status = ?");
        sql.append(" ORDER BY p.product_name ASC");
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (categoryId != null) ps.setInt(idx++, categoryId);
            if (brandId != null) ps.setInt(idx++, brandId);
            if (status != null) ps.setString(idx++, status);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { list.add(mapResultSet(rs)); } }
        } catch (Exception e) { throw new RuntimeException("ProductDAO.filter error: " + e.getMessage(), e); }
        return list;
    }
    
    public Product findById(int productId) {
        String sql = "SELECT p.product_id, p.barcode, p.product_name, p.unit, p.import_price, p.sale_price, p.stock_qty, p.min_stock, p.category_id, p.brand_id, p.status, p.created_at, p.updated_at, c.category_name, b.brand_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id LEFT JOIN brand b ON p.brand_id = b.brand_id WHERE p.product_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapResultSet(rs); }
        } catch (Exception e) { throw new RuntimeException("ProductDAO.findById error: " + e.getMessage(), e); }
        return null;
    }
    
    public Product findByBarcode(String barcode) {
        String sql = "SELECT p.product_id, p.barcode, p.product_name, p.unit, p.import_price, p.sale_price, p.stock_qty, p.min_stock, p.category_id, p.brand_id, p.status, p.created_at, p.updated_at, c.category_name, b.brand_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id LEFT JOIN brand b ON p.brand_id = b.brand_id WHERE p.barcode = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapResultSet(rs); }
        } catch (Exception e) { throw new RuntimeException("ProductDAO.findByBarcode error: " + e.getMessage(), e); }
        return null;
    }
    
    public List<Product> findLowStockProducts() {
        String sql = "SELECT p.product_id, p.barcode, p.product_name, p.unit, p.import_price, p.sale_price, p.stock_qty, p.min_stock, p.category_id, p.brand_id, p.status, p.created_at, p.updated_at, c.category_name, b.brand_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id LEFT JOIN brand b ON p.brand_id = b.brand_id WHERE p.stock_qty <= p.min_stock AND p.status = 'ACTIVE' ORDER BY p.stock_qty ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (Exception e) { throw new RuntimeException("ProductDAO.findLowStockProducts error: " + e.getMessage(), e); }
        return list;
    }
    
    public int insert(Product product) {
        String sql = "INSERT INTO product(barcode, product_name, unit, import_price, sale_price, stock_qty, min_stock, category_id, brand_id, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getBarcode());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getUnit());
            ps.setBigDecimal(4, product.getImportPrice());
            ps.setBigDecimal(5, product.getSalePrice());
            ps.setInt(6, product.getStockQty());
            ps.setInt(7, product.getMinStock());
            ps.setInt(8, product.getCategoryId());
            if (product.getBrandId() != null) ps.setInt(9, product.getBrandId()); else ps.setNull(9, Types.INTEGER);
            ps.setString(10, product.getStatus());
            int rows = ps.executeUpdate();
            if (rows > 0) { try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); } }
        } catch (Exception e) { throw new RuntimeException("ProductDAO.insert error: " + e.getMessage(), e); }
        return 0;
    }
    
    public void update(Product product) {
        String sql = "UPDATE product SET barcode=?, product_name=?, unit=?, import_price=?, sale_price=?, stock_qty=?, min_stock=?, category_id=?, brand_id=?, status=? WHERE product_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getBarcode());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getUnit());
            ps.setBigDecimal(4, product.getImportPrice());
            ps.setBigDecimal(5, product.getSalePrice());
            ps.setInt(6, product.getStockQty());
            ps.setInt(7, product.getMinStock());
            ps.setInt(8, product.getCategoryId());
            if (product.getBrandId() != null) ps.setInt(9, product.getBrandId()); else ps.setNull(9, Types.INTEGER);
            ps.setString(10, product.getStatus());
            ps.setInt(11, product.getProductId());
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException("ProductDAO.update error: " + e.getMessage(), e); }
    }
    
    public void updateStock(int productId, int newQty) {
        String sql = "UPDATE product SET stock_qty=? WHERE product_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQty);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException("ProductDAO.updateStock error: " + e.getMessage(), e); }
    }
    
    public void delete(int productId) {
        String sql = "DELETE FROM product WHERE product_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException("ProductDAO.delete error: " + e.getMessage(), e); }
    }
    
    private Product mapResultSet(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setBarcode(rs.getString("barcode"));
        p.setProductName(rs.getString("product_name"));
        p.setUnit(rs.getString("unit"));
        p.setImportPrice(rs.getBigDecimal("import_price"));
        p.setSalePrice(rs.getBigDecimal("sale_price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setMinStock(rs.getInt("min_stock"));
        p.setCategoryId(rs.getInt("category_id"));
        int brandId = rs.getInt("brand_id");
        p.setBrandId(rs.wasNull() ? null : brandId);
        p.setStatus(rs.getString("status"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) p.setCreatedAt(createdTs.toLocalDateTime());
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) p.setUpdatedAt(updatedTs.toLocalDateTime());
        p.setCategoryName(rs.getString("category_name"));
        p.setBrandName(rs.getString("brand_name"));
        return p;
    }

    public int countByCategory(int categoryId) {
        String sql = "SELECT COUNT(*) as cnt FROM product WHERE category_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (Exception e) {
            throw new RuntimeException("ProductDAO.countByCategory error: " + e.getMessage(), e);
        }
        return 0;
    }
}