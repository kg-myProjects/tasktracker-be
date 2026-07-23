package de.upteams.tasktracker.user.dto.response;

public record UpdateAvatarResponseDto(
        String avatarUrl,
        Long avatarUpdatedAt
) {}