package de.upteams.tasktracker.taskstatus.entity;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "task_status",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "name"})
)
public class TaskStatus extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column
    private Integer position;

    @OneToMany(mappedBy = "status")
    private Set<Task> tasks;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Override
    public String toString() {
        return "";
    }
}
