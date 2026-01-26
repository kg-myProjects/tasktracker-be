package de.upteams.tasktracker.taskstatus.persistence;

import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, UUID> {
    List<TaskStatus> findByProjectId(UUID id);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TaskStatus s SET s.position = s.position + 1 " +
            "WHERE s.project.id = :projectId AND s.position >= :position")
    void shiftPositionsForward(@Param("projectId") UUID projectId, @Param("position") Integer position);

}
