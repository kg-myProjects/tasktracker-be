package de.upteams.tasktracker.project.dto.request;

import de.upteams.tasktracker.project.constants.ProjectValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Project create DTO
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectCreateDto(
        @Schema(
                description = "Title of the Project",
                example = "New Website Development"
        )

        @NotBlank(message = "Must not be blank")
        @Size(
                min = ProjectValidationConstants.TITLE_MIN_LENGTH,
                max = ProjectValidationConstants.TITLE_MAX_LENGTH)
        @Pattern(
                regexp = ProjectValidationConstants.TITLE_REGEX,
                message = "Project title contains invalid characters"
        )
        String title,


        @Schema(
                description = "Detailed description of the Project",
                example = "A Project to develop a new company website"
        )
        @NotBlank(message = "Must not be blank")
        @Size(
                min = ProjectValidationConstants.DESC_MIN_LENGTH,
                max = ProjectValidationConstants.DESC_MAX_LENGTH)
        String description) {
}
