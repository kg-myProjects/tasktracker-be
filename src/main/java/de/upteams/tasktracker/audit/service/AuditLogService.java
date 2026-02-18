package de.upteams.tasktracker.audit.service;

public interface AuditLogService {

    void logAction(
            String entity,
            String entityId,
            String entityName,
            String projectId,
            String action,
            String userId,
            String userEmail,
            String userFirstName,
            String userLastName,
            String userAvatar,
            String difference
    );
}