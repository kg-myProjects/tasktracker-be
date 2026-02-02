package de.upteams.tasktracker.task.entity;

import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;

@Entity
@Table(name = "checklist_items")
@Getter
@Setter @NoArgsConstructor
public class ChecklistItem extends BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String text;

    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", text=" + text +
                ", completed='" + completed +
                ", taskId=" + getIdForToString(task) +
                '}';
    }
}
