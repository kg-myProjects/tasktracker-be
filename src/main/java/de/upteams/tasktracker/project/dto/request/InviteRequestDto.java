package de.upteams.tasktracker.project.dto.request;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request DTO for inviting a user to a project")
public record InviteRequestDto(
        @Schema(description = "Email of the user to invite", example = "collaborator@example.com")
        @NotBlank @Email
        String email,

        @Schema(description = "Role to assign to the user")
        @NotNull
        ProjectRoles role
) {}