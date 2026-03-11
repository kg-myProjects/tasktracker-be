package de.upteams.tasktracker.comment.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.comment.dto.request.CommentRequestDto;
import de.upteams.tasktracker.comment.dto.response.CommentResponseDto;
import de.upteams.tasktracker.comment.entity.Comment;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.comment.persistence.CommentRepository;
import de.upteams.tasktracker.comment.service.interfaces.CommentService;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.comment.utils.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final CollaboratorService collaboratorService;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponseDto addComment(String taskId, CommentRequestDto dto, AppUser authUser) {
        Task task = taskService.getOrThrow(taskId);

        collaboratorService.checkAccessAndGetProject(
                authUser,
                task.getProject().getId().toString(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER)
        );

        Comment comment = commentMapper.mapDtoToEntity(dto);

        comment.setTask(task);
        comment.setAuthor(authUser);

        return commentMapper.mapEntityToDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByTaskId(String taskId) {
        // 1. Конвертуємо String у UUID
        UUID taskUuid = UUID.fromString(taskId);

        return commentRepository.findAllByTaskIdOrderByCreatedAtDesc(taskUuid)
                .stream()
                .map(commentMapper::mapEntityToDto)
                .toList();
    }

}
