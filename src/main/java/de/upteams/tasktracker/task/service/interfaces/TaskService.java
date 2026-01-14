package de.upteams.tasktracker.task.service.interfaces;

import de.upteams.tasktracker.task.dto.TaskDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.Optional;

/**
 * Service for various operations with Tasks
 */
public interface TaskService {

    // Обновлено: теперь принимает и данные задачи, и пользователя
    TaskDto save(TaskDto task, AppUser user);

    // Обновлено: теперь принимает и ID задачи, и пользователя
    TaskDto getById(String id, AppUser user);

    Task getOrThrow(String id);

    Optional<Task> findById(String id);

    List<TaskDto> getAll(String projectId, AppUser authUser);

    void delete(String id, AppUser changer);

}