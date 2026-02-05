package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.audit.annotation.Auditable;
import de.upteams.tasktracker.audit.utils.AuditLogAction;
import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.marker.entity.Marker;
import de.upteams.tasktracker.marker.persistence.MarkerRepository;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.ChecklistItem;
import de.upteams.tasktracker.task.entity.Task;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final TaskStatusRepository taskStatusRepository;
    private final ProjectRepository projectRepository;
    private final MarkerRepository markerRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final TaskMappingService mappingService;
    private final CollaboratorService collaboratorService;

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
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        task.setStatus(status);
        task.setProject(project);

        return mappingService.mapEntityToDto(repository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDto update(String id, TaskCreateDto dto, AppUser authUser) {
        Task task = getOrThrow(id);
        boolean hasPermission = collaboratorService.hasUserPermission(
                authUser,
                task.getProject(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER)
        );

        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "You don't have permission to modify tasks in this project");
        }
        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getStatusId() != null) {
            TaskStatus status = taskStatusRepository.findById(UUID.fromString(dto.getStatusId()))
                    .orElseThrow(() -> new InvalidTaskPayloadException("Status not found"));
            task.setStatus(status);
        }
        if (dto.getExecutorIds() != null) {
            List<UUID> executorUuids = dto.getExecutorIds().stream()
                    .filter(Objects::nonNull)
                    .filter(idp -> !idp.isBlank())
                    .map(UUID::fromString).toList();

            List<Collaborator> collaborators = collaboratorRepository.findAllById(executorUuids);

            task.getExecutors().clear();
            task.getExecutors().addAll(collaborators);
        }

        if (dto.getMarkerIds() != null) {
            List<UUID> markerUuids = dto.getMarkerIds().stream()
                    .filter(Objects::nonNull)
                    .filter(idStr -> !idStr.isBlank())
                    .map(UUID::fromString).toList();

            List<Marker> markers = markerRepository.findAllById(markerUuids);

            task.getMarkers().clear();
            task.getMarkers().addAll(markers);
        }

        if (dto.getChecklist() != null) {
            List<ChecklistItem> currentChecklist = task.getChecklist();

            List<ChecklistItem> updatedItems = dto.getChecklist().stream()
                    .map(itemDto -> {
                        ChecklistItem item;
                        if (itemDto.id() != null && !itemDto.id().isBlank()) {

                            item = currentChecklist.stream()
                                    .filter(existing -> existing.getId().toString().equals(itemDto.id()))
                                    .findFirst()
                                    .orElse(new ChecklistItem());
                        } else {
                            item = new ChecklistItem();
                        }

                        item.setText(itemDto.text());
                        item.setCompleted(itemDto.completed());
                        item.setTask(task);
                        return item;
                    }).toList();

            currentChecklist.clear();
            currentChecklist.addAll(updatedItems);
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

}
