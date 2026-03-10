package de.upteams.tasktracker.taskstatus.service.impl;

import de.upteams.tasktracker.audit.annotation.Auditable;
import de.upteams.tasktracker.audit.utils.AuditLogAction;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusUpdateDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.taskstatus.exception.InvalidTaskStatusPayloadEsception;
import de.upteams.tasktracker.taskstatus.exception.TaskStatusNotFoundException;
import de.upteams.tasktracker.taskstatus.persistence.TaskStatusRepository;
import de.upteams.tasktracker.taskstatus.service.interfaces.TaskStatusService;
import de.upteams.tasktracker.taskstatus.utils.TaskStatusMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskStatusRepository repository;
    private final TaskStatusMappingService mappingService;
    private final CollaboratorService collaboratorService;

    @Override
    @Transactional
    @Auditable(entity = "Status", action = AuditLogAction.CREATE)
    public TaskStatusResponseDto save(TaskStatusCreateDto dto, AppUser authUser) {
        Project project = collaboratorService.checkAccessAndGetProject(
                authUser, dto.getProjectId(), List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN));

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new InvalidTaskStatusPayloadEsception("Invalid tasks status payload");
        }
        repository.shiftPositionsForward(project.getId(), dto.getPosition());
        repository.flush();

        TaskStatus taskStatus = mappingService.mapDtoToEntity(dto);

        taskStatus.setProject(project);

        TaskStatus saved = repository.save(taskStatus);
        return mappingService.mapEntityToStatusDto(saved);
    }

    @Override
    @Transactional
    public TaskStatusResponseDto update(TaskStatusUpdateDto dto, AppUser authUser) {
        TaskStatus taskStatus = findById(dto.getId())
                .orElseThrow(TaskStatusNotFoundException::new);
        String projectId = taskStatus.getProject().getId().toString();

         collaboratorService.checkAccessAndGetProject(
                authUser, projectId, List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN));

        if (dto.getName() != null && dto.getName().isBlank()) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Status name cannot be empty");
        }
        mappingService.updateEntityFromDto(dto, taskStatus);
        return mappingService.mapEntityToStatusDto(repository.save(taskStatus));
    }

    @Override
    @Transactional
    public List<TaskStatusResponseDto> updateTaskStatusesOrder(List<TaskStatusUpdateDto> dtos, AppUser authUser) {
        if (dtos.isEmpty()) return Collections.emptyList();

        List<UUID> ids = dtos.stream()
                .map(TaskStatusUpdateDto::getId)
                .map(UUID::fromString)
                .toList();

        List<TaskStatus> statuses = repository.findAllById(ids);

        if (statuses.isEmpty()) return Collections.emptyList();

        Map<String, Integer> idToNewPosition = dtos.stream()
                .collect(Collectors.toMap(TaskStatusUpdateDto::getId, TaskStatusUpdateDto::getPosition));

        statuses.forEach(status -> {
            Integer newPosition = idToNewPosition.get(status.getId().toString());
            if (newPosition != null && !newPosition.equals(status.getPosition())) {
                status.setPosition(newPosition);
            }
        });

        repository.saveAll(statuses);

        UUID projectId = statuses.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No task statuses found"))
                .getProject()
                .getId();
        return repository.findByProjectIdOrderByPositionAsc(projectId).stream()
                .map(mappingService::mapEntityToStatusDto)
                .toList();
    }

    @Override
    public TaskStatus getOrThrow(String id) {
        return findById(id).orElseThrow(TaskStatusNotFoundException::new);
    }

    @Override
    public Optional<TaskStatus> findById(String id) {
        return repository.findById(UUID.fromString(id));
    }

    @Override
    @Transactional
    @Auditable(
            entity = "Status",
            action = AuditLogAction.DELETE,
            nameField = "name",
            entityClass = TaskStatus.class
    )
    public void delete(String id, AppUser changer) {
        TaskStatus status = repository.findById(UUID.fromString(id))
                .orElseThrow(TaskStatusNotFoundException::new);


        Project project = collaboratorService.checkAccessAndGetProject(
                changer,
                status.getProject().getId().toString(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );

        if (project != null) {
            project.getTaskStatuses().remove(status);
        }

        repository.delete(status);
    }


}
