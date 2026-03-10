package de.upteams.tasktracker.comment.entity;

import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;


@Entity
@Table(name = "task_comments")
@Getter
@Setter
@NoArgsConstructor
public class Comment extends BaseEntity {

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + (text != null ? text : "null") + '\'' +
                ", task=" + (task != null ? getIdForToString(task) : "null") +
                ", author=" + (author != null ? getIdForToString(author) : "null") +
                ", createdAt=" + createdAt +
                '}';
    }
}
