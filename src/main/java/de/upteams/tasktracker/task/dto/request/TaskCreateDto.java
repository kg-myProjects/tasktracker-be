package de.upteams.tasktracker.task.dto.request;

import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Value;

import java.util.List;

@Schema(description = "Data Transfer Object for Task entity")
@Getter
@Value
public class TaskCreateDto {

    @Schema(
            description = "Unique identifier of the Task",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    String id;

    @Schema(description = "Title of the Task", example = "Implement repository layer")
    @NotBlank(message = "must not be blank")
    String title;

    @Schema(
            description = "Detailed description of the Task",
            example = "Create JPA repositories for all entities"
    )
    String description;

    @Schema(description = "Id of the TaskStatus", example = "031a4644-a980-4cdb-ae14-27c04fe6579d")
    @NotNull
    String statusId;


    @Schema(description = "Id of the project of the task", example = "06753a51-51de-4a04-8d75-2b96cc5a7f92")
    @NotNull
    String projectId;

    @Schema(
            description = "List of User Ids assigned to this Task")
    List<String> executorIds;

    @Schema(
            description = "List of marker IDs assigned to this task")
    List<String> markerIds;

    @Schema(
            description = "List of checklist of this task")
    List<ChecklistItemDto> checklist;

    @Schema(
            description = "Deadline of the task in ISO format",
            example = "2026-12-31T23:59:59"
    )
    String dueDate;

    @Schema(
            description = "List of attachments (links or file metadata) for the task"
    )
    List<AttachmentResponseDto> attachments;

}
