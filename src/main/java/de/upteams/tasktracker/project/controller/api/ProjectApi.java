package de.upteams.tasktracker.project.controller.api;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.marker.dto.request.MarkerCreateDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.project.dto.request.InviteRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Project API description for Swagger
 */
@Tag(name = "Project controller", description = "Controller for various operations with Projects")
@RequestMapping("/api/v1/projects")
@PreAuthorize("isAuthenticated()")
public interface ProjectApi {

    @Operation(summary = "Save/create Project", description = "Save new Project to the Database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "7",
                                      "title": "New Website Development",
                                      "description": "A Project to develop a new company website",
                                      "owner": {
                                        "id": "42",
                                        "email": "tes_dev@upteams.de",
                                        "firstName": "Test",
                                        "lastName": "Dev"
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid project payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "title", "messages": ["must not be blank"] }
                                    ]
                                    """))
            )
    })
    @PostMapping
    ProjectResponseDto save(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Instance of Project to save"
            )
            @Valid
            ProjectCreateDto newProjectDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get Project", description = "Get one Project from the Database by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponseDto.class)))
            ,
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Project not found with id: 7",
                                      "path": "/api/v1/projects/7"
                                    }
                                    """)))
    })
    @GetMapping("/{id}")
    ProjectResponseDto getById(
            @PathVariable
            @Parameter(required = true, description = "Project ID to search")
            UUID id
    );

    @Operation(summary = "Get all Projects", description = "Get all Projects from the Database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Projects list",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProjectResponseDto.class))))
    })
    @GetMapping
    List<ProjectResponseDto> getAll();

    @Operation(summary = "Get all Tasks Status for Project", description = "Retrieves all tasks Status for Project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks status for Project",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskStatusCreateDto.class))))
    })
    @GetMapping("/{id}/status")
    List<TaskStatusResponseDto> getAllStatusByProjectId(
            @PathVariable
            @Parameter(required = true, description = "Project ID to search")
            UUID id);

    @Operation(summary = "Get all Tasks for Project", description = "Retrieves all tasks under a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskCreateDto.class))))
            ,
            @ApiResponse(responseCode = "403", description = "Forbidden - user has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/{id}/tasks")
    List<TaskResponseDto> getAllTasksByProject(
            @PathVariable
            UUID id

//            @AuthenticationPrincipal
//            @Parameter(hidden = true)
//            AuthUserDetails principal
    );



    @Operation(summary = "Delete Project", description = "Delete Project from the Database by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Project not found with id: 7",
                                      "path": "/api/v1/projects/7"
                                    }
                                    """)))
    })
    @DeleteMapping("/{id}")
    void deleteById(
            @PathVariable
            @Parameter(required = true, description = "Project ID to delete")
            String id,
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Invite User to Project", description = "Add a new collaborator to the project by email and assign a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully invited"),
            @ApiResponse(responseCode = "404", description = "User or Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "User is already a collaborator",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/invite")
    CollaboratorShortResponseDto inviteUser(
            @RequestBody
            @Valid
            InviteRequestDto inviteDto,

            @PathVariable
            @Parameter(required = true, description = "ID of the project to invite to")
            UUID id,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get all markers for project")
    @GetMapping("/{id}/markers")
    List<MarkerResponseDto> getMarkersByProjectId(
            @PathVariable
            @Parameter(required = true, description = "ID of the project to invite to")
            UUID id);


    @Operation(summary = "Create new marker for project")
    @PostMapping("/{projectId}/markers")
    MarkerResponseDto createMarker(
               @RequestBody
               @io.swagger.v3.oas.annotations.parameters.RequestBody(
                       required = true,
                       description = "Instance of Project to save"
               )

               @Valid MarkerCreateDto dto,

            @PathVariable
            @Parameter(required = true, description = "ID of the project to invite to")
            UUID projectId,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal);

}
