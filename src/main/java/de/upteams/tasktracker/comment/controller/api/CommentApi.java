package de.upteams.tasktracker.comment.controller.api;

import de.upteams.tasktracker.comment.dto.request.CommentRequestDto;
import de.upteams.tasktracker.comment.dto.response.CommentResponseDto;
import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.security.dto.AuthUserDetails;
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

@Tag(name = "Comment controller")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/tasks/{taskId}/comments")
public interface CommentApi {

    @Operation(summary = "Add comment to Task", description = "Creates a new comment for a specific task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "7d7fb710-123e-412b-b570-76eb047d93c1",
                                      "text": "Please double-check the repository implementation.",
                                      "authorName": "John Doe",
                                      "authorAvatarUrl": "https://storage.com",
                                      "createdAt": "2025-04-26T10:00:00"
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid comment payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "text", "messages": ["must not be blank"] }
                                    ]
                                    """))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - user has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    CommentResponseDto addComment(
            @PathVariable
            @Parameter(description = "ID of the task to comment on")
            String taskId,

            @RequestBody
            @Valid
            CommentRequestDto dto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get comments for Task", description = "Retrieves all comments associated with a specific task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of comments successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CommentResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping
    List<CommentResponseDto> getComments(
            @PathVariable
            @Parameter(description = "ID of the task to get comments for")
            String taskId
    );

    @Operation(summary = "Delete comment from Task", description = "Deletes a specific comment if the user is the author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not the author of the comment",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Comment or Task not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{commentId}")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    void deleteComment(
            @PathVariable
            @Parameter(description = "ID of the task")
            String taskId,

            @PathVariable
            @Parameter(description = "ID of the comment to delete")
            String commentId,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Update comment", description = "Updates the text of an existing comment. Only the author can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not the author"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PatchMapping("/{commentId}")
    CommentResponseDto updateComment(
            @PathVariable
            @Parameter(description = "ID of the task")
            String taskId,

            @PathVariable
            @Parameter(description = "ID of the comment to update")
            String commentId,

            @RequestBody
            @Valid
            CommentRequestDto dto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );


}
