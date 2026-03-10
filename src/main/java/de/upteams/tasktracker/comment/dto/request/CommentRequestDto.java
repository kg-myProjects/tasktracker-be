package de.upteams.tasktracker.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO for creating a new Task Comment")
public record CommentRequestDto(
        @NotBlank(message = "Comment text cannot be blank")
        @Size(max = 2000, message = "Comment is too long (max 2000 characters)")
        @Schema(
                description = "The content of the comment",
                example = "I have finished the repository layer. Please review it.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String text
) {}
