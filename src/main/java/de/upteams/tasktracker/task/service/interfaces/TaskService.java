package de.upteams.tasktracker.task.service.interfaces;

import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.Optional;

/**
 * Service for various operations with Tasks
 */
public interface TaskService {

    TaskResponseDto save(TaskCreateDto newTaskCreateDto, AppUser user);

    TaskResponseDto update(String id, TaskUpdateDto newTaskUpdateDto, AppUser user);

    TaskResponseDto getById(String id, AppUser user);

    Task getOrThrow(String id);

    Optional<Task> findById(String id);

    void delete(String id, AppUser changer);

}
