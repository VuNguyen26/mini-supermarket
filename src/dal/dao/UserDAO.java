package dal.dao;

import dal.DBConnection;
import dto.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<User> findAll() throws SQLException {
        String sql = "SELECT user_id, username, password_hash, full_name, phone, role_id, status, created_at " +
                "FROM `user` ORDER BY user_id ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<User> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public List<User> search(String keyword) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, full_name, phone, role_id, status, created_at " +
                "FROM `user` " +
                "WHERE username LIKE ? OR full_name LIKE ? OR phone LIKE ? " +
                "ORDER BY user_id ASC";
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);

            List<User> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }

    public boolean existsUsername(String username, Integer excludeUserId) throws SQLException {
        String sql = "SELECT 1 FROM `user` WHERE username = ? " +
                (excludeUserId != null ? "AND user_id <> ?" : "") +
                " LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            if (excludeUserId != null) ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insert(User u) throws SQLException {
        String sql = "INSERT INTO `user`(username, password_hash, full_name, phone, role_id, status, created_at) " +
                "VALUES(?,?,?,?,?,?, NOW())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getPhone());
            ps.setInt(5, u.getRoleId());
            ps.setString(6, u.getStatus());

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        }
    }

    public boolean updateInfo(User u) throws SQLException {
        String sql = "UPDATE `user` SET full_name=?, phone=?, role_id=?, status=? WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setInt(3, u.getRoleId());
            ps.setString(4, u.getStatus());
            ps.setInt(5, u.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateWithPassword(User u) throws SQLException {
        String sql = "UPDATE `user` SET full_name=?, phone=?, role_id=?, status=?, password_hash=? WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setInt(3, u.getRoleId());
            ps.setString(4, u.getStatus());
            ps.setString(5, u.getPasswordHash());
            ps.setInt(6, u.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean setStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE `user` SET status=? WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, full_name, phone, role_id, status, created_at " +
                "FROM `user` WHERE username = ? LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setRoleId(rs.getInt("role_id"));
        u.setStatus(rs.getString("status"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}