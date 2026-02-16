package de.upteams.tasktracker.task.dto.request;

import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Data Transfer Object for partial Task update")
public record TaskUpdateDto(
        @Schema(description = "Updated title of the Task", example = "Refactor Service Layer")
        String title,

        @Schema(description = "Updated detailed description", example = "Move logic from Controller to Service")
        String description,

        @Schema(description = "New status ID (moving between columns)", example = "031a4644-a980-4cdb-ae14-27c04fe6579d")
        String statusId,

        @Schema(description = "List of Collaborator IDs assigned to this Task")
        List<String> executorIds,

        @Schema(description = "List of Marker IDs assigned to this Task")
        List<String> markerIds,

        @Schema(description = "Deadline date in ISO format", example = "2026-12-31T23:59:59")
        String dueDate,

        @Schema(description = "List of checklist items for the task")
        List<ChecklistItemDto> checklist,

        @Schema(description = "Attachments of the Task")
        List<AttachmentResponseDto> attachments
) {}
