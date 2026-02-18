package de.upteams.tasktracker.audit.service;

import de.upteams.tasktracker.audit.entity.AuditLogEntity;
import de.upteams.tasktracker.audit.persistence.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repository;

    @Override
    public void logAction(
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
    ) {
        AuditLogEntity audit = new AuditLogEntity();
        audit.setEntity(entity);
        audit.setEntityId(entityId);
        audit.setEntityName(entityName);
        audit.setProjectId(projectId);
        audit.setAction(action);
        audit.setUserId(userId);
        audit.setUserEmail(userEmail);
        audit.setUserFirstName(userFirstName);
        audit.setUserLastName(userLastName);
        audit.setUserAvatar(userAvatar);
        audit.setDifference(difference);
        audit.setCreatedAt(Instant.now());

        repository.save(audit);
    }
}