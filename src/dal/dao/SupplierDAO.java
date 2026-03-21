package dal.dao;

import dal.DBConnection;
import dto.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> findAll() {
        String sql = "SELECT supplier_id, supplier_code, supplier_name, phone, address, email, created_at " +
                "FROM supplier ORDER BY supplier_name ASC";
        List<Supplier> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.findAll error: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Supplier> search(String keyword) {
        String sql = "SELECT supplier_id, supplier_code, supplier_name, phone, address, email, created_at " +
                "FROM supplier " +
                "WHERE (supplier_code LIKE ? OR supplier_name LIKE ? OR phone LIKE ?) " +
                "ORDER BY supplier_name ASC";
        List<Supplier> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.search error: " + e.getMessage(), e);
        }
        return list;
    }

    public Supplier findById(int supplierId) {
        String sql = "SELECT supplier_id, supplier_code, supplier_name, phone, address, email, created_at " +
                "FROM supplier WHERE supplier_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.findById error: " + e.getMessage(), e);
        }
        return null;
    }

    public Supplier findByName(String supplierName) {
        String sql = "SELECT supplier_id, supplier_code, supplier_name, phone, address, email, created_at " +
                "FROM supplier WHERE supplier_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplierName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.findByName error: " + e.getMessage(), e);
        }
        return null;
    }

    public Supplier findByCode(String supplierCode) {
        String sql = "SELECT supplier_id, supplier_code, supplier_name, phone, address, email, created_at " +
                "FROM supplier WHERE supplier_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplierCode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.findByCode error: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean existsByCode(String supplierCode) {
        String sql = "SELECT 1 FROM supplier WHERE supplier_code = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplierCode);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.existsByCode error: " + e.getMessage(), e);
        }
    }

    public boolean existsByCodeExceptId(String supplierCode, int supplierId) {
        String sql = "SELECT 1 FROM supplier WHERE supplier_code = ? AND supplier_id <> ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplierCode);
            ps.setInt(2, supplierId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.existsByCodeExceptId error: " + e.getMessage(), e);
        }
    }

    public int insert(Supplier supplier) {
        String sql = "INSERT INTO supplier(supplier_code, supplier_name, phone, email, address) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, supplier.getSupplierCode());
            ps.setString(2, supplier.getSupplierName());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.insert error: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean update(Supplier supplier) {
        String sql = "UPDATE supplier " +
                "SET supplier_code = ?, supplier_name = ?, phone = ?, email = ?, address = ? " +
                "WHERE supplier_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplier.getSupplierCode());
            ps.setString(2, supplier.getSupplierName());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setInt(6, supplier.getSupplierId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.update error: " + e.getMessage(), e);
        }
    }

    public boolean delete(int supplierId) {
        String sql = "DELETE FROM supplier WHERE supplier_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("SupplierDAO.delete error: " + e.getMessage(), e);
        }
    }

    private Supplier mapResultSet(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setSupplierCode(rs.getString("supplier_code"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            s.setCreatedAt(created.toLocalDateTime());
        }

        return s;
    }
}