package de.upteams.tasktracker.collaborator.dto.response;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;

import java.util.Set;

public record CollaboratorShortResponseDto(
        String id,
        String email,
        String avatarUrl,
        Long avatarUpdatedAt,
        Set<ProjectRoles> roles
) {}