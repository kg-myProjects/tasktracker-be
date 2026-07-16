package de.upteams.tasktracker.project.dto.response;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * Project DTO
 *
 * @param id          Project ID
 * @param title       Project title
 * @param description Project description
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectResponseDto(

        @Schema(
                description = "Unique identifier of the Project",
                example = "7",
                accessMode = Schema.AccessMode.READ_ONLY
                )
        String id,

        @Schema(
                description = "Title of the Project",
                example = "New Website Development"
                )
        String title,

        @Schema(
                description = "Detailed description of the Project",
                example = "A Project to develop a new company website"
                )
        String description,

        @Schema(
                description = "List of collaborators assigned to this Project",
                accessMode = Schema.AccessMode.READ_ONLY
                )
        Set<CollaboratorShortResponseDto> projectTeam,

        @Schema(
                description = "List of Markers assigned to this Task",
                accessMode = Schema.AccessMode.READ_ONLY
                )
        Set<MarkerResponseDto> markers)
{}