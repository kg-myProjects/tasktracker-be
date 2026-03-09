package de.upteams.tasktracker.task.controller;

import de.upteams.tasktracker.BaseControllerTest;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.service.interfaces.AttachmentService;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;

@WebMvcTest(controllers = TaskController.class)

class TaskControllerTest extends BaseControllerTest {

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private AttachmentService attachmentService;

    @Test
    @DisplayName("POST /api/v1/tasks - Success")
    void saveTaskSuccess() throws Exception {
        String projectId = UUID.randomUUID().toString();
        String statusId = UUID.randomUUID().toString();

        TaskCreateDto request = new TaskCreateDto(
                null, "New Task", "Description", statusId, projectId,
                List.of(), List.of(), List.of(), "2026-12-31T23:59:59", List.of()
        );

        TaskResponseDto response = TaskResponseDto.builder()
                .id(UUID.randomUUID().toString())
                .title("New Task")
                .projectId(projectId)
                .statusId(statusId)
                .build();

        when(taskService.save(any(TaskCreateDto.class), any())).thenReturn(response);

        performPost("/api/v1/tasks", request, mockUserPrincipal)
                .andExpect(status().isCreated()) // 201 як у Swagger
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id} - Success")
    void updateTaskSuccess() throws Exception {
        String taskId = UUID.randomUUID().toString();

        TaskUpdateDto updateRequest = new TaskUpdateDto(
                "Updated Title", "New Desc", null, null, null, null, null, null
        );

        TaskResponseDto response = TaskResponseDto.builder()
                .id(taskId)
                .title("Updated Title")
                .description("New Desc")
                .build();

        when(taskService.update(eq(taskId), any(TaskUpdateDto.class), any())).thenReturn(response);

        performPatch("/api/v1/tasks/" + taskId, updateRequest, mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks - Bad Request (Blank Title)")
    void saveTaskBadRequest() throws Exception {

        TaskCreateDto invalidRequest = new TaskCreateDto(
                null, "", "Desc", "statusId", "projectId",
                null, null, null, null, null
        );

        performPost("/api/v1/tasks", invalidRequest, mockUserPrincipal)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/tasks - Forbidden (No Access to Project)")
    void saveTaskForbidden() throws Exception {
        String projectId = UUID.randomUUID().toString();
        TaskCreateDto request = new TaskCreateDto(
                null, "New Task", "Desc", "statusId", projectId,
                List.of(), List.of(), List.of(), null, List.of()
        );

        when(taskService.save(any(TaskCreateDto.class), any()))
                .thenThrow(new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "User has no access to this project"));

        performPost("/api/v1/tasks", request, mockUserPrincipal)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User has no access to this project"));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id} - Forbidden (No Access)")
    void updateTaskForbidden() throws Exception {
        String taskId = UUID.randomUUID().toString();
        TaskUpdateDto updateRequest = new TaskUpdateDto(
                "Title", null, null, null, null, null, null, null
        );

        when(taskService.update(eq(taskId), any(TaskUpdateDto.class), any()))
                .thenThrow(new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "User has no access to this project"));

        performPatch("/api/v1/tasks/" + taskId, updateRequest, mockUserPrincipal)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} - Success")
    void getTaskByIdSuccess() throws Exception {
        String taskId = UUID.randomUUID().toString();

        TaskResponseDto response = TaskResponseDto.builder()
                .id(taskId)
                .title("Test Task")
                .projectId(UUID.randomUUID().toString())
                .statusId(UUID.randomUUID().toString())
                .build();

        when(taskService.getById(eq(taskId), any())).thenReturn(response);

        performGet("/api/v1/tasks/" + taskId, mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} - Not Found")
    void getTaskByIdNotFound() throws Exception {
        String taskId = UUID.randomUUID().toString();

        when(taskService.getById(eq(taskId), any()))
                .thenThrow(new RestApiException(org.springframework.http.HttpStatus.NOT_FOUND, "Task not found"));

        performGet("/api/v1/tasks/" + taskId, mockUserPrincipal)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id} - Success")
    void deleteTaskSuccess() throws Exception {
        String taskId = UUID.randomUUID().toString();

        doNothing().when(taskService).delete(eq(taskId), any());

        performDelete("/api/v1/tasks/" + taskId, mockUserPrincipal)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id} - Forbidden")
    void deleteTaskForbidden() throws Exception {
        String taskId = UUID.randomUUID().toString();

        doThrow(new RestApiException(org.springframework.http.HttpStatus.FORBIDDEN, "No access"))
                .when(taskService).delete(eq(taskId), any());

        performDelete("/api/v1/tasks/" + taskId, mockUserPrincipal)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{id}/attachments - Success")
    void uploadAttachmentSuccess() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String attachmentId = UUID.randomUUID().toString();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "some content".getBytes()
        );

        AttachmentResponseDto response = new AttachmentResponseDto(
                attachmentId,
                "test.txt",
                "https://storage.com",
                "TEXT",
                LocalDateTime.now()
        );

        when(attachmentService.upload(eq(taskId), any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/tasks/" + taskId + "/attachments")
                        .file(file)
                        .with(user(mockUserPrincipal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test.txt"))
                .andExpect(jsonPath("$.url").value("https://storage.com"))
                .andExpect(jsonPath("$.type").value("TEXT"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{id}/attachments - Bad Request (Empty File)")
    void uploadAttachmentEmptyFile() throws Exception {
        String taskId = UUID.randomUUID().toString();

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        when(attachmentService.upload(eq(taskId), any(), any()))
                .thenThrow(new RestApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "File is empty"));

        mockMvc.perform(multipart("/api/v1/tasks/" + taskId + "/attachments")
                        .file(emptyFile)
                        .with(user(mockUserPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is empty"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{id}/attachments - Forbidden (No Permission)")
    void uploadAttachmentForbidden() throws Exception {
        String taskId = UUID.randomUUID().toString();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        when(attachmentService.upload(eq(taskId), any(), any()))
                .thenThrow(new RestApiException(org.springframework.http.HttpStatus.FORBIDDEN,
                        "You don't have permission to upload files to this task"));

        mockMvc.perform(multipart("/api/v1/tasks/" + taskId + "/attachments")
                        .file(file)
                        .with(user(mockUserPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You don't have permission to upload files to this task"));
    }


    @Test
    @DisplayName("DELETE /api/v1/tasks/{id}/attachments/{attachmentId} - Success")
    void deleteAttachmentSuccess() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String attachmentId = UUID.randomUUID().toString();

        doNothing().when(attachmentService).delete(eq(taskId), eq(attachmentId), any());

        performDelete("/api/v1/tasks/" + taskId + "/attachments/" + attachmentId, mockUserPrincipal)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id}/attachments/{attachmentId} - Not Found")
    void deleteAttachmentNotFound() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String attachmentId = UUID.randomUUID().toString();

        doThrow(new RestApiException(org.springframework.http.HttpStatus.NOT_FOUND, "Attachment not found"))
                .when(attachmentService).delete(eq(taskId), eq(attachmentId), any());

        performDelete("/api/v1/tasks/" + taskId + "/attachments/" + attachmentId, mockUserPrincipal)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id}/attachments/{attachmentId} - Forbidden")
    void deleteAttachmentForbidden() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String attachmentId = UUID.randomUUID().toString();

        doThrow(new RestApiException(org.springframework.http.HttpStatus.FORBIDDEN,
                "You don't have permission to delete this attachment"))
                .when(attachmentService).delete(eq(taskId), eq(attachmentId), any());

        performDelete("/api/v1/tasks/" + taskId + "/attachments/" + attachmentId, mockUserPrincipal)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You don't have permission to delete this attachment"));
    }

}
