package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.utils.CollaboratorMapper;
import de.upteams.tasktracker.project.dto.request.InviteRequestDto;
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
import de.upteams.tasktracker.user.exception.UserNotFoundException;
import de.upteams.tasktracker.user.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.HashSet;
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
    private final CollaboratorRepository collaboratorRepository;
    private final UserRepository userRepository;
    private final CollaboratorMapper collaboratorMapper;

    @Override
    @Transactional
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner) {
        if (!StringUtils.hasText(newProjectDto.title()) || !StringUtils.hasText(newProjectDto.description())) {
            throw new InvalidProjectPayloadException("Title and description are required");
        }

        Project project = mappingService.mapDtoToEntity(newProjectDto);
        project.setOwner(projectOwner);
        if (project.getProjectTeam() == null) {
            project.setProjectTeam(new HashSet<>());
        }

        Project savedProject = repository.save(project);

        Collaborator owner = new Collaborator();
        owner.setAppUser(projectOwner);
        owner.setProject(savedProject);
        owner.getProjectRolesSet().add(ProjectRoles.OWNER);
        savedProject.getProjectTeam().add(owner);
        collaboratorRepository.save(owner);
        return mappingService.mapEntityToDto(savedProject);
    }

    @Override
    public ProjectResponseDto getById(UUID id) {
        return mappingService.mapEntityToDto(getOrTrow(id));
    }

    @Override
    public Project getOrTrow(UUID id) {
        return repository
                .findByIdWithTeam(id)
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return repository
                .findAllWithTeam()
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

    @Override
    public CollaboratorShortResponseDto inviteUser(InviteRequestDto inviteDto, UUID projectId) {
        if (inviteDto.role() == ProjectRoles.OWNER) {
            throw new IllegalArgumentException("Cannot invite another OWNER");
        }
        Project project = repository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        AppUser userToInvite = userRepository.findByEmailIgnoreCase(inviteDto.email())
                .orElseThrow(UserNotFoundException::new);

        boolean alreadyMember = collaboratorRepository.existsByProjectAndAppUser(project, userToInvite);
        if (alreadyMember) {
            throw new IllegalStateException("User is already a collaborator in this project");
        }

        Collaborator collaborator = new Collaborator();
        collaborator.setAppUser(userToInvite);
        collaborator.setProject(project);
        collaborator.getProjectRolesSet().add(inviteDto.role());

        Collaborator saved = collaboratorRepository.save(collaborator);
        return collaboratorMapper.mapEntityToShortDto(saved);
    }
}
