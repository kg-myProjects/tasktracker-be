package de.upteams.tasktracker.task.dto.request;

import de.upteams.tasktracker.task.entity.AttachmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record AttachmentRequestDto(
        @Schema(description = "Name of the file or link title", example = "Project_Specs.pdf")
        @NotBlank
        String name,

        @Schema(description = "URL or file path", example = "https://storage.com")
        @NotBlank
        String url,

        @Schema(description = "Type of attachment (e.g., LINK, PDF, IMAGE)", example = "PDF")
        @NotBlank
        AttachmentType type

) {
}
