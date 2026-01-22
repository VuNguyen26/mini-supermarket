package bus;

import dal.dao.AuditLogDAO;
import dto.AuditLog;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AuditLogService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // Lấy tất cả audit logs
    public List<AuditLog> getAllAuditLogs() throws SQLException {
        return auditLogDAO.getAllAuditLogs();
    }

    // Lấy audit logs theo khoảng thời gian
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return auditLogDAO.getAuditLogsByDateRange(startDate, endDate);
    }

    // Lấy audit logs theo user
    public List<AuditLog> getAuditLogsByUser(int userId) throws SQLException {
        return auditLogDAO.getAuditLogsByUser(userId);
    }

    // Lấy audit logs theo action
    public List<AuditLog> getAuditLogsByAction(String action) throws SQLException {
        List<AuditLog> allLogs = getAllAuditLogs();
        return allLogs.stream()
                .filter(log -> action.equals(log.getAction()))
                .toList();
    }

    // Lấy audit logs theo table
    public List<AuditLog> getAuditLogsByTable(String tableName) throws SQLException {
        List<AuditLog> allLogs = getAllAuditLogs();
        return allLogs.stream()
                .filter(log -> tableName.equals(log.getTableName()))
                .toList();
    }

    // Thêm audit log mới
    public void logAction(int userId, String action, String tableName, String recordId,
                         String oldValues, String newValues, String ipAddress) throws SQLException {
        // TODO: Implement insert into audit_log table
        // Hiện tại chỉ log ra console
        System.out.println("AUDIT LOG: User " + userId + " performed " + action +
                          " on " + tableName + " record " + recordId);
    }
}
