package de.upteams.tasktracker.task.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.controller.api.TaskApi;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController implements TaskApi {

    /**
     * Service for various operations with Tasks
     */
    private final TaskService service;

    @Override
    public TaskResponseDto save(
            TaskCreateDto task,
            AuthUserDetails principal
    ) {
        // Исправлено: теперь передаем пользователя, чтобы задача знала своего автора
        return service.save(task, principal.user());
    }

    @Override
    public TaskResponseDto update(String id, TaskCreateDto task, AuthUserDetails principal) {
        return service.update(id, task);
    }

    @Override
    public TaskResponseDto getById(
            String id,
            AuthUserDetails principal
    ) {
        // Исправлено: теперь передаем пользователя для проверки прав доступа к задаче
        return service.getById(id, principal.user());
    }



    @Override
    public void deleteById(
            String id,
            AuthUserDetails principal
    ) {
        service.delete(id, principal.user());
    }
}