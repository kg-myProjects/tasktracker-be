package de.upteams.tasktracker.task.controller.api;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
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

@Tag(name = "Task controller")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/tasks")
public interface TaskApi {

    @Operation(summary = "Create/save Task", description = "Creates a new task associated with a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskCreateDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "5",
                                      "title": "Implement repository layer",
                                      "description": "Create JPA repositories for all entities",
                                      "status": {"id": "2", "name": "To Do"},
                                      "project": { "id": "7", "title": "New Website Development" },
                                      "executors": []
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid task payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "title", "messages": ["must not be blank"] }
                                    ]
                                    """))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - user has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "User has no access to this project",
                                      "path": "/api/v1/tasks"
                                    }
                                    """))
            )
    })
    @PostMapping
    TaskResponseDto save(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Instance of Task to save"
            )
            @Valid
            TaskCreateDto task,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Update Tasks ", description = "Update Status of task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tasks Status successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskCreateDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "5",
                                      "title": "Implement repository layer",
                                      "description": "Create JPA repositories for all entities",
                                      "status": "To Do",
                                      "project": { "id": "7", "title": "New Website Development" },
                                      "executors": []
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid task payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "title", "messages": ["Uncorrected tittle"] }
                                    ]
                                    """))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - user has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "User has no access to this project",
                                      "path": "/api/v1/tasks"
                                    }
                                    """))
            )
    })
    @PatchMapping("/{id}")
    TaskResponseDto update(
            @PathVariable
            @Parameter(required = true, description = "Task ID to search")
            String id,

            @RequestBody
            TaskUpdateDto task,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );





    @Operation(summary = "Get Task", description = "Retrieves a task by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskCreateDto.class),
                               examples = @ExampleObject(value = """
                                    {
                                      "id": "5",
                                      "title": "Implement repository layer",
                                      "description": "Create JPA repositories for all entities",
                                      "status": "To Do",
                                      "project": { "id": "7", "title": "New Website Development" },
                                      "executors": []
                                    }
                                    """))
            ),

            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Task not found with id: 5",
                                      "path": "/api/v1/tasks/5"
                                    }
                                    """))
            )
    })
    @GetMapping("/{id}")
    TaskResponseDto getById(
            @PathVariable
            String id,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );





    @Operation(summary = "Delete Task", description = "Deletes a task by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
            ,
            @ApiResponse(responseCode = "403", description = "Forbidden - user has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    void deleteById(
            @PathVariable
            String id,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

}
