package de.upteams.tasktracker.task.dto.response;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.task.dto.request.ChecklistItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Task DTO
 */
@Schema(description = "Data Transfer Object for Task entity")
@Builder
public record TaskResponseDto(

        @Schema(description = "Unique identifier of the Task", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
        String id,

        @Schema(description = "Title of the Task", example = "Implement repository layer")
        String title,

        @Schema(description = "Number of the Task", example = "3")
        Long taskNumber,

        @Schema(description = "Detailed description of the Task", example = "Create JPA repositories for all entities")
        String description,

        @Schema(description = "Id of the TaskStatus", example = "7")
        String statusId,

        @Schema(description = "The ProjectId with which this Task is associated", accessMode = Schema.AccessMode.READ_ONLY)
        String projectId,

        @Schema(description = "List of Users assigned to this Task", accessMode = Schema.AccessMode.READ_ONLY)
        Set<CollaboratorShortResponseDto> executors,

        @Schema(description = "List of Markers assigned to this Task", accessMode = Schema.AccessMode.READ_ONLY)
        Set<MarkerResponseDto> markers,

        @Schema(description = "List of ChecklistItem of this Task", accessMode = Schema.AccessMode.READ_ONLY)
        List<ChecklistItemDto> checklist,

        @Schema(description = "Date of the deadline Task")
        String dueDate,

        @Schema(description = "Creation date of the task")
        String createdAt,

        @Schema(description = "The last time the task was modified", accessMode = Schema.AccessMode.READ_ONLY, example = "2024-03-11T15:30:00")
        String updatedAt,

        @Schema(description = "Attachments of the Task")
        List<AttachmentResponseDto> attachments

) {
    public TaskResponseDto {
        if (executors == null) executors = Collections.emptySet();
        if (markers == null) markers = Collections.emptySet();
        if (checklist == null) checklist = Collections.emptyList();
        if (attachments == null) attachments = Collections.emptyList();
    }
}
