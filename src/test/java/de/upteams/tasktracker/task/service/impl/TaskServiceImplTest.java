package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.marker.service.interfaces.MarkerService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.dto.request.ChecklistItemDto;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Attachment;
import de.upteams.tasktracker.task.entity.AttachmentType;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.exception.InvalidTaskPayloadException;
import de.upteams.tasktracker.task.exception.TaskNotFoundException;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.service.interfaces.CheckListService;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.taskstatus.persistence.TaskStatusRepository;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository repository;

    @Mock
    private TaskStatusRepository taskStatusRepository;

    @Mock
    private TaskMappingService mappingService;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private MarkerService markerService;

    @Mock
    private CheckListService checklistService;

    @InjectMocks
    private TaskServiceImpl service;

    private AppUser authUser;

    @BeforeEach
    void setUp() {
        authUser = new AppUser();
        ReflectionTestUtils.setField(authUser, "id", UUID.randomUUID());
    }

    private Task createTask() {

        Task task = new Task();
        ReflectionTestUtils.setField(task, "id", UUID.randomUUID());
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        TaskStatus status = new TaskStatus();
        ReflectionTestUtils.setField(status, "id", UUID.randomUUID());


        task.setProject(project);
        task.setStatus(status);
        task.setAttachments(new ArrayList<>());
        task.setMarkers(new HashSet<>());
        task.setExecutors(new HashSet<>());
        return task;
    }

    @Test
    @DisplayName("save() should create task and set project/status when user has permission")
    void saveShouldCreateTaskWhenUserHasPermission() {
        String projectId = UUID.randomUUID().toString();
        String statusId = UUID.randomUUID().toString();

        TaskCreateDto dto = TaskCreateDto.builder()
                .title("Task")
                .description("Description")
                .statusId(statusId)
                .projectId(projectId)
                .build();

        Project project = new Project();
        TaskStatus status = new TaskStatus();
        Task task = new Task();

        when(taskStatusRepository.findById(UUID.fromString(statusId))).thenReturn(Optional.of(status));

        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(project);

        when(mappingService.mapDtoToEntity(dto)).thenReturn(task);
        when(repository.save(task)).thenReturn(task);
        when(mappingService.mapEntityToDto(task)).thenReturn(mock(TaskResponseDto.class));

        service.save(dto, authUser);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertEquals(project, savedTask.getProject(), "Project must be correctly linked");
        assertEquals(status, savedTask.getStatus(), "Status must be correctly linked");
    }

    @Test
    @DisplayName("getById() should return task when user has any project role")
    void getByIdShouldReturnTaskWhenUserHasAccess() {

        Task task = createTask();
        String taskId = task.getId().toString();
        String projectId = task.getProject().getId().toString();
        TaskResponseDto expectedResponse = TaskResponseDto.builder()
                .id(taskId)
                .title(task.getTitle())
                .build();

        when(repository.findById(task.getId())).thenReturn(Optional.of(task));
        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(task.getProject());
        when(mappingService.mapEntityToDto(task)).thenReturn(expectedResponse);

        TaskResponseDto result = service.getById(taskId, authUser);

        assertNotNull(result);
        assertEquals(taskId, result.id());

        verify(repository).findById(task.getId());
        verify(mappingService).mapEntityToDto(task);
    }

    @Test
    @DisplayName("getById() should throw 403 Forbidden when user has no access to the project")
    void getByIdShouldThrowForbiddenWhenNoAccess() {
        Task task = createTask();
        String taskId = task.getId().toString();
        String projectId = task.getProject().getId().toString();

        when(repository.findById(task.getId())).thenReturn(Optional.of(task));
        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenThrow(new RestApiException(HttpStatus.FORBIDDEN, "No access"));
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.getById(taskId, authUser));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(mappingService, never()).mapEntityToDto(any());
    }

    @Test
    @DisplayName("getById() should throw TaskNotFoundException when task does not exist")
    void getByIdShouldThrowTaskNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> service.getById(id.toString(), authUser));

        verify(repository).findById(id);
        verifyNoInteractions(collaboratorService, mappingService);
    }

    @Test
    @DisplayName("getOrThrow() should return task when it exists")
    void getOrThrowShouldReturnTaskWhenExists() {
        Task task = createTask();
        UUID taskId = task.getId();

        when(repository.findById(taskId)).thenReturn(Optional.of(task));

        Task result = service.getOrThrow(taskId.toString());

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        verify(repository).findById(taskId);
    }

    @Test
    @DisplayName("getOrThrow() should throw TaskNotFoundException when task does not exist")
    void getOrThrowShouldThrowExceptionWhenNotFound() {
        UUID taskId = UUID.randomUUID();
        when(repository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> service.getOrThrow(taskId.toString()));

        verify(repository).findById(taskId);
    }

    @Test
    @DisplayName("findById() should return Optional containing task when it exists")
    void findByIdShouldReturnOptionalWithTaskWhenExists() {

        Task task = createTask();
        UUID taskId = task.getId();

        when(repository.findById(taskId)).thenReturn(Optional.of(task));

        Optional<Task> result = service.findById(taskId.toString());

        assertTrue(result.isPresent());
        assertEquals(task, result.get());
        verify(repository).findById(taskId);
    }

    @Test
    @DisplayName("findById() should return empty Optional when task does not exist")
    void findByIdShouldReturnEmptyOptionalWhenNotFound() {
        UUID taskId = UUID.randomUUID();
        when(repository.findById(taskId)).thenReturn(Optional.empty());

        Optional<Task> result = service.findById(taskId.toString());

        assertTrue(result.isEmpty());
        verify(repository).findById(taskId);
    }


    @Test
    @DisplayName("delete() should remove task from project's task set (relying on orphanRemoval)")
    void deleteShouldRemoveTaskFromProjectSetWhenUserHasPermission() {
        Task task = createTask();
        Project project = task.getProject();
        String projectId = project.getId().toString();

        project.setTasks(new HashSet<>(List.of(task)));
        UUID taskId = task.getId();

        when(repository.findById(taskId)).thenReturn(Optional.of(task));

        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(task.getProject());

        service.delete(taskId.toString(), authUser);

        assertFalse(project.getTasks().contains(task), "Task must be removed from the project's task set to trigger orphanRemoval");

        verify(repository, never()).delete(any());

        verify(repository).findById(taskId);
    }

    @Test
    @DisplayName("delete() should throw 403 Forbidden when user has no permission")
    void deleteForbidden() {
        Task task = createTask();
        String projectId = task.getProject().getId().toString();

        when(repository.findById(task.getId())).thenReturn(Optional.of(task));

        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenThrow(new RestApiException(HttpStatus.FORBIDDEN, "No access"));

        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.delete(task.getId().toString(), authUser));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    }

    @Test
    @DisplayName("delete() should throw TaskNotFoundException when task does not exist")
    void deleteNotFound() {
        UUID taskId = UUID.randomUUID();
        when(repository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> service.delete(taskId.toString(), authUser));
    }


    @Test
    @DisplayName("update() should update description when provided in DTO")
    void updateShouldUpdateDescription() {
        Task task = createTask();
        task.setDescription("old");
        UUID taskId = task.getId();
        String projectId = task.getProject().getId().toString();

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .description("new description")
                .build();

        when(repository.findById(taskId)).thenReturn(Optional.of(task));

        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(task.getProject());

        when(repository.saveAndFlush(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        service.update(taskId.toString(), dto, authUser);

        assertEquals("new description", task.getDescription());
        verify(repository).saveAndFlush(task);
    }

    @Test
    @DisplayName("update() should change task status when new statusId is different from current")
    void updateShouldUpdateStatus() {
        Task task = createTask();
        UUID taskId = task.getId();
        String projectId = task.getProject().getId().toString();
        UUID oldStatusId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(task.getStatus(), "id", oldStatusId);

        UUID newStatusId = UUID.randomUUID();
        TaskStatus newStatus = new TaskStatus();
        ReflectionTestUtils.setField(newStatus, "id", newStatusId);

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .statusId(newStatusId.toString())
                .build();

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskStatusRepository.findById(newStatusId)).thenReturn(Optional.of(newStatus));

        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(task.getProject());

        when(repository.saveAndFlush(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        service.update(taskId.toString(), dto, authUser);

        assertEquals(newStatus, task.getStatus(), "Task status should be updated");
        verify(taskStatusRepository).findById(newStatusId);
    }

    @Test
    @DisplayName("update() should update due date when provided in DTO")
    void updateShouldUpdateDueDate() {
        Task task = createTask();
        UUID taskId = task.getId();
        String projectId = task.getProject().getId().toString();

        String newDueDate = Instant.now()
                .plus(Duration.ofDays(1))
                .truncatedTo(ChronoUnit.SECONDS)
                .toString();

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .dueDate(newDueDate)
                .build();


        when(repository.findById(taskId)).thenReturn(Optional.of(task));

        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(task.getProject());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(repository.saveAndFlush(taskCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        service.update(taskId.toString(), dto, authUser);

        Task savedTask = taskCaptor.getValue();
        assertNotNull(savedTask.getDueDate(), "Due date should not be null after update");
        verify(repository).saveAndFlush(task);
    }

    @Test
    @DisplayName("update() should ThrowException when InvalidDate()")
    void updateShouldThrowExceptionShenInvalidDate() {
        Task task = createTask();
        TaskUpdateDto dto = TaskUpdateDto.builder()
                .dueDate("invalid-date")
                .build();

        when(repository.findById(any())).thenReturn(Optional.of(task));

        lenient().when(collaboratorService.hasUserPermission(
                any(), any(), anyCollection()
        )).thenReturn(true);

        assertThrows(
                InvalidTaskPayloadException.class,
                () -> service.update(task.getId().toString(), dto, authUser)
        );
    }

    @Test
    @DisplayName("update() should add attachment() when provided in DTO")
    void updateShouldAddAttachment() {
        Task task = createTask();
        UUID taskId = task.getId();
        String projectId = task.getProject().getId().toString();

        AttachmentResponseDto attachmentDto = new AttachmentResponseDto(
                null, "Google", "https://google.com", AttachmentType.LINK, LocalDateTime.now()
        );

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .attachments(List.of(attachmentDto))
                .build();

        when(repository.findById(taskId)).thenReturn(Optional.of(task));

        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(task.getProject());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(repository.saveAndFlush(taskCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        service.update(taskId.toString(), dto, authUser);

        Task savedTask = taskCaptor.getValue();
        assertEquals(1, savedTask.getAttachments().size(), "Attachment should be added to the task");

        Attachment added = savedTask.getAttachments().iterator().next();
        assertEquals("Google", added.getName());
        assertEquals(AttachmentType.LINK, added.getType());
        assertEquals(savedTask, added.getTask(), "Attachment must be linked back to the task");
    }

    @Test
    @DisplayName("update() should call markerService.syncTaskMarkers when markerIds are provided")
    void updateShouldSyncMarkers() {
        Task task = createTask();
        UUID taskId = task.getId();
        String projectId = task.getProject().getId().toString();

        List<String> markerIds = List.of(UUID.randomUUID().toString());

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .markerIds(markerIds)
                .build();

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(task.getProject());
        when(repository.saveAndFlush(any())).thenReturn(task);

        service.update(taskId.toString(), dto, authUser);

        verify(markerService).syncTaskMarkers(task, markerIds);
        verify(repository).saveAndFlush(task);
    }

    @Test
    @DisplayName("update() should call checklistService.syncChecklist when checklist is provided")
    void updateShouldSyncChecklist() {
        Task task = createTask();
        UUID taskId = task.getId();
        String projectId = task.getProject().getId().toString();

        ChecklistItemDto itemDto = new ChecklistItemDto(null, "New Task Item", false);
        List<ChecklistItemDto> checklist = List.of(itemDto);

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .checklist(checklist)
                .build();


        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.checkAccessAndGetProject(eq(authUser), eq(projectId), anyCollection()))
                .thenReturn(task.getProject());
        when(repository.saveAndFlush(any())).thenReturn(task);

        service.update(taskId.toString(), dto, authUser);

        verify(checklistService).syncChecklist(task, checklist);
        verify(repository).saveAndFlush(task);
    }


}