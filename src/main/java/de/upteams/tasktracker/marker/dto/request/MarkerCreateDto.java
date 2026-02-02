package de.upteams.tasktracker.marker.dto.request;

public record MarkerCreateDto(
        String name,
        String color,
        String projectId
) {}
