package de.upteams.tasktracker.task.dto.response;

import de.upteams.tasktracker.task.entity.AttachmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response DTO for Task Attachment")
public record AttachmentResponseDto(
        @Schema(description = "Unique identifier of the attachment", example = "6c6db690-651e-498a-b469-65eb036d82b0")
        String id,

        @Schema(description = "Name of the file or link title", example = "Project_Specs.pdf")
        String name,

        @Schema(description = "URL or file path", example = "https://storage.com")
        String url,

        @Schema(description = "Type of attachment (e.g., LINK, PDF, IMAGE)", example = "PDF")
        AttachmentType type,

        @Schema(description = "Creation date of the attachment")
        LocalDateTime createdAt
) {}
