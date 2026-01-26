package de.upteams.tasktracker.collaborator.dto.response;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import java.util.Set;

public record CollaboratorShortResponseDto(
        String userId,
        String email, // Так как в AppUser пока только email
        Set<ProjectRoles> roles
) {}
