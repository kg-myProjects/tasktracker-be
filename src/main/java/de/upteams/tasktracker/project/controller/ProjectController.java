package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller that receives http-requests for various operations with Projects
 */
@RestController
public class ProjectController implements ProjectApi {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AuthUserDetails principal) {
        return service.save(newProjectDto, principal.user());
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
    public void deleteById(String id) {
        service.delete(id);
    }
}
