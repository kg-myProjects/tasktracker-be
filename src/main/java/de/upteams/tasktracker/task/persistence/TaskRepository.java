package de.upteams.tasktracker.task.persistence;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Query("select t from Task t where t.project = ?1")
    List<Task> findByProject(Project project);
    @Query("SELECT MAX(t.taskNumber) FROM Task t WHERE t.project.id = :projectId")
    Optional<Long> findMaxTaskNumberByProjectId(@Param("projectId") UUID projectId);
}
