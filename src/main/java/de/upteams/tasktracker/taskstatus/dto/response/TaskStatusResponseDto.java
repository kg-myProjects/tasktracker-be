package de.upteams.tasktracker.taskstatus.dto.response;

import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema(description = "Data Transfer Object for TaskStatus entity")
@Value
public class TaskStatusResponseDto {
    @Schema(
            description = "Unique identifier of the TaskStatus",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    String id;

    @Schema(description = "Name of the TaskStatus", example = "To Do")
    String name;

    @Schema(description = "Position", example = "1")
    Integer position;

    @Schema(description = "Id of the project", example = "06753a51-51de-4a04-8d75-2b96cc5a7f92")
    ProjectResponseDto project;


}

