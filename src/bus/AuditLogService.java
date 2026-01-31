package bus;

import dal.dao.AuditLogDAO;
import dto.AuditLog;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AuditLogService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // Dùng schema DB: user_id, action, entity, entity_id, message
    public void log(Integer userId, String action, String entity, Long entityId, String message) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());

        try {
            auditLogDAO.insert(log);
        } catch (SQLException e) {
            e.printStackTrace(); // không throw lên UI
        }
    }

    public List<AuditLog> getAll() {
        try {
            return auditLogDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải audit log", e);
        }
    }
}
