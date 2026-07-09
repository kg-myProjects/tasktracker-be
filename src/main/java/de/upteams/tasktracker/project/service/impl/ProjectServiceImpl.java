package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.audit.persistence.AuditLogRepository;
import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.collaborator.utils.CollaboratorMapper;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.marker.dto.request.MarkerCreateDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.marker.entity.Marker;
import de.upteams.tasktracker.marker.persistence.MarkerRepository;
import de.upteams.tasktracker.marker.utils.MarkerMapper;
import de.upteams.tasktracker.project.dto.request.InviteRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectLogDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
    private final CollaboratorService collaboratorService;
    private final UserRepository userRepository;
    private final CollaboratorMapper collaboratorMapper;
    private final MarkerRepository markerRepository;
    private final MarkerMapper markerMapper;
    private final AuditLogRepository auditLogRepository;

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
    @Transactional
    public ProjectResponseDto update(UUID id,ProjectCreateDto dto, AppUser authUser) {
        Project project = collaboratorService.checkAccessAndGetProject(
                authUser,
                id.toString(),
                List.of(ProjectRoles.OWNER)
        );

        if (dto.title() != null) updateTitle(project, dto.title());
        if (dto.description() != null) updateDescription(project, dto.description());
        return mappingService.mapEntityToDto(repository.save(project));
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
    public List<ProjectResponseDto> getMyProjects(AppUser authUser) {
        return repository.findAllForUser(authUser.getId())
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
        return taskRepository
                .findByProject(project)
                .stream()
                .map(taskMappingService::mapEntityToDto)
                .toList();

    }

    @Override
    public List<MarkerResponseDto> getMarkersByProjectId(UUID id) {
        return markerRepository
                .findAllByProjectId(id)
                .stream()
                .map(markerMapper::mapEntityToDto)
                .toList();
    }

    @Override
    public List<ProjectLogDto> getProjectLogs(UUID projectId) {
        return auditLogRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId.toString())
                .stream()
                .map(log -> new ProjectLogDto(
                        log.getId(),
                        log.getEntity(),
                        log.getEntityName(),
                        log.getAction(),
                        log.getUserEmail(),
                        log.getUserFirstName(),
                        log.getUserLastName(),
                        log.getUserAvatar(),
                        log.getUserAvatarUpdatedAt(),
                        log.getDifference(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id, AppUser projectOwner) {
        Project project = collaboratorService.checkAccessAndGetProject(
                projectOwner,
                id.toString(),
                List.of(ProjectRoles.OWNER)
        );

        project.getTasks().forEach(task -> {
            task.getExecutors().clear();
            task.getMarkers().clear();
        });

        project.getTaskStatuses().clear();
        project.getTasks().clear();
        project.getProjectTeam().clear();
        project.getMarkers().clear();

        auditLogRepository.deleteAllByProjectId(project.getId().toString());
        repository.saveAndFlush(project);
        repository.delete(project);
    }

    @Override
    public CollaboratorShortResponseDto inviteUser(InviteRequestDto inviteDto, UUID projectId, AppUser inviter) {
        if (inviteDto.role() == ProjectRoles.OWNER) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Cannot invite another OWNER");
        }
        Project project = collaboratorService.checkAccessAndGetProject(
                inviter,
                projectId.toString(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        AppUser userToInvite = userRepository.findByEmailIgnoreCase(inviteDto.email())
                .orElseThrow(UserNotFoundException::new);

        boolean alreadyMember = collaboratorRepository.existsByProjectAndAppUser(project, userToInvite);
        if (alreadyMember) {
            throw new RestApiException(HttpStatus.CONFLICT, "User is already a collaborator in this project");
        }

        Collaborator collaborator = new Collaborator();
        collaborator.setAppUser(userToInvite);
        collaborator.setProject(project);
        collaborator.getProjectRolesSet().add(inviteDto.role());

        Collaborator saved = collaboratorRepository.save(collaborator);
        return collaboratorMapper.mapEntityToShortDto(saved);
    }


    @Override
    @Transactional
    public MarkerResponseDto createMarker(MarkerCreateDto dto, UUID projectId, AppUser authUser) {
        Project project = collaboratorService.checkAccessAndGetProject(
                authUser,
                projectId.toString(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER, ProjectRoles.VIEWER)
        );

        Marker marker = markerMapper.mapDtoToEntity(dto);
        marker.setProject(project);
        Marker savedMarker = markerRepository.save(marker);
        return markerMapper.mapEntityToDto(savedMarker);
    }

    @Override
    @Transactional
    public void deleteMarker(UUID projectId, UUID markerId, AppUser authUser) {
        collaboratorService.checkAccessAndGetProject(
                authUser,
                projectId.toString(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        Marker marker = markerRepository.findById(markerId)
                .orElseThrow(() -> new RestApiException(HttpStatus.NOT_FOUND, "Marker not found"));

        if (!marker.getProject().getId().equals(projectId)) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "Marker does not belong to this project");
        }
        if (marker.getTasks() != null) {
            marker.getTasks().forEach(task -> task.getMarkers().remove(marker));
        }
        markerRepository.delete(marker);
    }


    private void updateTitle(Project project, String newTitle) {
        if (newTitle != null && !newTitle.equals(project.getTitle())) {
            project.setTitle(newTitle);
        }
    }

    private void updateDescription(Project project, String newDesc) {
        if (newDesc != null && !newDesc.equals(project.getDescription())) {
            project.setDescription(newDesc);
        }
    }


}
