package de.upteams.tasktracker.taskstatus.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.taskstatus.controller.api.TaskStatusApi;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusUpdateDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.service.interfaces.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskStatusController implements TaskStatusApi {
    private final TaskStatusService service;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusResponseDto save(TaskStatusCreateDto task, AuthUserDetails principal) {
        return service.save(task, principal.user());
    }

    @Override
    public TaskStatusResponseDto update(TaskStatusUpdateDto dto, AuthUserDetails principal) {
        return service.update(dto, principal.user());
    }

    @Override
    public List<TaskStatusResponseDto> updateTaskStatusesOrder(List<TaskStatusUpdateDto> dtos, AuthUserDetails principal) {
        return service.updateTaskStatusesOrder(dtos, principal.user());
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(String id, AuthUserDetails principal) {
service.delete(id,principal.user());
    }
}
