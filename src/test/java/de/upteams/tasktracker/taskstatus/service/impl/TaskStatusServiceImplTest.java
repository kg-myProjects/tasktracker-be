package de.upteams.tasktracker.taskstatus.service.impl;

import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusUpdateDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.taskstatus.exception.InvalidTaskStatusPayloadEsception;
import de.upteams.tasktracker.taskstatus.exception.TaskStatusNotFoundException;
import de.upteams.tasktracker.taskstatus.persistence.TaskStatusRepository;
import de.upteams.tasktracker.taskstatus.utils.TaskStatusMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskStatusServiceImplTest {

    @Mock
    private TaskStatusRepository repository;

    @Mock
    private TaskStatusMappingService mappingService;

    @Mock
    private CollaboratorService collaboratorService;

    @InjectMocks
    private TaskStatusServiceImpl service;

    private AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    private TaskStatus createMockStatus(UUID id, String name, int position, Project project) {
        TaskStatus status = new TaskStatus();
        ReflectionTestUtils.setField(status, "id", id);
        status.setName(name);
        status.setPosition(position);
        if (project.getId() == null) {
            ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        }
        status.setProject(project);
        return status;
    }

    @Test
    @DisplayName("save() should create taskStatus using mapper")
    void saveShouldCreateTaskStatusUsingMapper() {
        String projectId = UUID.randomUUID().toString();
        TaskStatusCreateDto dto = new TaskStatusCreateDto(null, "To do", 1, projectId);
        Project project = new Project();

        TaskStatus mappedStatus = new TaskStatus();
        mappedStatus.setName("To do");
        mappedStatus.setPosition(1);

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId), anyCollection()))
                .thenReturn(project);

        when(mappingService.mapDtoToEntity(dto)).thenReturn(mappedStatus);

        when(repository.save(any(TaskStatus.class))).thenAnswer(i -> i.getArgument(0));

        when(mappingService.mapEntityToStatusDto(any())).thenReturn(mock(TaskStatusResponseDto.class));

        service.save(dto, user);

        verify(mappingService).mapDtoToEntity(dto);
        verify(repository).save(mappedStatus);
        assertEquals(project, mappedStatus.getProject());
    }

    @Test
    @DisplayName("save() should throw 400 Exception when name is blank")
    void save_shouldThrowException_whenNameIsBlank() {
        String projectId = UUID.randomUUID().toString();
        TaskStatusCreateDto dto = new TaskStatusCreateDto(null, "  ", 0, projectId);
        Project project = new Project();

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId), anyCollection()))
                .thenReturn(project);
        assertThrows(InvalidTaskStatusPayloadEsception.class,
                () -> service.save(dto, user));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save() should throw 403 Forbidden when user has no permission")
    void save_shouldThrowForbidden_whenNoPermission() {
        String projectId = UUID.randomUUID().toString();
        TaskStatusCreateDto dto = new TaskStatusCreateDto(null, "To Do", 0, projectId);

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId), anyCollection()))
                .thenThrow(new de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException(
                        HttpStatus.FORBIDDEN, "No permission to create status"));

        RestApiException exception =
                assertThrows(de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException.class,
                        () -> service.save(dto, user));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update() should update status fields when user has permission")
    void update_shouldUpdateStatus_whenUserHasPermission() {
        String statusId = UUID.randomUUID().toString();
        String projectId = UUID.randomUUID().toString();

        TaskStatusUpdateDto updateDto = new TaskStatusUpdateDto(statusId, "In Progress", 2, projectId);
        Project project = new Project();

        ReflectionTestUtils.setField(project, "id", UUID.fromString(projectId));

        TaskStatus existingStatus = createMockStatus(UUID.fromString(statusId), "To do", 1, project);


        when(repository.findById(UUID.fromString(statusId))).thenReturn(Optional.of(existingStatus));

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId), anyCollection()))
                .thenReturn(project);

        when(repository.save(any(TaskStatus.class))).thenAnswer(i -> i.getArgument(0));

        when(mappingService.mapEntityToStatusDto(any())).thenReturn(mock(TaskStatusResponseDto.class));

        service.update(updateDto, user);

        verify(mappingService).updateEntityFromDto(updateDto, existingStatus);

        verify(repository).save(existingStatus);

        verify(collaboratorService).checkAccessAndGetProject(eq(user), eq(projectId), anyCollection());
    }


    @Test
    @DisplayName("update() should throw 400 Bad Request when status name is blank")
    void update_shouldThrowBadRequest_whenNameIsBlank() {
        String statusId = UUID.randomUUID().toString();
        String projectId = UUID.randomUUID().toString();

        TaskStatusUpdateDto updateDto = new TaskStatusUpdateDto(statusId, "   ", 2, projectId);

        TaskStatus existingStatus = new TaskStatus();
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", UUID.fromString(projectId));
        existingStatus.setProject(project);

        when(repository.findById(UUID.fromString(statusId))).thenReturn(Optional.of(existingStatus));

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId), anyCollection()))
                .thenReturn(project);

        RestApiException exception =
                assertThrows(de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException.class,
                        () -> service.update(updateDto, user));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("Status name cannot be empty", exception.getMessage());

        verify(mappingService, never()).updateEntityFromDto(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateTaskStatusesOrder() should update positions for multiple statuses")
    void updateTaskStatusesOrder_Success() {
        Project project = new Project();
        UUID projectId = UUID.randomUUID();
        ReflectionTestUtils.setField(project, "id", projectId);

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        TaskStatus status1 = createMockStatus(id1, "To do", 1, project);
        TaskStatus status2 = createMockStatus(id2, "In Progress", 2, project);

        TaskStatusUpdateDto dto1 = new TaskStatusUpdateDto(id1.toString(), "To do", 2, project.getId().toString());
        TaskStatusUpdateDto dto2 = new TaskStatusUpdateDto(id2.toString(), "In Progress", 1, project.getId().toString());
        List<TaskStatusUpdateDto> dtos = List.of(dto1, dto2);

        when(repository.findAllById(anyList())).thenReturn(List.of(status1, status2));
        when(repository.saveAll(anyList())).thenReturn(List.of(status1, status2));
        when(repository.findByProjectIdOrderByPositionAsc(any())).thenReturn(List.of(status2, status1));

        List<TaskStatusResponseDto> result = service.updateTaskStatusesOrder(dtos, user);

        assertEquals(2, result.size());
        assertEquals(2, status1.getPosition());
        assertEquals(1, status2.getPosition());
        verify(repository).saveAll(anyList());
    }

    @Test
    @DisplayName("update() should not overwrite existing position if DTO position is null")
    void update_shouldKeepExistingPosition_whenDtoPositionIsNull() {
        UUID statusId = UUID.randomUUID();
        TaskStatus existingStatus = createMockStatus(statusId, "Old Name", 5, new Project());

        TaskStatusUpdateDto dto = new TaskStatusUpdateDto(statusId.toString(), "New Name", null, UUID.randomUUID().toString());

        when(repository.findById(statusId)).thenReturn(Optional.of(existingStatus));
        when(collaboratorService.checkAccessAndGetProject(any(), any(), anyCollection())).thenReturn(new Project());

        doAnswer(invocation -> {
            TaskStatusUpdateDto d = invocation.getArgument(0);
            TaskStatus s = invocation.getArgument(1);
            if (d.getName() != null) s.setName(d.getName());
            if (d.getPosition() != null) s.setPosition(d.getPosition());
            return null;
        }).when(mappingService).updateEntityFromDto(any(), any());

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.update(dto, user);

        assertEquals("New Name", existingStatus.getName());
        assertEquals(5, existingStatus.getPosition(), "Position should remain unchanged");
    }

    @Test
    @DisplayName("getOrThrow() should return TaskStatus when it exists")
    void getOrThrow_shouldReturnStatus_whenExists() {
        UUID statusId = UUID.randomUUID();
        TaskStatus status = new TaskStatus();
        ReflectionTestUtils.setField(status, "id", statusId);

        when(repository.findById(statusId)).thenReturn(Optional.of(status));

        TaskStatus result = service.getOrThrow(statusId.toString());

        assertNotNull(result);
        assertEquals(statusId, result.getId());
        verify(repository).findById(statusId);
    }

    @Test
    @DisplayName("getOrThrow() should throw TaskStatusNotFoundException when status does not exist")
    void getOrThrow_shouldThrowException_whenNotFound() {
        UUID statusId = UUID.randomUUID();
        when(repository.findById(statusId)).thenReturn(Optional.empty());

        assertThrows(TaskStatusNotFoundException.class,
                () -> service.getOrThrow(statusId.toString()));

        verify(repository).findById(statusId);
    }
    @Test
    @DisplayName("findById() should return Optional containing status when it exists")
    void findById_shouldReturnOptionalWithStatus_whenExists() {
        UUID statusId = UUID.randomUUID();
        TaskStatus status = new TaskStatus();
        ReflectionTestUtils.setField(status, "id", statusId);

        when(repository.findById(statusId)).thenReturn(Optional.of(status));

        Optional<TaskStatus> result = service.findById(statusId.toString());

        assertTrue(result.isPresent());
        assertEquals(status, result.get());
        verify(repository).findById(statusId);
    }

    @Test
    @DisplayName("findById() should return empty Optional when status does not exist")
    void findById_shouldReturnEmptyOptional_whenNotFound() {
        UUID statusId = UUID.randomUUID();
        when(repository.findById(statusId)).thenReturn(Optional.empty());

        Optional<TaskStatus> result = service.findById(statusId.toString());

        assertTrue(result.isEmpty());
        verify(repository).findById(statusId);
    }

    @Test
    @DisplayName("delete() should successfully remove status when user has permission")
    void delete_shouldDeleteStatus_whenUserHasPermission() {
        UUID statusId = UUID.randomUUID();
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID()); // Даємо ID проекту

        TaskStatus status = createMockStatus(statusId, "To Delete", 1, project);

        when(repository.findById(statusId)).thenReturn(Optional.of(status));

        when(collaboratorService.checkAccessAndGetProject(eq(user), any(), anyCollection()))
                .thenReturn(project);

        service.delete(statusId.toString(), user);

        verify(repository).delete(status);
        verify(collaboratorService).checkAccessAndGetProject(eq(user), any(), anyCollection());
    }

    @Test
    @DisplayName("delete() should throw 403 Forbidden when user has no delete permission")
    void delete_shouldThrowForbidden_whenNoPermission() {
        UUID statusId = UUID.randomUUID();
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        TaskStatus status = createMockStatus(statusId, "Protected", 1, project);

        when(repository.findById(statusId)).thenReturn(Optional.of(status));

        when(collaboratorService.checkAccessAndGetProject(eq(user), any(), anyCollection()))
                .thenThrow(new RestApiException(HttpStatus.FORBIDDEN, "No delete permission"));

        assertThrows(RestApiException.class, () -> service.delete(statusId.toString(), user));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete() should throw 404 Not Found when status does not exist")
    void delete_shouldThrowNotFound_whenStatusDoesNotExist() {
        UUID statusId = UUID.randomUUID();
        when(repository.findById(statusId)).thenReturn(Optional.empty());

        assertThrows(TaskStatusNotFoundException.class, () -> service.delete(statusId.toString(), user));

        verify(repository, never()).delete(any());
    }

}
