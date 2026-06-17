package de.upteams.tasktracker.project.entity;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.project.constants.ProjectValidationConstants;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.marker.entity.Marker;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;
import static de.upteams.tasktracker.utils.EntityUtil.getIdsForToString;

/**
 * Project entity
 */
@Entity
@Table(name = "project")
@NoArgsConstructor
@Getter
@Setter
public class Project extends BaseEntity {

    @Column(name = "title", nullable = false, length = ProjectValidationConstants.TITLE_MAX_LENGTH)
    @NotBlank
    private String title;

    @Column(name = "description", nullable = false, length = ProjectValidationConstants.DESC_MAX_LENGTH)
    @NotBlank
    private String description;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Collaborator> projectTeam = new HashSet<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskStatus> taskStatuses = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Marker> markers = new HashSet<>();


    public Project(String title, String description, AppUser owner) {
        this.title = title;
        this.description = description;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", authorId=" + getIdForToString(owner) +
                ", tasksIds=" + getIdsForToString(tasks) +
                '}';
    }
}
