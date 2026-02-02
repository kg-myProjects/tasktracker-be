package de.upteams.tasktracker.task.dto.request;

public record ChecklistItemDto(
        String id,
        String text,
        boolean completed
) {}

