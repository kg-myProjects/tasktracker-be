package de.upteams.tasktracker.collaborator.controller.api;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.security.dto.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Collaborator controller", description = "Operations related to project members and task assignments")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/collaborators")
public interface CollaboratorApi {

    @Operation(summary = "Get all project collaborators", description = "Returns a list of all users invited to a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of collaborators retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CollaboratorShortResponseDto.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": "uuid-1",
                                        "email": "user@example.com",
                                        "roles": ["MEMBER"]
                                      }
                                    ]
                                    """))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/project/{projectId}")
    List<CollaboratorShortResponseDto> getByProjectId(
            @PathVariable @Parameter(description = "ID of the project") String projectId,
            @AuthenticationPrincipal @Parameter(hidden = true) AuthUserDetails principal
    );

}
