package de.upteams.tasktracker.project.service.interfaces;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.project.dto.request.InviteRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
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

    ProjectResponseDto getById(UUID id);

    Project getOrTrow(UUID id);

    List<ProjectResponseDto> getAll();

    List<TaskStatusResponseDto> getAllStatusByProjectId(UUID id);
    List<TaskResponseDto> getAllTasksByProject(UUID id);


    void delete(String id);

    CollaboratorShortResponseDto inviteUser(InviteRequestDto inviteDto, UUID projectId);

}
