package de.upteams.tasktracker.taskstatus.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.taskstatus.controller.api.TaskStatusApi;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusUpdateDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.service.interfaces.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskStatusController implements TaskStatusApi {
    private final TaskStatusService service;

    @Override
    public TaskStatusResponseDto save(TaskStatusCreateDto task, AuthUserDetails principal) {
        return service.save(task);
    }

    @Override
    public TaskStatusResponseDto update(TaskStatusUpdateDto dto) {
        return service.update(dto);
    }

    @Override
    public List<TaskStatusResponseDto> updateTaskStatusesOrder(List<TaskStatusUpdateDto> dtos) {
        return service.updateTaskStatusesOrder(dtos);
    }

    @Override
    public void deleteById(String id, AuthUserDetails principal) {
service.delete(id,principal.user());
    }
}
