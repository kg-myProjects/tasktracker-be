package de.upteams.tasktracker.task.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Value;


/**
 * Task DTO
 */
@Schema(description = "Data Transfer Object for Task entity")
@Getter
@Value
public class TaskCreateDto {

//        @Schema(
//                description = "Unique identifier of the Task",
//                example = "5",
//                accessMode = Schema.AccessMode.READ_ONLY
//        )
//        String id;

    @Schema(description = "Title of the Task", example = "Implement repository layer")
    @NotBlank(message = "must not be blank" )
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
    String  projectId;

//    @Schema(
//            description = "List of Users assigned to this Task",
//            accessMode = Schema.AccessMode.READ_ONLY
//    )
//    Set<EmployeeDto> executors = new HashSet<>();

}
