package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Добавлено
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProjectController implements ProjectApi {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, @AuthenticationPrincipal AuthUserDetails principal) {
        // Добавлена аннотация @AuthenticationPrincipal выше
        return service.save(newProjectDto, principal.user());
    }

    @Override
    public ProjectResponseDto getById(String id) {
        return service.getById(id);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return service.getAll();
    }

    @Override
    public void deleteById(String id) {
        service.delete(id);
    }
}