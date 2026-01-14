package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.dto.TaskDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.exception.TaskNotFoundException;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for various operations with Tasks
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final TaskMappingService mappingService;
    private final ProjectService projectService;
    private final CollaboratorService collaboratorService;

    @Override
    public TaskDto save(final TaskDto newTaskDto, final AppUser authUser) {
        final Task entity = mappingService.mapDtoToEntity(newTaskDto);

        // Исправление бага: проверяем доступ пользователя к проекту этой задачи
        if (entity.getProject() != null) {
            boolean userInProject = collaboratorService.isUserInProject(authUser, entity.getProject());
            if (!userInProject) {
                throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
            }
        }

        return mappingService.mapEntityToDto(repository.save(entity));
    }

    @Override
    public TaskDto getById(final String id, final AppUser authUser) {
        final Task task = getOrThrow(id);

        // Исправление бага: проверяем, имеет ли право пользователь видеть эту задачу
        boolean userInProject = collaboratorService.isUserInProject(authUser, task.getProject());
        if (!userInProject) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }

        return mappingService.mapEntityToDto(task);
    }

    @Override
    public Task getOrThrow(String id) {
        return findById(id)
                .orElseThrow(TaskNotFoundException::new);
    }

    @Override
    public Optional<Task> findById(String id) {
        return repository
                .findById(UUID.fromString(id));
    }

    @Override
    public List<TaskDto> getAll(final String projectId, final AppUser authUser) {
        final Project project = projectService.getOrTrow(projectId);
        boolean userInProject = collaboratorService.isUserInProject(authUser, project);
        if (!userInProject) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
        return repository
                .findByProject(project)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public void delete(final String id, final AppUser changer) {
        final Task existedTask = getOrThrow(id);
        final boolean hasPermission = collaboratorService.hasUserPermission(
                changer,
                existedTask.getProject(),
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
        repository.delete(existedTask);
    }
}