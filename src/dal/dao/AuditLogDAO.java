package dal.dao;

import dal.DBConnection;
import dto.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    /**
     * Tìm kiếm audit log theo keyword + action
     * keyword: tìm trong username, entity_name, description
     * action: CREATE / UPDATE / DELETE / LOGIN / LOGOUT
     */
    public List<AuditLog> search(String keyword, String action) {
        List<AuditLog> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT
                log_id,
                username,
                action,
                entity_name,
                description,
                created_at
            FROM audit_log
            WHERE 1 = 1
        """);

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("""
                AND (
                    username LIKE ?
                    OR entity_name LIKE ?
                    OR description LIKE ?
                )
            """);
        }

        if (action != null && !action.isEmpty()) {
            sql.append(" AND action = ? ");
        }

        sql.append(" ORDER BY created_at DESC ");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (keyword != null && !keyword.isEmpty()) {
                String key = "%" + keyword + "%";
                ps.setString(index++, key);
                ps.setString(index++, key);
                ps.setString(index++, key);
            }

            if (action != null && !action.isEmpty()) {
                ps.setString(index++, action);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Ghi log audit
     * Dùng cho CREATE / UPDATE / DELETE / LOGIN / LOGOUT
     */
    public void insert(AuditLog log) {
        String sql = """
            INSERT INTO audit_log
                (username, action, entity_name, description, created_at)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, log.getUsername());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityName());
            ps.setString(4, log.getDescription());
            ps.setTimestamp(5, Timestamp.valueOf(log.getCreatedAt()));

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MAP RESULT =================
    private AuditLog map(ResultSet rs) throws Exception {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getLong("log_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setEntityName(rs.getString("entity_name"));
        log.setDescription(rs.getString("description"));
        log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return log;
    }
}
