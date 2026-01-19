package de.upteams.tasktracker.taskstatus.controller.api;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusUpdateDto;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "TaskStatus controller")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/status")

public interface TaskStatusApi {

    @Operation(summary = "Create/save TaskStatus", description = "Creates a new tasks status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tasks status successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskStatusCreateDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "5",
                                      "name": "To Do",
                                      "position": "1",
                                      "project_id": "7"
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid tasks status payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "name", "messages": ["must not be blank"] }
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
                                      "path": "/api/v1/status"
                                    }
                                    """))
            )
    })
    @PostMapping
    TaskStatusResponseDto save(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Instance of tasks status to save"
            )
            TaskStatusCreateDto task,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );



    @Operation(summary = "Update position of TaskStatus", description = "Update position of TaskStatus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Position of the tasks status successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskStatusResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "5",
                                      "name": "To Do",
                                      "position": "1",
                                      "project_id": "7"
                                    }
                                    """))
            )
    })
    @PatchMapping
    TaskStatusResponseDto update(
            @RequestBody
            TaskStatusUpdateDto task
            );



    @Operation(summary = "Delete tasks Status", description = "Deletes a tasks status by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tasks Status deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tasks Status not found",
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
            @Parameter(required = true, description = "Task status ID to search")
            String id,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

}
