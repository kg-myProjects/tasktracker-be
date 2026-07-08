package de.upteams.tasktracker.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectLogDto {

    private UUID id;
    private String entity;
    private String entityName;
    private String action;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private String userAvatar;
    private String difference;
    private Instant createdAt;
}