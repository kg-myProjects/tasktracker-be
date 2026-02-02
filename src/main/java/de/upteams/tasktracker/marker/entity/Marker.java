package de.upteams.tasktracker.marker.entity;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "markers")
@Getter
@Setter
@NoArgsConstructor
public class Marker extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // У класі Marker замініть поле markers на це:
    @ManyToMany(mappedBy = "markers") // Вказуємо на поле в класі Task
    private Set<Task> tasks = new HashSet<>();


    @Override
    public String toString() {
        return "Marker{"+"name=" + name +"color=" + color+ '}';
    }
}

