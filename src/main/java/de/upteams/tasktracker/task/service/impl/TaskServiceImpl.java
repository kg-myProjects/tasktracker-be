package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
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

    @Override
    @Transactional
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

        return mappingService.mapEntityToDto(repository.save(task));
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
        repository.delete(existedTask);
    }


    @Override
    @Transactional
    public TaskResponseDto addExecutor(String taskId, String collaboratorId, AppUser authUser) {
        Task task = getOrThrow(taskId);

        boolean hasPermission = collaboratorService.hasUserPermission(
                authUser, task.getProject(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER));

        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "No permission to assign executors");
        }

        Collaborator collaborator = collaboratorService.findById(UUID.fromString(collaboratorId));

        if (!collaborator.getProject().getId().equals(task.getProject().getId())) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Collaborator belongs to another project");
        }

        task.getExecutors().add(collaborator);

        return mappingService.mapEntityToDto(repository.save(task));
    }


}
