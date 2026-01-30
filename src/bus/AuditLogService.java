package bus;

import dal.dao.AuditLogDAO;
import dto.AuditLog;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AuditLogService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // Ghi log
    public void log(String username, String action, String entityName, String description) {

        if (username == null || username.isBlank()) {
            username = "SYSTEM";
        }

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());

        try {
            auditLogDAO.insert(log);
        } catch (SQLException e) {
            // KHÔNG throw ngược lên UI
            e.printStackTrace();
        }
    }

    //Lấy toàn bộ audit log
    public List<AuditLog> getAll() {
        try {
            return auditLogDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải audit log", e);
        }
    }
}
