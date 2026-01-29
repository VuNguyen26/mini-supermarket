package dto;

import java.time.LocalDateTime;

public class AuditLog {

    private long logId;
    private String username;
    private String action;
    private String entityName;
    private String description;
    private LocalDateTime createdAt;

    // ===== Constructor =====
    public AuditLog() {
    }

    public AuditLog(String username, String action, String entityName, String description, LocalDateTime createdAt) {
        this.username = username;
        this.action = action;
        this.entityName = entityName;
        this.description = description;
        this.createdAt = createdAt;
    }

    // ===== Getter & Setter =====
    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
