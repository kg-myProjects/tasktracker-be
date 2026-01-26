package de.upteams.tasktracker.taskstatus.service.impl;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {
    
    private final TaskStatusRepository repository;
    private final TaskStatusMappingService mappingService;
    private final ProjectRepository projectRepository;
    
    @Override
    @Transactional
    public TaskStatusResponseDto save(TaskStatusCreateDto dto) {
        Project project = projectRepository.findByIdWithTeam(UUID.fromString(dto.getProjectId())
        ).orElseThrow(() ->
                new InvalidTaskStatusPayloadEsception("Project of the tasks status not found")
        );
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new InvalidTaskStatusPayloadEsception("Invalid tasks status payload");
        }
        repository.shiftPositionsForward(project.getId(), dto.getPosition());
        repository.flush();

        TaskStatus taskStatus =new TaskStatus();
      taskStatus.setName(dto.getName());
      taskStatus.setPosition(dto.getPosition());
      taskStatus.setProject(project);
      TaskStatus saved = repository.save(taskStatus);
        return mappingService.mapEntityToStatusDto(saved);
    }

    @Override
    @Transactional
    public TaskStatusResponseDto update(TaskStatusUpdateDto dto) {
        String id = dto.getId();
        TaskStatus taskStatus= findById(id).orElseThrow(TaskStatusNotFoundException::new);
        if (dto.getPosition() != null){
            Integer oldPosition = taskStatus.getPosition();
            Integer newPosition = dto.getPosition();

            if (!oldPosition.equals(newPosition)) {
                repository.shiftPositionsForward(taskStatus.getProject().getId(), newPosition);
                taskStatus.setPosition(newPosition);
            }
        }
        return mappingService.mapEntityToStatusDto(repository.save(taskStatus));
    }

    @Override
    public TaskStatusResponseDto getById(String id) {
        return mappingService.mapEntityToStatusDto(getOrThrow(id));
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
    public void delete(String id, AppUser changer) {
      repository.deleteById(UUID.fromString(id));
    }
}
