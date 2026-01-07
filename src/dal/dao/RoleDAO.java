package dal.dao;

import dal.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Load roles for combobox.
 * role(role_id, role_name)
 */
public class RoleDAO {

    public Map<Integer, String> findAllRoles() {
        String sql = "SELECT role_id, role_name FROM role ORDER BY role_id ASC";
        Map<Integer, String> map = new LinkedHashMap<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getInt("role_id"), rs.getString("role_name"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Load roles failed: " + e.getMessage(), e);
        }
        return map;
    }
}
