package de.upteams.tasktracker.collaborator.dto.response;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;

import java.util.Set;

public record CollaboratorShortResponseDto(
        String id,
        String email,
        String avatarUrl,
        Set<ProjectRoles> roles
) {}
