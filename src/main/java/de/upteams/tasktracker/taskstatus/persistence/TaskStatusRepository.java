package de.upteams.tasktracker.taskstatus.persistence;

import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, UUID> {
    List<TaskStatus> findByProjectId(UUID id);}
