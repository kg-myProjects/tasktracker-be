package de.upteams.tasktracker.task.entity;

import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;

@Entity
@Table(name = "task_attachments")
@Getter
@Setter
@NoArgsConstructor
public class Attachment extends BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AttachmentType  type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", taskId=" + getIdForToString(task) +
                '}';
    }
}
