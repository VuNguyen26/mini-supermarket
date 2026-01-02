package dal.dao;

import bus.AuthService.AuthUser;
import dal.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public AuthUser findByUsername(String username) {
        String sql = """
            SELECT u.user_id, u.username, u.password_hash, u.full_name, u.phone, u.role_id, u.status,
                   r.role_name
            FROM `user` u
            JOIN role r ON u.role_id = r.role_id
            WHERE u.username = ?
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                AuthUser u = new AuthUser();
                u.userId = rs.getInt("user_id");
                u.username = rs.getString("username");
                u.passwordHash = rs.getString("password_hash");
                u.fullName = rs.getString("full_name");
                u.phone = rs.getString("phone");
                u.roleId = rs.getInt("role_id");
                u.status = rs.getString("status");
                u.roleName = rs.getString("role_name");
                return u;
            }
        } catch (Exception e) {
            throw new RuntimeException("UserDAO.findByUsername error: " + e.getMessage(), e);
        }
    }
}
