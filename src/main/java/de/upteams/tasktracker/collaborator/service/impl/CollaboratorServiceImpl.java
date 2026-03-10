 package de.upteams.tasktracker.collaborator.service.impl;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaboratorServiceImpl implements CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;
    private final ProjectRepository projectRepository;

    @Override
    public boolean isUserInProject(AppUser user, Project project) {
        return getCollaborator(user, project).isPresent();
    }

    @Override
    public Optional<Collaborator> getCollaborator(AppUser user, Project project) {
        return collaboratorRepository.findCollaborator(user, project);
    }

    @Override
    public boolean hasUserPermission(AppUser user, Project project, ProjectRoles requiredRole) {
        return hasUserPermission(user, project, Collections.singletonList(requiredRole));
    }

    @Override
    public boolean hasUserPermission(AppUser user, Project project, Collection<ProjectRoles> requiredRoles) {
        return getCollaborator(user, project)
                .map(collaborator -> hasAnyRequiredRole(collaborator, requiredRoles))
                .orElse(false);
    }

    @Override
    public Project checkAccessAndGetProject(AppUser user, String projectId, Collection<ProjectRoles> requiredRoles) {
        Project project = projectRepository.findByIdWithTeam(UUID.fromString(projectId))
                .orElseThrow(ProjectNotFoundException::new);

        boolean hasPermission = hasUserPermission(user, project, requiredRoles);

        if (!hasPermission) {
            throw new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You don't have permission to perform this action in project: " + project.getTitle());
        }

        return project;
    }


    @Override
    public Collaborator findById(UUID id) {
        return collaboratorRepository.findById(id)
                .orElseThrow(() -> new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Collaborator not found with id: " + id));
    }


    private boolean hasAnyRequiredRole(Collaborator collaborator, Collection<ProjectRoles> requiredRoles) {
        return collaborator.getProjectRolesSet()
                .stream()
                .anyMatch(requiredRoles::contains);
    }

    @Override
    public List<CollaboratorShortResponseDto> findByProjectId(String projectId, AppUser authUser) {

        List<Collaborator> collaborators = collaboratorRepository.findAllByProjectId(UUID.fromString(projectId));

        return collaborators.stream()
                .map(c -> new CollaboratorShortResponseDto(
                        c.getId().toString(),
                        c.getAppUser().getEmail(),
                        c.getProjectRolesSet()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void syncTaskExecutors(Task task, List<String> executorIds) {
        List<UUID> newUuids = executorIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .map(UUID::fromString)
                .toList();
        List<Collaborator> newExecutors = collaboratorRepository.findAllById(newUuids);
        task.getExecutors().clear();
        task.getExecutors().addAll(newExecutors);
    }


}
