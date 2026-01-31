package dal.dao;

import dal.DBConnection;
import dto.AuditLog;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public void insert(AuditLog log) throws SQLException {

        // created_at trong DB thường có DEFAULT CURRENT_TIMESTAMP
        // nhưng mình vẫn set nếu log.getCreatedAt() != null cho chắc chắn
        String sql = """
            INSERT INTO audit_log (user_id, action, entity, entity_id, message, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // user_id nullable
            if (log.getUserId() == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, log.getUserId());

            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntity());

            // entity_id nullable
            if (log.getEntityId() == null) ps.setNull(4, Types.BIGINT);
            else ps.setLong(4, log.getEntityId());

            ps.setString(5, log.getMessage());

            if (log.getCreatedAt() == null) {
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            } else {
                ps.setTimestamp(6, Timestamp.valueOf(log.getCreatedAt()));
            }

            ps.executeUpdate();
        }
    }

    public List<AuditLog> findAll() throws SQLException {
        List<AuditLog> list = new ArrayList<>();

        // Join user để lấy username hiển thị
        String sql = """
            SELECT
                al.log_id,
                al.user_id,
                u.username AS username,
                al.action,
                al.entity,
                al.entity_id,
                al.message,
                al.created_at
            FROM audit_log al
            LEFT JOIN user u ON al.user_id = u.user_id
            ORDER BY al.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        }

        return list;
    }

    public List<AuditLog> search(
            LocalDate fromDate,
            LocalDate toDate,
            String username,
            String action
    ) throws SQLException {

        List<AuditLog> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT
                al.log_id,
                al.user_id,
                u.username AS username,
                al.action,
                al.entity,
                al.entity_id,
                al.message,
                al.created_at
            FROM audit_log al
            LEFT JOIN user u ON al.user_id = u.user_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append(" AND al.created_at >= ?");
            params.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }

        if (toDate != null) {
            sql.append(" AND al.created_at < ?");
            params.add(Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
        }

        if (username != null && !username.isBlank()) {
            sql.append(" AND u.username = ?");
            params.add(username.trim());
        }

        if (action != null && !action.isBlank()) {
            sql.append(" AND al.action = ?");
            params.add(action.trim());
        }

        sql.append(" ORDER BY al.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }

        return list;
    }

    private AuditLog map(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();

        log.setLogId(rs.getLong("log_id"));

        int uid = rs.getInt("user_id");
        log.setUserId(rs.wasNull() ? null : uid);

        log.setUsername(rs.getString("username"));

        log.setAction(rs.getString("action"));
        log.setEntity(rs.getString("entity"));

        long eid = rs.getLong("entity_id");
        log.setEntityId(rs.wasNull() ? null : eid);

        log.setMessage(rs.getString("message"));

        Timestamp ts = rs.getTimestamp("created_at");
        log.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return log;
    }
}
