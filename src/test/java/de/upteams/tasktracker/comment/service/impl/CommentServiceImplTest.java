package de.upteams.tasktracker.comment.service.impl;

import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.comment.dto.request.CommentRequestDto;
import de.upteams.tasktracker.comment.dto.response.CommentResponseDto;
import de.upteams.tasktracker.comment.entity.Comment;
import de.upteams.tasktracker.comment.persistence.CommentRepository;
import de.upteams.tasktracker.comment.utils.CommentMapper;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.user.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskService taskService;
    @Mock
    private CollaboratorService collaboratorService;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl service;

    private AppUser authUser;
    private Task task;
    private Project project;
    private UUID taskId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        authUser = new AppUser();
        ReflectionTestUtils.setField(authUser, "id", UUID.randomUUID());

        projectId = UUID.randomUUID();
        project = new Project();
        ReflectionTestUtils.setField(project, "id", projectId);

        taskId = UUID.randomUUID();
        task = new Task();
        ReflectionTestUtils.setField(task, "id", taskId);
        task.setProject(project);
    }

    @Test
    @DisplayName("addComment() should save comment when user has permission")
    void addComment_Success() {
        CommentRequestDto dto = new CommentRequestDto("Test comment");
        Comment comment = new Comment();

        when(taskService.getOrThrow(taskId.toString())).thenReturn(task);

        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId.toString()), anyCollection()))
                .thenReturn(project);

        when(commentMapper.mapDtoToEntity(dto)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));
        when(commentMapper.mapEntityToDto(any())).thenReturn(mock(CommentResponseDto.class));

        service.addComment(taskId.toString(), dto, authUser);

        verify(collaboratorService).checkAccessAndGetProject(eq(authUser), eq(projectId.toString()), anyCollection());
        verify(commentRepository).save(any(Comment.class));
        assertEquals(task, comment.getTask());
        assertEquals(authUser, comment.getAuthor());
    }

    @Test
    @DisplayName("getCommentsByTaskId() should return sorted list of comments")
    void getCommentsByTaskId_Success() {
        Comment comment = new Comment();
        when(commentRepository.findAllByTaskIdOrderByCreatedAtDesc(taskId))
                .thenReturn(List.of(comment));
        when(commentMapper.mapEntityToDto(comment)).thenReturn(mock(CommentResponseDto.class));

        List<CommentResponseDto> result = service.getCommentsByTaskId(taskId.toString());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(commentRepository).findAllByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Test
    @DisplayName("addComment() should throw 403 when access is denied")
    void addComment_Forbidden() {
        CommentRequestDto dto = new CommentRequestDto("Forbidden text");
        when(taskService.getOrThrow(taskId.toString())).thenReturn(task);

        when(collaboratorService.checkAccessAndGetProject(any(), any(), anyCollection()))
                .thenThrow(new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "No access"));

        assertThrows(de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException.class,
                () -> service.addComment(taskId.toString(), dto, authUser));

        verify(commentRepository, never()).save(any());
    }
}
