package de.upteams.tasktracker.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response DTO for Task Comment")
public record CommentResponseDto(
        @Schema(description = "Unique identifier of the comment", example = "7d7fb710-123e-412b-b570-76eb047d93c1")
        UUID id,

        @Schema(description = "The content of the comment", example = "Please double-check the repository implementation.")
        String text,

        @Schema(description = "Full name of the comment author", example = "John Doe")
        String authorName,

        @Schema(description = "URL to the author's avatar image", example = "https://storage.com")
        String authorAvatarUrl,

        @Schema(description = "Creation date and time of the comment")
        LocalDateTime createdAt
) {}
