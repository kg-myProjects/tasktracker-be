package de.upteams.tasktracker.audit.entity;

import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogEntity extends BaseEntity {

    @Column(name = "entity", nullable = false)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_first_name")
    private String userFirstName;

    @Column(name = "user_last_name")
    private String userLastName;

    @Column(name = "user_avatar")
    private String userAvatar;

    @Column(name = "diff", columnDefinition = "TEXT")
    private String difference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Override
    public String toString() {
        return "AuditLogEntity{" +
                "entity='" + entity + '\'' +
                ", entityId='" + entityId + '\'' +
                ", entityName='" + entityName + '\'' +
                ", projectId='" + projectId + '\'' +
                ", action='" + action + '\'' +
                ", userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userFirstName='" + userFirstName + '\'' +
                ", userLastName='" + userLastName + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                ", difference='" + difference + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}