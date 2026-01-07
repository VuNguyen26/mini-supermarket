package dal.dao;

import dal.DBConnection;
import dto.Permission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class PermissionDAO {

    // ====== bạn đang dùng hàm này cho login load permission ======
    public Set<String> findPermCodesByUserId(int userId) {
        String sql =
                "SELECT p.perm_code " +
                        "FROM `user` u " +
                        "JOIN role_permission rp ON u.role_id = rp.role_id " +
                        "JOIN permission p ON p.perm_id = rp.perm_id " +
                        "WHERE u.user_id = ?";

        Set<String> perms = new HashSet<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) perms.add(rs.getString("perm_code"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Load permissions by user failed: " + e.getMessage(), e);
        }
        return perms;
    }

    // ====== NEW: list toàn bộ permission để hiện lên bảng checkbox ======
    public List<Permission> findAll() {
        String sql = "SELECT perm_id, perm_code, perm_name FROM permission ORDER BY perm_code ASC";
        List<Permission> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Permission(
                        rs.getInt("perm_id"),
                        rs.getString("perm_code"),
                        rs.getString("perm_name")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Load permissions failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ====== NEW: lấy perm_id hiện tại của 1 role để tick checkbox ======
    public Set<Integer> findPermIdsByRoleId(int roleId) {
        String sql =
                "SELECT rp.perm_id " +
                        "FROM role_permission rp " +
                        "WHERE rp.role_id = ?";

        Set<Integer> ids = new HashSet<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("perm_id"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Load role permissions failed: " + e.getMessage(), e);
        }
        return ids;
    }
}
