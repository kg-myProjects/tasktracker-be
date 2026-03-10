package de.upteams.tasktracker.comment.service.interfaces;

import de.upteams.tasktracker.comment.dto.request.CommentRequestDto;
import de.upteams.tasktracker.comment.dto.response.CommentResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;

public interface CommentService {
    CommentResponseDto addComment(String taskId, CommentRequestDto dto, AppUser authUser);
    List<CommentResponseDto> getCommentsByTaskId(String taskId);
}
