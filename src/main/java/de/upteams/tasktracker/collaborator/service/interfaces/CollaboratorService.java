package de.upteams.tasktracker.collaborator.service.interfaces;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollaboratorService {

    boolean isUserInProject(AppUser user, Project project);

    Optional<Collaborator> getCollaborator(AppUser user, Project project);

    boolean hasUserPermission(AppUser user, Project project, ProjectRoles requiredRole);

    boolean hasUserPermission(AppUser user, Project project, Collection<ProjectRoles> requiredRoles);

    Project checkAccessAndGetProject(AppUser user, String projectId, Collection<ProjectRoles> requiredRoles);

    Collaborator findById(UUID id);

    List<CollaboratorShortResponseDto> findByProjectId(String projectId, AppUser authUser);

    void syncTaskExecutors(Task task, List<String> executorIds);

}
