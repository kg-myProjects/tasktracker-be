package de.upteams.tasktracker.project.service.interfaces;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.marker.dto.request.MarkerCreateDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.project.dto.request.InviteRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectLogDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.UUID;

/**
 * Service for various operations with Projects
 */
public interface ProjectService {

    ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner);
    ProjectResponseDto update(UUID id, ProjectCreateDto newProjectDto, AppUser authUser);

    ProjectResponseDto getById(UUID id);

    Project getOrTrow(UUID id);

    List<ProjectResponseDto> getAll();

    List<ProjectResponseDto> getMyProjects(AppUser authUser);

    List<TaskStatusResponseDto> getAllStatusByProjectId(UUID id);

    List<TaskResponseDto> getAllTasksByProject(UUID id);

    List<MarkerResponseDto> getMarkersByProjectId(UUID id);

    List<ProjectLogDto> getProjectLogs(UUID projectId);

    void delete(UUID id, AppUser projectOwner);

    CollaboratorShortResponseDto inviteUser(InviteRequestDto inviteDto, UUID projectId, AppUser inviter);

    MarkerResponseDto createMarker(MarkerCreateDto dto, UUID projectId, AppUser authUser);
    void deleteMarker(UUID projectId, UUID markerId, AppUser authUser);

}
