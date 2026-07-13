package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.BaseControllerTest;
import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.marker.dto.request.MarkerCreateDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.project.dto.request.InviteRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectLogDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ProjectController.class)

class ProjectControllerTest extends BaseControllerTest {

    @MockitoBean
    private ProjectService projectService;

    private final String projectId = UUID.randomUUID().toString();

    @Test
    @DisplayName("POST /api/v1/projects - Success (Created)")
    void saveProjectSuccess() throws Exception {
        ProjectCreateDto request = new ProjectCreateDto("New Project", "Description");
        ProjectResponseDto response = new ProjectResponseDto(projectId, "New Project", "Description", null, null, null);

        when(projectService.save(any(), any())).thenReturn(response);

        performPost("/api/v1/projects", request, mockUserPrincipal)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Project"));
    }

    @Test
    @DisplayName("POST /api/v1/projects - Unauthorized")
    void saveProjectUnauthorized() throws Exception {
        ProjectCreateDto request = new ProjectCreateDto("Title", "Desc");

        performPost("/api/v1/projects", request, null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/v1/projects/{id} - Success (OK)")
    void updateProjectSuccess() throws Exception {
        UUID id = UUID.fromString(projectId);
        ProjectCreateDto request = new ProjectCreateDto("Updated Title", "Updated Desc");
        ProjectResponseDto response = new ProjectResponseDto(projectId, "Updated Title", "Updated Desc", null, null, null);

        when(projectService.update(eq(id), any(), any())).thenReturn(response);

        performPatch("/api/v1/projects/" + projectId, request, mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("PATCH /api/v1/projects/{id} - Bad Request (Validation Fail)")
    void updateProjectBadRequest() throws Exception {
        ProjectCreateDto invalidRequest = new ProjectCreateDto("", "Description");

        performPatch("/api/v1/projects/" + projectId, invalidRequest, mockUserPrincipal)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} - Success")
    void getProjectByIdSuccess() throws Exception {
        UUID id = UUID.fromString(projectId);
        ProjectResponseDto response = new ProjectResponseDto(projectId, "Found Project", "Description", null, null, null);

        when(projectService.getById(id)).thenReturn(response);

        performGet("/api/v1/projects/" + projectId, mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.title").value("Found Project"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} - Not Found")
    void getProjectByIdNotFound() throws Exception {
        UUID id = UUID.fromString(projectId);

        when(projectService.getById(id))
                .thenThrow(new ProjectNotFoundException());

        performGet("/api/v1/projects/" + projectId, mockUserPrincipal)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/v1/projects - Success (List)")
    void getAllProjectsSuccess() throws Exception {
        ProjectResponseDto p1 = new ProjectResponseDto(UUID.randomUUID().toString(), "Project 1", "Desc 1", null, null, null);
        ProjectResponseDto p2 = new ProjectResponseDto(UUID.randomUUID().toString(), "Project 2", "Desc 2", null, null, null);
        List<ProjectResponseDto> projects = List.of(p1, p2);

        when(projectService.getAll()).thenReturn(projects);

        performGet("/api/v1/projects", mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Project 1"))
                .andExpect(jsonPath("$[1].title").value("Project 2"));
    }

    @Test
    @DisplayName("GET /api/v1/projects - Empty List")
    void getAllProjectsEmpty() throws Exception {
        when(projectService.getAll()).thenReturn(List.of());

        performGet("/api/v1/projects", mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/projects/my - Success")
    void getMyProjectsSuccess() throws Exception {
        ProjectResponseDto p1 = new ProjectResponseDto(UUID.randomUUID().toString(), "My Own Project", "Owner", null, null, null);
        ProjectResponseDto p2 = new ProjectResponseDto(UUID.randomUUID().toString(), "Collaborator Project", "Collaborator", null, null, null);
        List<ProjectResponseDto> myProjects = List.of(p1, p2);

        when(projectService.getMyProjects(any())).thenReturn(myProjects);

        performGet("/api/v1/projects/my", mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("My Own Project"))
                .andExpect(jsonPath("$[1].title").value("Collaborator Project"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/my - Unauthorized")
    void getMyProjectsUnauthorized() throws Exception {
        performGet("/api/v1/projects/my", null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id}/status - Success")
    void getAllStatusByProjectIdSuccess() throws Exception {
        UUID projId = UUID.fromString(projectId);

        TaskStatusResponseDto status = new TaskStatusResponseDto(
                UUID.randomUUID().toString(),
                "To Do",
                1,
                projectId
        );

        when(projectService.getAllStatusByProjectId(eq(projId)))
                .thenReturn(List.of(status));

        performGet("/api/v1/projects/" + projectId + "/status", mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("To Do"))
                .andExpect(jsonPath("$.[0].projectId").value(projectId))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id}/status - Project Not Found")
    void getAllStatusByProjectIdNotFound() throws Exception {
        UUID id = UUID.fromString(projectId);


        when(projectService.getAllStatusByProjectId(eq(id)))
                .thenThrow(new ProjectNotFoundException());

        performGet("/api/v1/projects/" + projectId + "/status", mockUserPrincipal)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id}/tasks - Success")
    void getAllTasksByProjectSuccess() throws Exception {
        UUID id = UUID.fromString(projectId);
        String mockStatusId = UUID.randomUUID().toString();
        TaskResponseDto task = TaskResponseDto.builder()
                .id(UUID.randomUUID().toString())
                .title("Task Title")
                .description("Description")
                .statusId(mockStatusId)
                .projectId(projectId)
                .executors(new HashSet<>())
                .markers(new HashSet<>())
                .checklist(List.of())
                .dueDate("2025-12-31")
                .attachments(List.of())
                .build();

        List<TaskResponseDto> tasks = List.of(task);

        when(projectService.getAllTasksByProject(eq(id))).thenReturn(tasks);

        performGet("/api/v1/projects/" + projectId + "/tasks", mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task Title"))
                .andExpect(jsonPath("$[0].projectId").value(projectId))
                .andExpect(jsonPath("$[0].statusId").value(mockStatusId));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id}/tasks - Forbidden")
    void getAllTasksByProjectForbidden() throws Exception {
        UUID id = UUID.fromString(projectId);

        when(projectService.getAllTasksByProject(eq(id)))
                .thenThrow(new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "Access denied"));

        performGet("/api/v1/projects/" + projectId + "/tasks", mockUserPrincipal)
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("DELETE /api/v1/projects/{id} - Success")
    void deleteProjectSuccess() throws Exception {
        UUID id = UUID.fromString(projectId);

        doNothing().when(projectService).delete(eq(id), any());

        performDelete("/api/v1/projects/" + projectId, mockUserPrincipal)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{id} - Not Found")
    void deleteProjectNotFound() throws Exception {
        UUID id = UUID.fromString(projectId);

        doThrow(new ProjectNotFoundException())
                .when(projectService).delete(eq(id), any());

        performDelete("/api/v1/projects/" + projectId, mockUserPrincipal)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/invite - Success")
    void inviteUserSuccess() throws Exception {
        UUID projId = UUID.fromString(projectId);

        InviteRequestDto inviteDto = new InviteRequestDto("new-user@example.com", ProjectRoles.MEMBER);

        CollaboratorShortResponseDto response = new CollaboratorShortResponseDto(
                UUID.randomUUID().toString(),
                "new-user@example.com",
                null,
                null,
                Set.of(ProjectRoles.MEMBER)
        );

        when(projectService.inviteUser(any(InviteRequestDto.class), eq(projId), any()))
                .thenReturn(response);

        performPost("/api/v1/projects/" + projectId + "/invite", inviteDto, mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new-user@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("MEMBER"));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/invite - Bad Request (Invalid Email)")
    void inviteUserBadRequest() throws Exception {
        InviteRequestDto invalidDto = new InviteRequestDto("not-an-email", ProjectRoles.MEMBER);

        performPost("/api/v1/projects/" + projectId + "/invite", invalidDto, mockUserPrincipal)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/invite - Conflict (User already in project)")
    void inviteUserConflict() throws Exception {
        UUID projId = UUID.fromString(projectId);
        InviteRequestDto inviteDto = new InviteRequestDto("member@test.com", ProjectRoles.MEMBER);

        when(projectService.inviteUser(any(InviteRequestDto.class), eq(projId), any()))
                .thenThrow(new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                        org.springframework.http.HttpStatus.CONFLICT, "User already a collaborator"));

        performPost("/api/v1/projects/" + projectId + "/invite", inviteDto, mockUserPrincipal)
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id}/markers - Success")
    void getMarkersByProjectIdSuccess() throws Exception {
        UUID projId = UUID.fromString(projectId);

        MarkerResponseDto m1 = new MarkerResponseDto(
                UUID.randomUUID().toString(),
                "Urgent",
                "#FF0000",
                projectId
        );
        MarkerResponseDto m2 = new MarkerResponseDto(
                UUID.randomUUID().toString(),
                "Backend",
                "#00FF00",
                projectId
        );
        List<MarkerResponseDto> markers = List.of(m1, m2);

        when(projectService.getMarkersByProjectId(eq(projId))).thenReturn(markers);

        performGet("/api/v1/projects/" + projectId + "/markers", mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Urgent"))
                .andExpect(jsonPath("$[0].color").value("#FF0000"))
                .andExpect(jsonPath("$[0].projectId").value(projectId))
                .andExpect(jsonPath("$[1].name").value("Backend"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id}/markers - Not Found")
    void getMarkersByProjectIdNotFound() throws Exception {
        UUID projId = UUID.fromString(projectId);

        when(projectService.getMarkersByProjectId(eq(projId)))
                .thenThrow(new ProjectNotFoundException());

        performGet("/api/v1/projects/" + projectId + "/markers", mockUserPrincipal)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/markers - Success")
    void createMarkerSuccess() throws Exception {
        UUID projId = UUID.fromString(projectId);

        MarkerCreateDto request = new MarkerCreateDto("Urgent", "#FF0000",projectId);

        MarkerResponseDto response = new MarkerResponseDto(
                UUID.randomUUID().toString(),
                "Urgent",
                "#FF0000",
                projectId
        );

        when(projectService.createMarker(any(MarkerCreateDto.class), eq(projId), any()))
                .thenReturn(response);

        performPost("/api/v1/projects/" + projectId + "/markers", request, mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Urgent"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/markers - Bad Request (Empty Name)")
    void createMarkerBadRequest() throws Exception {
        MarkerCreateDto invalidRequest = new MarkerCreateDto("", "#FF0000",projectId);

        performPost("/api/v1/projects/" + projectId + "/markers", invalidRequest, mockUserPrincipal)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{projectId}/markers/{markerId} - Success")
    void deleteMarkerSuccess() throws Exception {
        UUID projId = UUID.fromString(projectId);
        UUID markerId = UUID.randomUUID();

        doNothing().when(projectService).deleteMarker(eq(projId), eq(markerId), any());

        performDelete("/api/v1/projects/" + projectId + "/markers/" + markerId, mockUserPrincipal)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{projectId}/markers/{markerId} - Not Found")
    void deleteMarkerNotFound() throws Exception {
        UUID projId = UUID.fromString(projectId);
        UUID markerId = UUID.randomUUID();

        doThrow(new ProjectNotFoundException())
                .when(projectService).deleteMarker(eq(projId), eq(markerId), any());

        performDelete("/api/v1/projects/" + projectId + "/markers/" + markerId, mockUserPrincipal)
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("GET /api/v1/projects/{id}/logs - Success")
    void getProjectLogsSuccess() throws Exception {
        UUID logId = UUID.randomUUID();
        UUID projId = UUID.fromString(projectId);
        Instant now = Instant.now();

        ProjectLogDto log = new ProjectLogDto(
                logId,
                "Project",
                "Project Alpha",
                "UPDATE",
                "user@test.com",
                "John",
                "Doe",
                "avatar-url",
                null,
                "Changed title from A to B",
                now
        );

        List<ProjectLogDto> logs = List.of(log);

        when(projectService.getProjectLogs(eq(projId))).thenReturn(logs);

        performGet("/api/v1/projects/" + projectId + "/logs", mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entity").value("Project"))
                .andExpect(jsonPath("$[0].action").value("UPDATE"))
                .andExpect(jsonPath("$[0].userEmail").value("user@test.com"))
                .andExpect(jsonPath("$[0].difference").value("Changed title from A to B"))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }
}
