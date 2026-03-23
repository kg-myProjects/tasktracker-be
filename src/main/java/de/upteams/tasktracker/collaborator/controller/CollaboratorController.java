package de.upteams.tasktracker.collaborator.controller;

import de.upteams.tasktracker.collaborator.controller.api.CollaboratorApi;
import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.security.dto.AuthUserDetails;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CollaboratorController implements CollaboratorApi {

    private final TaskService taskService;
    private final CollaboratorService collaboratorService;

    @Override
    public List<CollaboratorShortResponseDto> getByProjectId(String projectId, AuthUserDetails principal) {
        return collaboratorService.findByProjectId(projectId, principal.user());
    }

}
