package de.upteams.tasktracker.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * Project DTO
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectCreateDto(
        @Schema(
                description = "Title of the Project",
                example = "New Website Development"
        )

        @NotBlank(message = "must not be blank" )
        @Length(min = 3, max = 50)
        @Pattern(
                regexp = "[A-Z][a-zA-Z0-9 ]{2,49}",
                message = "Project title should be at least 3 characters and start with capital letter"
        )
        String title,


        @Schema(
                description = "Detailed description of the Project",
                example = "A Project to develop a new company website"
        )
        @NotBlank(message = "must not be blank" )
        @Pattern(
                regexp = "[A-Z][a-zA-Z0-9 ]{2,}",
                message = "Project title should be at least 3 characters and start with capital letter"
        )

        String description) {

        @Override
        public String title() {
                return title;
        }

        @Override
        public String description() {
                return description;
        }
}
