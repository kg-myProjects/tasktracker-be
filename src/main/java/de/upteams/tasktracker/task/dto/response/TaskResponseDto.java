package de.upteams.tasktracker.task.dto.response;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.task.dto.request.ChecklistItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Task DTO
 */
@Schema(description = "Data Transfer Object for Task entity")
@Getter
@Value
@Builder
public class TaskResponseDto {

    @Schema(
            description = "Unique identifier of the Task",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    String id;

    @Schema(description = "Title of the Task", example = "Implement repository layer")
    String title;

    @Schema(
            description = "Detailed description of the Task",
            example = "Create JPA repositories for all entities"
    )
    String description;

    @Schema(description = "Id of the TaskStatus", example = "7")
    String statusId;

   // @JsonIgnore
    @Schema(
            description = "The ProjectId whit which this Task is associated",
            accessMode = Schema.AccessMode.READ_ONLY
    )
   String  projectId;

    @Schema(
            description = "List of Users assigned to this Task",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Builder.Default
    Set<CollaboratorShortResponseDto> executors = new HashSet<>();

    @Schema(
            description = "List of Markers assigned to this Task",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    Set<MarkerResponseDto> markers;

    @Schema(
            description = "List of ChecklistItem  of this Task",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    List<ChecklistItemDto> checklist;

    @Schema(description = "Date of the deadline Task")
    String dueDate;

    @Schema(description = "Attachments of the Task")
    List<AttachmentResponseDto> attachments;
}
