package de.upteams.tasktracker.audit.aspect;

import de.upteams.tasktracker.audit.annotation.Auditable;
import de.upteams.tasktracker.audit.resolver.CurrentUserResolver;
import de.upteams.tasktracker.audit.utils.AuditLogAction;
import de.upteams.tasktracker.audit.service.AuditLogService;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.marker.entity.Marker;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.user.entity.AppUser;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final CurrentUserResolver currentUserResolver;
    private final EntityManager entityManager;

    private final ThreadLocal<Object> oldStatusHolder = new ThreadLocal<>();
    private final ThreadLocal<Set<Marker>> oldMarkerHolder = new ThreadLocal<>();
    private final ThreadLocal<String> oldTitleHolder = new ThreadLocal<>();
    private final ThreadLocal<String> oldDescriptionHolder = new ThreadLocal<>();
    private final ThreadLocal<Instant> oldDueDateHolder = new ThreadLocal<>();

    //===== CREATE =====
    @AfterReturning(value = "@annotation(auditable)", returning = "result")
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

    //===== DELETE =====
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

        Object arg = args[0];

        UUID entityId;
        try {
            if (arg instanceof UUID) {
                entityId = (UUID) arg;
            } else {
                entityId = UUID.fromString(arg.toString());
            }
        } catch (Exception e) {
            return;
        }

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

    //===== UPDATE =====
    @Before("@annotation(auditable)")
    public void beforeUpdate(JoinPoint joinPoint, Auditable auditable) {
        if (auditable.action() != AuditLogAction.UPDATE) return;
        if (auditable.entityClass() == Void.class) return;

        Object[] args = joinPoint.getArgs();
        if (args.length == 0) return;

        Object arg = args[0];
        UUID entityId;
        try {
            entityId = (arg instanceof UUID) ? (UUID) arg : UUID.fromString(arg.toString());
        } catch (Exception e) {
            return;
        }

        Task entity = entityManager.find(Task.class, entityId);
        if (entity != null) {
            oldStatusHolder.set(entity.getStatus() != null ? entity.getStatus().getId() : null);
            oldMarkerHolder.set(new HashSet<>(entity.getMarkers()));
            oldTitleHolder.set(entity.getTitle());
            oldDescriptionHolder.set(entity.getDescription());
            oldDueDateHolder.set(entity.getDueDate());
        }
    }

    @AfterReturning(value = "@annotation(auditable)", returning = "result")
    public void afterUpdate(JoinPoint joinPoint, Auditable auditable, Object result) {
        if (auditable.action() != AuditLogAction.UPDATE || result == null) return;

        AppUser user = currentUserResolver.getCurrentUser();
        if (user == null) return;

        String entityId = extractId(result);
        String entityName = extractNameFromDto(result);
        String projectId = extractProjectIdFromDto(result);

        TaskResponseDto taskDto = (TaskResponseDto) result;

        //===== MOVE =====
        Object oldStatusObj = oldStatusHolder.get();
        oldStatusHolder.remove();
        String oldStatusId = oldStatusObj != null ? oldStatusObj.toString() : null;
        String newStatusId = extractStatusIdFromDto(result);

        if (oldStatusId != null && !oldStatusId.equals(newStatusId)) {
            String oldStatusTitle = getStatusTitleById(oldStatusId);
            String newStatusTitle = getStatusTitleById(newStatusId);
            String diff = String.format("fromStatus=%s,toStatus=%s",
                    oldStatusTitle != null ? oldStatusTitle : oldStatusId,
                    newStatusTitle != null ? newStatusTitle : newStatusId
            );

            auditLogService.logAction(
                    auditable.entity(),
                    entityId,
                    entityName,
                    projectId,
                    AuditLogAction.MOVE.name(),
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getAvatarUrl(),
                    diff
            );
        }

        //===== MARKERS =====
        Set<Marker> oldMarkers = oldMarkerHolder.get();
        oldMarkerHolder.remove();

        Set<MarkerResponseDto> newMarkersDto = new HashSet<>();
        try {
            if (taskDto.markers() != null) newMarkersDto.addAll(taskDto.markers());
        } catch (Exception ignored) {
        }

        String markerDiff = buildMarkerDiff(oldMarkers, newMarkersDto);
        if (!markerDiff.isEmpty()) {
            auditLogService.logAction(
                    auditable.entity(),
                    entityId,
                    entityName,
                    projectId,
                    AuditLogAction.MARKERS.name(),
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getAvatarUrl(),
                    markerDiff
            );
        }

        //===== TITLE =====
        String oldTitle = oldTitleHolder.get();
        oldTitleHolder.remove();
        if (oldTitle != null && !oldTitle.equals(taskDto.title())) {
            String titleDiff = String.format("oldTitle=%s,newTitle=%s", oldTitle, taskDto.title());
            auditLogService.logAction(
                    auditable.entity(),
                    entityId,
                    entityName,
                    projectId,
                    AuditLogAction.TITLE.name(),
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getAvatarUrl(),
                    titleDiff
            );
        }

        //===== DESCRIPTION =====
        String oldDescription = oldDescriptionHolder.get();
        oldDescriptionHolder.remove();
        if (oldDescription != null && !oldDescription.equals(taskDto.description())) {
            String descriptionDiff = String.format("oldDescription=%s,newDescription=%s", oldDescription, taskDto.description());
            auditLogService.logAction(
                    auditable.entity(),
                    entityId,
                    entityName,
                    projectId,
                    AuditLogAction.DESCRIPTION.name(),
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getAvatarUrl(),
                    descriptionDiff
            );
        }

        //===== DUE DATE =====
        Instant oldDueDate = oldDueDateHolder.get();
        oldDueDateHolder.remove();

        Instant newDueDate = null;
        try {
            if (taskDto.dueDate() != null) newDueDate = Instant.parse(taskDto.dueDate());
        } catch (Exception ignored) {
        }

        if (oldDueDate == null && newDueDate != null) {
            auditLogService.logAction(
                    auditable.entity(),
                    entityId,
                    entityName,
                    projectId,
                    AuditLogAction.DUE_DATE.name(),
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getAvatarUrl(),
                    "dueDateAdded=" + newDueDate
            );
        } else if (oldDueDate != null && newDueDate == null) {
            auditLogService.logAction(
                    auditable.entity(),
                    entityId,
                    entityName,
                    projectId,
                    AuditLogAction.DUE_DATE.name(),
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getAvatarUrl(),
                    "dueDateRemoved=" + oldDueDate
            );
        } else if (oldDueDate != null && !oldDueDate.equals(newDueDate)) {
            auditLogService.logAction(
                    auditable.entity(),
                    entityId,
                    entityName,
                    projectId,
                    AuditLogAction.DUE_DATE.name(),
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getAvatarUrl(),
                    "dueDateChanged=" + oldDueDate + "->" + newDueDate
            );
        }
    }

    //===== HELPERS ====
    private String extractId(Object dto) {
        try {
            return dto.getClass()
                    .getMethod("getId")
                    .invoke(dto)
                    .toString();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String extractNameFromDto(Object dto) {
        try {
            return dto.getClass()
                    .getMethod("getTitle")
                    .invoke(dto)
                    .toString();
        } catch (Exception ignored) {
        }

        try {
            return dto.getClass()
                    .getMethod("getName")
                    .invoke(dto)
                    .toString();
        } catch (Exception ignored) {
        }

        return "UNKNOWN";
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
            return "UNKNOWN";
        }

        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(entity);
            return value != null ? value.toString() : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
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

    private String extractStatusIdFromDto(Object dto) {
        try {
            Object statusId = dto.getClass().getMethod("getStatusId").invoke(dto); // <-- изменила здесь
            return statusId != null ? statusId.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getStatusTitleById(String statusId) {
        if (statusId == null) return null;
        try {
            TaskStatus status = entityManager.find(TaskStatus.class, UUID.fromString(statusId));
            return status != null ? status.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildMarkerDiff(Set<Marker> oldMarkers, Set<MarkerResponseDto> newMarkersDto) {
        if (oldMarkers == null && newMarkersDto == null) return "";

        Set<String> added = new HashSet<>();
        Set<String> removed = new HashSet<>();

        Set<String> oldTitles = new HashSet<>();
        if (oldMarkers != null) {
            oldMarkers.forEach(m -> oldTitles.add(m.getName()));
        }

        Set<String> newTitles = new HashSet<>();
        if (newMarkersDto != null) {
            newMarkersDto.forEach(m -> newTitles.add(m.name()));
        }

        for (String title : newTitles) {
            if (!oldTitles.contains(title)) added.add(title);
        }
        for (String title : oldTitles) {
            if (!newTitles.contains(title)) removed.add(title);
        }

        StringBuilder sb = new StringBuilder();
        if (!added.isEmpty()) sb.append("markerAdded=").append(String.join("|", added));
        if (!removed.isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("markerRemoved=").append(String.join("|", removed));
        }
        return sb.toString();
    }
}