package de.upteams.tasktracker.comment.controller;

import de.upteams.tasktracker.comment.controller.api.CommentApi;
import de.upteams.tasktracker.comment.dto.request.CommentRequestDto;
import de.upteams.tasktracker.comment.dto.response.CommentResponseDto;
import de.upteams.tasktracker.comment.service.interfaces.CommentService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService service;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto addComment(String taskId, CommentRequestDto dto, AuthUserDetails principal) {
        return service.addComment(taskId, dto, principal.user());
    }

    @Override
    public List<CommentResponseDto> getComments(String taskId) {
        return service.getCommentsByTaskId(taskId);
    }
}
