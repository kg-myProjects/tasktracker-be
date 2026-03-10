package de.upteams.tasktracker.taskstatus.controller;

import de.upteams.tasktracker.BaseControllerTest;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusUpdateDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.service.interfaces.TaskStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskStatusController.class)
class TaskStatusControllerTest extends BaseControllerTest {

    @MockitoBean
    private TaskStatusService taskStatusService;
    private final  String projectId = UUID.randomUUID().toString();
    private final  String statusId = UUID.randomUUID().toString();

    @Test
    @DisplayName("POST /api/v1/status - Success")
    void saveStatusSuccess() throws Exception {
        TaskStatusCreateDto request = new TaskStatusCreateDto(statusId,"To Do", 1, projectId);

        TaskStatusResponseDto response = new TaskStatusResponseDto(
                UUID.randomUUID().toString(),
                "To Do",
                1,
                projectId
        );

        when(taskStatusService.save(any(TaskStatusCreateDto.class), any())).thenReturn(response);

        performPost("/api/v1/status", request, mockUserPrincipal)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("To Do"));
    }

    @Test
    @DisplayName("PATCH /api/v1/status - Update Success")
    void updateStatusSuccess() throws Exception {
        TaskStatusUpdateDto request = new TaskStatusUpdateDto(statusId, "In Progress", 2,
                projectId);

        TaskStatusResponseDto response = new TaskStatusResponseDto(
                 statusId, "In Progress", 2, projectId
        );

        when(taskStatusService.update(any(TaskStatusUpdateDto.class), any())).thenReturn(response);

        performPatch("/api/v1/status", request, mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("In Progress"));
    }

    @Test
    @DisplayName("PATCH /api/v1/status/order - Success")
    void updateOrderSuccess() throws Exception {
        List<TaskStatusUpdateDto> request = List.of(new TaskStatusUpdateDto(statusId, "To Do", 1,projectId));

        TaskStatusResponseDto response = new TaskStatusResponseDto(
                statusId, "To Do", 1, projectId);

        when(taskStatusService.updateTaskStatusesOrder(anyList(), any())).thenReturn(List.of(response));

        performPatch("/api/v1/status/order", request, mockUserPrincipal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("To Do"));
    }

    @Test
    @DisplayName("DELETE /api/v1/status/{id} - Success")
    void deleteStatusSuccess() throws Exception {
        doNothing().when(taskStatusService).delete(eq(statusId), any());

        performDelete("/api/v1/status/" + statusId, mockUserPrincipal)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/status - Forbidden")
    void saveStatusForbidden() throws Exception {
        TaskStatusCreateDto request = new TaskStatusCreateDto(statusId,"To Do", 1, projectId);

        when(taskStatusService.save(any(), any()))
                .thenThrow(new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "No access to project"));

        performPost("/api/v1/status", request, mockUserPrincipal)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/status - Bad Request (Invalid Payload)")
    void saveStatusBadRequest() throws Exception {
        TaskStatusCreateDto invalidRequest = new TaskStatusCreateDto(
                null,
                "",
                1,
                null
        );


        performPost("/api/v1/status", invalidRequest, mockUserPrincipal)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/status/{id} - Not Found")
    void deleteStatusNotFound() throws Exception {
        doThrow(new de.upteams.tasktracker.taskstatus.exception.TaskStatusNotFoundException())
                .when(taskStatusService).delete(eq(statusId), any());

        performDelete("/api/v1/status/" + statusId, mockUserPrincipal)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/status/{id} - Forbidden (No Access)")
    void deleteStatusForbidden() throws Exception {
        doThrow(new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                org.springframework.http.HttpStatus.FORBIDDEN, "User has no access to delete this status"))
                .when(taskStatusService).delete(eq(statusId), any());

        performDelete("/api/v1/status/" + statusId, mockUserPrincipal)
                .andExpect(status().isForbidden());
    }

}
