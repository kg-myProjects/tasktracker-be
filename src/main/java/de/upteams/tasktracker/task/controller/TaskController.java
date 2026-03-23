package de.upteams.tasktracker.task.controller;

import de.upteams.tasktracker.security.dto.AuthUserDetails;
import de.upteams.tasktracker.task.controller.api.TaskApi;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.service.interfaces.AttachmentService;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class TaskController implements TaskApi {

    private final TaskService service;
    private final AttachmentService attachmentService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto save(TaskCreateDto task, AuthUserDetails principal) {
        return service.save(task, principal.user());
    }

    @Override
    public TaskResponseDto update(String id, TaskUpdateDto task, AuthUserDetails principal) {
        return service.update(id, task, principal.user());
    }

    @Override
    public TaskResponseDto getById(String id, AuthUserDetails principal) {
        return service.getById(id, principal.user());
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(String id, AuthUserDetails principal) {
        service.delete(id, principal.user());
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponseDto uploadAttachment(String id, MultipartFile file, AuthUserDetails principal) {
        return attachmentService.upload(id, file, principal.user());
    }

    @Override
    public void deleteAttachment(String id, String attachmentId, AuthUserDetails principal) {
        attachmentService.delete(id, attachmentId, principal.user());
    }

}
