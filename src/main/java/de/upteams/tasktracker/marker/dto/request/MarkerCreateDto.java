package de.upteams.tasktracker.marker.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MarkerCreateDto(
        @NotBlank(message = "Name must not be blank")
        String name,
        @NotBlank
        String color,
        String projectId
) {}
