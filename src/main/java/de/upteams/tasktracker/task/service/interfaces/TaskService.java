package de.upteams.tasktracker.task.service.interfaces;

import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
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
    TaskResponseDto save(TaskCreateDto newTaskCreateDto);
    TaskResponseDto update(String id, TaskCreateDto newTaskCreateDto);

    TaskResponseDto getById(String id);

    Task getOrThrow(String id);

    Optional<Task> findById(String id);

 //   List<TaskResponseDto> getAllByProject(String projectId, AppUser authUser);

    void delete(String id, AppUser changer);

}