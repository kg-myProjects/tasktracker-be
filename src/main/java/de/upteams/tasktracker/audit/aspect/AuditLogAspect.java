package de.upteams.tasktracker.audit.aspect;

import de.upteams.tasktracker.audit.annotation.Auditable;
import de.upteams.tasktracker.audit.resolver.CurrentUserResolver;
import de.upteams.tasktracker.audit.utils.AuditLogAction;
import de.upteams.tasktracker.audit.service.AuditLogService;
import de.upteams.tasktracker.user.entity.AppUser;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final CurrentUserResolver currentUserResolver;
    private final EntityManager entityManager;

    @AfterReturning(
            value = "@annotation(auditable)",
            returning = "result"
    )
    public void afterCreate(JoinPoint joinPoint, Auditable auditable, Object result) {
        if (auditable.action() != AuditLogAction.CREATE) {
            return;
        }

        if (result == null) {
            return;
        }

        AppUser user = currentUserResolver.getCurrentUser();
        if (user == null) {
            return;
        }

        String entityId = extractId(result);
        String entityName = extractNameFromDto(result);
        String projectId = extractProjectIdFromDto(result);

        auditLogService.logAction(
                auditable.entity(),
                entityId,
                entityName,
                projectId,
                AuditLogAction.CREATE.name(),
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                ""
        );
    }

    @Before("@annotation(auditable)")
    public void beforeDelete(JoinPoint joinPoint, Auditable auditable) {
        if (auditable.action() != AuditLogAction.DELETE) {
            return;
        }

        if (auditable.entityClass() == Void.class) {
            return;
        }

        AppUser user = currentUserResolver.getCurrentUser();
        if (user == null) {
            return;
        }

        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return;
        }

        UUID entityId = UUID.fromString(args[0].toString());

        Object entity = entityManager.find(auditable.entityClass(), entityId);
        if (entity == null) {
            return;
        }

        String entityName = readField(entity, auditable.nameField());
        String projectId = readProjectId(entity);

        auditLogService.logAction(
                auditable.entity(),
                entityId.toString(),
                entityName,
                projectId,
                AuditLogAction.DELETE.name(),
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                ""
        );
    }



    private String extractId(Object dto) {
        try {
            return dto.getClass()
                    .getMethod("getId")
                    .invoke(dto)
                    .toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractNameFromDto(Object dto) {
        try {
            return dto.getClass()
                    .getMethod("getTitle")
                    .invoke(dto)
                    .toString();
        } catch (Exception ignored) {}

        try {
            return dto.getClass()
                    .getMethod("getName")
                    .invoke(dto)
                    .toString();
        } catch (Exception ignored) {}

        return null;
    }

    private String extractProjectIdFromDto(Object dto) {
        try {
            Object projectId = dto.getClass()
                    .getMethod("getProjectId")
                    .invoke(dto);

            return projectId != null ? projectId.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String readField(Object entity, String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return null;
        }

        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(entity);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String readProjectId(Object entity) {
        try {
            Object project = entity.getClass()
                    .getMethod("getProject")
                    .invoke(entity);

            return project.getClass()
                    .getMethod("getId")
                    .invoke(project)
                    .toString();
        } catch (Exception e) {
            return null;
        }
    }
}