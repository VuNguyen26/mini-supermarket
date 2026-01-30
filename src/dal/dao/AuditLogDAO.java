package dal.dao;

import dal.DBConnection;
import dto.AuditLog;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {


    public void insert(AuditLog log) throws SQLException {
        String sql = """
            INSERT INTO audit_log (username, action, entity_name, description)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, log.getUsername());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityName());
            ps.setString(4, log.getDescription());
            ps.executeUpdate();
        }
    }


    public List<AuditLog> findAll() throws SQLException {
        List<AuditLog> list = new ArrayList<>();

        String sql = """
            SELECT log_id, username, action, entity_name, description, created_at
            FROM audit_log
            ORDER BY created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    // Search Thêm

    public List<AuditLog> search(
            LocalDate fromDate,
            LocalDate toDate,
            String username,
            String action
    ) throws SQLException {

        List<AuditLog> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT log_id, username, action, entity_name, description, created_at
            FROM audit_log
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append(" AND created_at >= ?");
            params.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }

        if (toDate != null) {
            sql.append(" AND created_at < ?");
            params.add(Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
        }

        if (username != null && !username.isBlank()) {
            sql.append(" AND username = ?");
            params.add(username);
        }

        if (action != null && !action.isBlank()) {
            sql.append(" AND action = ?");
            params.add(action);
        }

        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    // MAP RESULT

    private AuditLog map(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getLong("log_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setEntityName(rs.getString("entity_name"));
        log.setDescription(rs.getString("description"));

        Timestamp ts = rs.getTimestamp("created_at");
        log.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return log;
    }
}
