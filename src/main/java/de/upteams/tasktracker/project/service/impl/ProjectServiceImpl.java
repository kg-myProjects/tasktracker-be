package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.InvalidProjectPayloadException;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.persistence.TaskStatusRepository;
import de.upteams.tasktracker.taskstatus.utils.TaskStatusMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for various operations with Projects
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMappingService taskStatusMappingService;
    private final TaskRepository taskRepository;
    private final TaskMappingService taskMappingService;
    private final ProjectMapper mappingService;

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner) {
        if (newProjectDto.title() == null || newProjectDto.title().isBlank()) {
            throw new InvalidProjectPayloadException("Invalid project payload");
        }
        if (newProjectDto.description() == null || newProjectDto.description().isBlank()) {
            throw new InvalidProjectPayloadException("Invalid project payload");
        }

        Project project = mappingService.mapDtoToEntity(newProjectDto);
        project.setOwner(projectOwner);
        return mappingService.mapEntityToDto(repository.save(project));
    }

    @Override
    public ProjectResponseDto getById(UUID id) {
        return mappingService.mapEntityToDto(getOrTrow(id));
    }

    @Override
    public Project getOrTrow(UUID id) {
        return repository
                .findById(id)
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public List<TaskStatusResponseDto> getAllStatusByProjectId(UUID id) {
        return taskStatusRepository.findByProjectId(id)
                .stream()
                .map(taskStatusMappingService::mapEntityToStatusDto)
                .toList();
    }

    @Override
    public List<TaskResponseDto> getAllTasksByProject(UUID id) {

        final Project project = getOrTrow(id);
        //       boolean userInProject = collaboratorService.isUserInProject(authUser, project);
//        if (!userInProject) {
//            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
//        }
        return taskRepository
                .findByProject(project)
                .stream()
                .map(taskMappingService::mapEntityToDto)
                .toList();

    }

    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
    }
}
