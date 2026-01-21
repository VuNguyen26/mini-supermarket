package dal.dao;

import dal.DBConnection;
import dto.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    public List<Customer> searchCustomer(String keyword) {
        List<Customer> list = new ArrayList<>();
        list.add(new Customer(0, "Khách lẻ", "", "", 0));
        String sql;
        if (keyword == null || keyword.trim().isEmpty()) {
            sql = "SELECT * FROM customer ORDER BY created_at DESC LIMIT 20";
        } else {
            sql = "SELECT * FROM customer WHERE customer_name LIKE ? OR phone LIKE ? LIMIT 20";
        }
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String query = "%" + keyword + "%";
                ps.setString(1, query);
                ps.setString(2, query);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Customer c = new Customer();
                c.setCustomerId(rs.getInt("customer_id"));
                c.setCustomerName(rs.getString("customer_name"));
                c.setPhone(rs.getString("phone"));
                c.setAddress(rs.getString("address"));
                c.setPoints(rs.getInt("points"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customer WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getInt("points"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}