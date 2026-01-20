package de.upteams.tasktracker.taskstatus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Schema(description = "Data Transfer Object for TaskStatus entity")
@Value
public class TaskStatusUpdateDto {
    @Schema(description = "Id", example = "1")
    @NotNull
    String id;

    @Schema(description = "Position", example = "1")
    @NotNull
    Integer position;


}
