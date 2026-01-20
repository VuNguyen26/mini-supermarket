package dal.dao;

import dal.DBConnection;
import dto.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> findAll() {
        String sql = "SELECT customer_id, customer_name, phone, address, points, created_at FROM customer ORDER BY customer_name ASC";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (Exception e) { throw new RuntimeException("CustomerDAO.findAll error: " + e.getMessage(), e); }
        return list;
    }

    public List<Customer> search(String keyword) {
        String sql = "SELECT customer_id, customer_name, phone, address, points, created_at FROM customer WHERE customer_name LIKE ? OR phone LIKE ? ORDER BY customer_name ASC";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { list.add(mapResultSet(rs)); } }
        } catch (Exception e) { throw new RuntimeException("CustomerDAO.search error: " + e.getMessage(), e); }
        return list;
    }

    public Customer findById(int customerId) {
        String sql = "SELECT customer_id, customer_name, phone, address, points, created_at FROM customer WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapResultSet(rs); }
        } catch (Exception e) { throw new RuntimeException("CustomerDAO.findById error: " + e.getMessage(), e); }
        return null;
    }

    public Customer findByPhone(String phone) {
        String sql = "SELECT customer_id, customer_name, phone, address, points, created_at FROM customer WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapResultSet(rs); }
        } catch (Exception e) { throw new RuntimeException("CustomerDAO.findByPhone error: " + e.getMessage(), e); }
        return null;
    }

    public int insert(Customer customer) {
        String sql = "INSERT INTO customer(customer_name, phone, address, points) VALUES(?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getAddress());
            ps.setInt(4, customer.getPoints() != null ? customer.getPoints() : 0);
            int rows = ps.executeUpdate();
            if (rows > 0) { try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); } }
        } catch (Exception e) { throw new RuntimeException("CustomerDAO.insert error: " + e.getMessage(), e); }
        return -1;
    }

    public boolean update(Customer customer) {
        String sql = "UPDATE customer SET customer_name=?, phone=?, address=?, points=? WHERE customer_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getAddress());
            ps.setInt(4, customer.getPoints());
            ps.setInt(5, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { throw new RuntimeException("CustomerDAO.update error: " + e.getMessage(), e); }
    }

    public boolean delete(int customerId) {
        String sql = "DELETE FROM customer WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { throw new RuntimeException("CustomerDAO.delete error: " + e.getMessage(), e); }
    }

    public boolean updateLoyaltyPoints(int customerId, int points) {
        String sql = "UPDATE customer SET points = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, customerId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { throw new RuntimeException("CustomerDAO.updateLoyaltyPoints error: " + e.getMessage(), e); }
    }

    private Customer mapResultSet(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setCustomerName(rs.getString("customer_name"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setPoints(rs.getInt("points"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) c.setCreatedAt(created.toLocalDateTime());
        return c;
    }
}
