package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.audit.annotation.Auditable;
import de.upteams.tasktracker.audit.utils.AuditLogAction;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.marker.service.interfaces.MarkerService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Attachment;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.service.interfaces.CheckList;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.task.exception.InvalidTaskPayloadException;
import de.upteams.tasktracker.task.exception.TaskNotFoundException;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.taskstatus.persistence.TaskStatusRepository;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final TaskStatusRepository taskStatusRepository;
    private final ProjectRepository projectRepository;
    private final TaskMappingService mappingService;
    private final CollaboratorService collaboratorService;
    private final MarkerService markerService;
    private final CheckList checklistService;

    @Override
    @Transactional
    @Auditable(entity = "Task", action = AuditLogAction.CREATE)
    public TaskResponseDto save(TaskCreateDto dto, AppUser authUser) {
        TaskStatus status = taskStatusRepository.findById(UUID.fromString(dto.getStatusId()))
                .orElseThrow(() -> new InvalidTaskPayloadException("TaskStatus not found"));
        Project project = projectRepository.findById(UUID.fromString(dto.getProjectId()))
                .orElseThrow(() -> new InvalidTaskPayloadException("Project not found"));

        boolean canCreate = collaboratorService.hasUserPermission(
                authUser,
                project,
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER)
        );

        if (!canCreate) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "You don't have permission to create tasks in this project");
        }
        Task task = mappingService.mapDtoToEntity(dto);
        task.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        task.setStatus(status);
        task.setProject(project);

        return mappingService.mapEntityToDto(repository.save(task));
    }


    @Override
    @Transactional
    public TaskResponseDto update(String id, TaskUpdateDto dto, AppUser authUser) {
        Task task = getOrThrow(id);

        Project project = task.getProject();

        boolean hasPermission = collaboratorService.hasUserPermission(
                authUser,
                project,
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER)
        );

       if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "You don't have permission to modify tasks in this project");
        }

        if (dto.title() != null) updateTitle(task, dto.title(), authUser);
        if (dto.description() != null) updateDescription(task, dto.description(), authUser);
        if (dto.statusId() != null) updateStatus(task, dto.statusId(), authUser);
        if (dto.executorIds() != null) collaboratorService.syncTaskExecutors(task, dto.executorIds());

        if (dto.markerIds() != null) markerService.syncTaskMarkers(task, dto.markerIds());

        if (dto.checklist() != null) {
            checklistService.syncChecklist(task, dto.checklist());
        }
        if (dto.dueDate() != null) {
            updateDueDate(task, dto.dueDate(), authUser);
        }

        if (dto.attachments() != null) {
            syncAttachments(task, dto.attachments());
        }

        Task savedTask = repository.saveAndFlush(task);
        return mappingService.mapEntityToDto(savedTask);

    }

    @Override
    public TaskResponseDto getById(String id, AppUser authUser) {
        Task task = getOrThrow(id);
        boolean canView = collaboratorService.hasUserPermission(
                authUser,
                task.getProject(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER, ProjectRoles.VIEWER)
        );

        if (!canView) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "You don't have access to view this task");
        }
        return mappingService.mapEntityToDto(task);
    }

    @Override
    public Task getOrThrow(String id) {
        return findById(id).orElseThrow(TaskNotFoundException::new);
    }

    @Override
    public Optional<Task> findById(String id) {
        return repository.findById(UUID.fromString(id));
    }

    @Override
    @Transactional
    @Auditable(
            entity = "Task",
            action = AuditLogAction.DELETE,
            nameField = "title",
            entityClass = Task.class
    )
    public void delete(String id, AppUser changer) {
        Task existedTask = getOrThrow(id);
        boolean hasPermission = collaboratorService.hasUserPermission(
                changer,
                existedTask.getProject(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "No delete permission");
        }
        Project project = existedTask.getProject();
        project.getTasks().remove(existedTask);
    }

    private void updateTitle(Task task, String newTitle, AppUser user) {
        if (newTitle != null && !newTitle.equals(task.getTitle())) {
            task.setTitle(newTitle);
        }
    }

    private void updateDescription(Task task, String newDesc, AppUser user) {
        if (newDesc != null && !newDesc.equals(task.getDescription())) {
            task.setDescription(newDesc);
        }
    }

    private void updateStatus(Task task, String statusId, AppUser user) {
        if (statusId != null) {
            UUID newStatusId = UUID.fromString(statusId);

            if (!task.getStatus().getId().equals(newStatusId)) {
                TaskStatus newStatus = taskStatusRepository.findById(newStatusId)
                        .orElseThrow(() -> new InvalidTaskPayloadException("Status not found"));

                task.setStatus(newStatus);
            }
        }
    }

    private void updateDueDate(Task task, String dueDateStr, AppUser user) {
        if (dueDateStr.isBlank()) {
            task.setDueDate(null);
            return;
        }

        try {
           LocalDateTime newDate = LocalDateTime.parse(dueDateStr);

            if (newDate.isBefore(LocalDateTime.now().minusMinutes(1))) {
                throw new InvalidTaskPayloadException("Deadline cannot be in the past!");
            }
            if (!newDate.equals(task.getDueDate())) {
                task.setDueDate(newDate);
            }
        } catch (DateTimeParseException e) {
            throw new InvalidTaskPayloadException("Invalid date format. Use ISO format (YYYY-MM-DDTHH:mm:ss)");
        }
    }

    private void syncAttachments(Task task, List<AttachmentResponseDto> dtos) {
        for (AttachmentResponseDto adto : dtos) {
            if (adto.id() == null || adto.id().trim().isEmpty())  {
                Attachment link = new Attachment();
                link.setName(adto.name());
                link.setUrl(adto.url());
                link.setType("LINK");
                link.setTask(task);
                task.getAttachments().add(link);
            }
        }
    }
}
