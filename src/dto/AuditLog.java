package dto;

import java.time.LocalDateTime;

public class AuditLog {

    // ===== Columns in DB audit_log =====
    private long logId;
    private Integer userId;       // nullable
    private String action;        // e.g. CREATE/UPDATE/DELETE
    private String entity;        // e.g. supplier, sales_invoice...
    private Long entityId;        // nullable
    private String message;       // detail
    private LocalDateTime createdAt;

    // ===== Optional display (from join user table) =====
    private String username;      // not stored in audit_log, only for UI display

    public AuditLog() {}

    // ===== Getters/Setters (DB) =====
    public long getLogId() { return logId; }
    public void setLogId(long logId) { this.logId = logId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ===== For UI display =====
    public String getUsername() { return (username == null || username.isBlank()) ? "SYSTEM" : username; }
    public void setUsername(String username) { this.username = username; }

    // ===== COMPAT (để code UI cũ không vỡ) =====
    // UI của bạn đang dùng: getEntityName(), getDescription()
    public String getEntityName() { return getEntity(); }
    public void setEntityName(String entityName) { setEntity(entityName); }

    public String getDescription() { return getMessage(); }
    public void setDescription(String description) { setMessage(description); }
}
