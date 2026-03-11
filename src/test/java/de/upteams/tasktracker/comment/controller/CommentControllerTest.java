package de.upteams.tasktracker.comment.controller;

import de.upteams.tasktracker.BaseControllerTest;
import de.upteams.tasktracker.comment.dto.request.CommentRequestDto;
import de.upteams.tasktracker.comment.dto.response.CommentResponseDto;
import de.upteams.tasktracker.comment.service.interfaces.CommentService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest extends BaseControllerTest {

    @MockitoBean
    private CommentService commentService;

    @Test
    @DisplayName("POST /api/v1/tasks/{taskId}/comments - Success")
    void addCommentSuccess() throws Exception {
        String taskId = UUID.randomUUID().toString();
        CommentRequestDto request = new CommentRequestDto("Test comment");

        CommentResponseDto response = new CommentResponseDto(
                UUID.randomUUID(),
                "Test comment",
                "John Doe",
                "https://avatar.com",
                LocalDateTime.now()
        );

        when(commentService.addComment(eq(taskId), any(CommentRequestDto.class), any()))
                .thenReturn(response);

        performPost("/api/v1/tasks/" + taskId + "/comments", request, mockUserPrincipal)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.authorName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{taskId}/comments - Success")
    void getCommentsSuccess() throws Exception {
        String taskId = UUID.randomUUID().toString();
        CommentResponseDto comment = new CommentResponseDto(
                UUID.randomUUID(), "Hello", "Jane", "https://avatar.com", LocalDateTime.now()
        );

        when(commentService.getCommentsByTaskId(taskId)).thenReturn(List.of(comment));

        performGet("/api/v1/tasks/" + taskId + "/comments", mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Hello"));
    }

    @Test
    @DisplayName("POST - Bad Request (Empty Text)")
    void addCommentBadRequest() throws Exception {
        String taskId = UUID.randomUUID().toString();
        CommentRequestDto invalidRequest = new CommentRequestDto("");

        performPost("/api/v1/tasks/" + taskId + "/comments", invalidRequest, mockUserPrincipal)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{taskId}/comments - Forbidden (No Access)")
    void addCommentForbidden() throws Exception {
        String taskId = UUID.randomUUID().toString();
        CommentRequestDto request = new CommentRequestDto("Trying to comment...");

        when(commentService.addComment(eq(taskId), any(CommentRequestDto.class), any()))
                .thenThrow(new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project"));

        performPost("/api/v1/tasks/" + taskId + "/comments", request, mockUserPrincipal)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User has no access to this project"));

        verify(commentService).addComment(eq(taskId), any(CommentRequestDto.class), any());
    }

    @Test
    @WithMockUser
    @DisplayName("getComments() should return list of comments and 200 OK")
    void getComments_Success() throws Exception {
        String taskId = UUID.randomUUID().toString();

        CommentResponseDto comment1 = new CommentResponseDto(
                UUID.randomUUID(),
                "First comment",
                "John Doe",
                "https://avatar.com",
                java.time.LocalDateTime.now()
        );

        CommentResponseDto comment2 = new CommentResponseDto(
                UUID.randomUUID(),
                "Second comment",
                "Jane Smith",
                "https://avatar.com",
                java.time.LocalDateTime.now()
        );

        when(commentService.getCommentsByTaskId(taskId)).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/api/v1/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("First comment"))
                .andExpect(jsonPath("$[1].authorName").value("Jane Smith"));

        verify(commentService).getCommentsByTaskId(taskId);
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{taskId}/comments - Not Found")
    void getCommentsNotFound() throws Exception {
        String taskId = UUID.randomUUID().toString();

        when(commentService.getCommentsByTaskId(taskId))
                .thenThrow(new RestApiException(HttpStatus.NOT_FOUND, "Task not found"));
        performGet("/api/v1/tasks/" + taskId + "/comments", mockUserPrincipal)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));

        verify(commentService).getCommentsByTaskId(taskId);
    }

}
