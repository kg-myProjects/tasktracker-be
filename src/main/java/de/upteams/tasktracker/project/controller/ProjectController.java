package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.marker.dto.request.MarkerCreateDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.project.dto.request.InviteRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import de.upteams.tasktracker.project.dto.response.ProjectLogDto;

import java.util.List;
import java.util.UUID;

@RestController
public class ProjectController implements ProjectApi {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Override
    public ProjectResponseDto save(ProjectCreateDto dto, @AuthenticationPrincipal AuthUserDetails principal) {
        return service.save(dto, principal.user());
    }

    @Override
    public ProjectResponseDto getById(UUID id) {
        return service.getById(id);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return service.getAll();
    }

    @Override
    public List<TaskStatusResponseDto> getAllStatusByProjectId(UUID id) {
        return service.getAllStatusByProjectId(id);
    }

    @Override
    public List<TaskResponseDto> getAllTasksByProject(UUID id) {
        return service.getAllTasksByProject(id);
    }

    @Override
    public void deleteById(String id, @AuthenticationPrincipal AuthUserDetails principal) {
        service.delete(id, principal.user());
    }

    @Override
    public CollaboratorShortResponseDto inviteUser(InviteRequestDto inviteDto, UUID id, AuthUserDetails principal) {
     return    service.inviteUser(inviteDto, id);
    }

    @Override
    public List<MarkerResponseDto> getMarkersByProjectId(UUID projectId) {
        return service.getMarkersByProjectId(projectId);
    }

    @Override
    public List<ProjectLogDto> getProjectLogs(UUID id) {
        return service.getProjectLogs(id);
    }

    @Override
    public MarkerResponseDto createMarker(MarkerCreateDto dto, UUID projectId, @AuthenticationPrincipal AuthUserDetails principal) {
        return service.createMarker(dto, projectId, principal.user());
    }


}
