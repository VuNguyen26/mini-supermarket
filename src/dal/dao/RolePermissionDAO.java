package dal.dao;

import dal.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Update mapping role_permission.
 * Safety rule:
 *  - If role is ADMIN => always keep ROLE_PERMISSION_MANAGE
 *    (avoid locking yourself out of Permission screen)
 */
public class RolePermissionDAO {

    private static final String PERM_ROLE_PERMISSION_MANAGE = "ROLE_PERMISSION_MANAGE";

    public void replaceRolePermissions(int roleId, Set<Integer> permIds) {
        String deleteSql = "DELETE FROM role_permission WHERE role_id = ?";
        String insertSql = "INSERT INTO role_permission(role_id, perm_id) VALUES(?, ?)";

        // clone to avoid modifying caller set
        Set<Integer> safePermIds = (permIds == null) ? new HashSet<>() : new HashSet<>(permIds);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // ===== Safety: ADMIN always has ROLE_PERMISSION_MANAGE =====
            if (isAdminRole(con, roleId)) {
                Integer pid = getPermIdByCode(con, PERM_ROLE_PERMISSION_MANAGE);
                if (pid != null) safePermIds.add(pid);
            }
            // ===========================================================

            // 1) delete old
            try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                ps.setInt(1, roleId);
                ps.executeUpdate();
            }

            // 2) insert new
            if (!safePermIds.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    for (Integer pid : safePermIds) {
                        ps.setInt(1, roleId);
                        ps.setInt(2, pid);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            con.commit();
        } catch (Exception e) {
            throw new RuntimeException("Save role permissions failed: " + e.getMessage(), e);
        }
    }

    private boolean isAdminRole(Connection con, int roleId) throws Exception {
        String sql = "SELECT role_name FROM role WHERE role_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String roleName = rs.getString("role_name");
                return roleName != null && "ADMIN".equalsIgnoreCase(roleName.trim());
            }
        }
    }

    private Integer getPermIdByCode(Connection con, String code) throws Exception {
        String sql = "SELECT perm_id FROM permission WHERE perm_code = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("perm_id");
            }
        }
    }
}
