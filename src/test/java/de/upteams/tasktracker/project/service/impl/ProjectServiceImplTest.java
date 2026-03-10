package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.audit.entity.AuditLogEntity;
import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.marker.dto.request.MarkerCreateDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.marker.entity.Marker;
import de.upteams.tasktracker.marker.persistence.MarkerRepository;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectLogDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.taskstatus.persistence.TaskStatusRepository;
import de.upteams.tasktracker.taskstatus.utils.TaskStatusMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.persistence.UserRepository;
import de.upteams.tasktracker.audit.persistence.AuditLogRepository;
import de.upteams.tasktracker.marker.utils.MarkerMapper;
import de.upteams.tasktracker.collaborator.utils.CollaboratorMapper;
import org.springframework.test.util.ReflectionTestUtils;
import de.upteams.tasktracker.project.exception.*;
import org.springframework.http.HttpStatus;
import de.upteams.tasktracker.project.dto.request.InviteRequestDto;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository repository;

    @Mock
    private TaskStatusRepository taskStatusRepository;

    @Mock
    private TaskStatusMappingService taskStatusMappingService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMappingService taskMappingService;

    @Mock
    private ProjectMapper mappingService;

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollaboratorMapper collaboratorMapper;

    @Mock
    private MarkerRepository markerRepository;

    @Mock
    private MarkerMapper markerMapper;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ProjectServiceImpl service;

    private AppUser user;
    private Project project;

    private  UUID userId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        userId = UUID.randomUUID();
        project = new Project();
        projectId = UUID.randomUUID();
        project.setOwner(user);
        project.setProjectTeam(new HashSet<>());
    }

    @Test
    @DisplayName("save()  Success (Created)")
    void save_shouldCreateProjectAndOwnerCollaborator() {

        ProjectCreateDto dto = new ProjectCreateDto("Test Project", "Description");

        Project mappedProject = new Project();
        mappedProject.setProjectTeam(new HashSet<>());

        ProjectResponseDto responseDto =
                new ProjectResponseDto("1","Test Project","Description",null,null,null);

        when(mappingService.mapDtoToEntity(dto)).thenReturn(mappedProject);
        when(repository.save(mappedProject)).thenReturn(mappedProject);
        when(mappingService.mapEntityToDto(mappedProject)).thenReturn(responseDto);

        ProjectResponseDto result = service.save(dto, user);

        assertEquals("Test Project", result.title());
        assertEquals(1, mappedProject.getProjectTeam().size());
        Collaborator createdCollaborator = mappedProject.getProjectTeam().iterator().next();
        assertEquals(user, createdCollaborator.getAppUser());

        verify(repository).save(mappedProject);
        verify(collaboratorRepository).save(any(Collaborator.class));
    }

    @Test
    @DisplayName("save() should throw exception when title is empty")
    void save_shouldThrowException_whenTitleIsEmpty() {
        ProjectCreateDto invalidDto = new ProjectCreateDto("", "Some description");

        assertThrows(InvalidProjectPayloadException.class,
                () -> service.save(invalidDto, user));

        verifyNoInteractions(repository, collaboratorRepository);
    }


    @Test
    @DisplayName("update() Success")
    void update_shouldUpdateProject_whenUserHasPermission() {
        ReflectionTestUtils.setField(project, "id", projectId);
        ProjectCreateDto dto = new ProjectCreateDto("New title", "New description");

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId.toString()), anyCollection()))
                .thenReturn(project);

        when(repository.save(project)).thenReturn(project);

        when(mappingService.mapEntityToDto(project))
                .thenReturn(new ProjectResponseDto(
                        projectId.toString(),
                        "New title",
                        "New description",
                        null, null, null));

        ProjectResponseDto result = service.update(projectId, dto, user);

        assertEquals("New title", result.title());
        verify(repository).save(project);
    }

    @Test
    @DisplayName("update() should throw 403 Forbidden when user has no permission")
    void update_shouldThrowRestApiException_whenNoPermission() {
        ReflectionTestUtils.setField(project, "id", projectId);
        ProjectCreateDto dto = new ProjectCreateDto("Title", "Desc");

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId.toString()), anyCollection()))
                .thenThrow(new RestApiException(HttpStatus.FORBIDDEN, "No permission"));

        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.update(projectId, dto, user));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(repository, never()).save(any());
    }


    @Test
    void getById_shouldReturnProjectDto() {

        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(project, "id", id);

        when(repository.findByIdWithTeam(id))
                .thenReturn(Optional.of(project));

        when(mappingService.mapEntityToDto(project))
                .thenReturn(new ProjectResponseDto(
                        id.toString(),
                        "Title",
                        "Desc",
                        null,null,null));

        ProjectResponseDto result = service.getById(id);

        assertEquals("Title", result.title());
    }


    @Test
    void getAll_shouldReturnListOfProjects() {
        ReflectionTestUtils.setField(project, "id", projectId);
        when(repository.findAllWithTeam())
                .thenReturn(List.of(project));

        when(mappingService.mapEntityToDto(project))
                .thenReturn(new ProjectResponseDto(
                        project.getId().toString(),
                        "Title",
                        "Desc",
                        null,null,null));

        List<ProjectResponseDto> result = service.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void getOrThrow_shouldReturnProject() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdWithTeam(id))
                .thenReturn(Optional.of(project));

        Project result = service.getOrTrow(id);

        assertNotNull(result);
    }

    @Test
    void getOrThrow_shouldThrowException_whenProjectNotFound() {

        UUID id = UUID.randomUUID();

        when(repository.findByIdWithTeam(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> service.getOrTrow(id)
        );
    }

    @Test
    void getMyProjects_shouldReturnUserProjects() {

        ReflectionTestUtils.setField(user,"id",userId);

        when(repository.findAllForUser(userId))
                .thenReturn(List.of(project));

        when(mappingService.mapEntityToDto(project))
                .thenReturn(new ProjectResponseDto(
                        "1","Title","Desc",null,null,null));

        List<ProjectResponseDto> result = service.getMyProjects(user);

        assertEquals(1,result.size());
    }

    @Test
    void getMyProjects_shouldReturnEmptyList() {

        ReflectionTestUtils.setField(user, "id", userId);

        when(repository.findAllForUser(userId))
                .thenReturn(Collections.emptyList());

        List<ProjectResponseDto> result = service.getMyProjects(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repository).findAllForUser(userId);
        verifyNoInteractions(mappingService);
    }

    @Test
    void getAllStatusByProjectId_shouldReturnStatuses() {

        TaskStatus status = mock(TaskStatus.class);
        TaskStatusResponseDto dto = mock(TaskStatusResponseDto.class);

        when(taskStatusRepository.findByProjectId(projectId))
                .thenReturn(List.of(status));

        when(taskStatusMappingService.mapEntityToStatusDto(status))
                .thenReturn(dto);

        List<TaskStatusResponseDto> result = service.getAllStatusByProjectId(projectId);

        assertEquals(1, result.size());
    }


    @Test
    @DisplayName("getAllStatusByProjectId() should return empty list when no statuses exist")
    void getAllStatusByProjectId_shouldReturnEmptyList() {

        when(taskStatusRepository.findByProjectId(projectId))
                .thenReturn(Collections.emptyList());

        List<TaskStatusResponseDto> result =
                service.getAllStatusByProjectId(projectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskStatusRepository).findByProjectId(projectId);
        verifyNoInteractions(taskStatusMappingService);
    }

    @Test
    @DisplayName("getAllTasksByProject() should return tasks list")
    void getAllTasksByProject_shouldReturnTasks() {
        ReflectionTestUtils.setField(project, "id", projectId);

        Task task = mock(Task.class);
        TaskResponseDto dto = mock(TaskResponseDto.class);

        when(repository.findByIdWithTeam(projectId))
                .thenReturn(Optional.of(project));

        when(taskRepository.findByProject(project))
                .thenReturn(List.of(task));

        when(taskMappingService.mapEntityToDto(task))
                .thenReturn(dto);

        List<TaskResponseDto> result = service.getAllTasksByProject(projectId);

        assertEquals(1, result.size());

        verify(taskRepository).findByProject(project);
        verify(taskMappingService).mapEntityToDto(task);
    }

    @Test
    @DisplayName("getAllTasksByProject() should return empty list when project has no tasks")
    void getAllTasksByProject_shouldReturnEmptyList() {
        ReflectionTestUtils.setField(project, "id", projectId);

        when(repository.findByIdWithTeam(projectId))
                .thenReturn(Optional.of(project));

        when(taskRepository.findByProject(project))
                .thenReturn(Collections.emptyList());

        List<TaskResponseDto> result =
                service.getAllTasksByProject(projectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByProject(project);
        verifyNoInteractions(taskMappingService);
    }

    @Test
    @DisplayName("delete() should delete project when user is the owner")
    void delete_shouldDeleteProject_whenUserIsOwner() {
        ReflectionTestUtils.setField(project, "id", projectId);

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId.toString()), anyCollection()))
                .thenReturn(project);

        service.delete(projectId, user);

        verify(repository).delete(project);
        verify(auditLogRepository).deleteAllByProjectId(projectId.toString());
    }

    @Test
    @DisplayName("delete() should throw 403 Forbidden when user is not the owner")
    void delete_shouldThrowException_whenNotOwner() {
        ReflectionTestUtils.setField(project, "id", projectId);

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId.toString()), anyCollection()))
                .thenThrow(new RestApiException(HttpStatus.FORBIDDEN, "Only the owner can delete"));

        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.delete(projectId, user));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("inviteUser() should successfully invite a new member")
    void inviteUser_Success() {
        InviteRequestDto inviteDto = new InviteRequestDto("guest@test.com", ProjectRoles.MEMBER);
        AppUser userToInvite = new AppUser();
        ReflectionTestUtils.setField(userToInvite, "id", UUID.randomUUID());

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId.toString()), anyCollection()))
                .thenReturn(project);

        when(userRepository.findByEmailIgnoreCase("guest@test.com")).thenReturn(Optional.of(userToInvite));

        when(collaboratorRepository.existsByProjectAndAppUser(project, userToInvite)).thenReturn(false);

        when(collaboratorRepository.save(any(Collaborator.class))).thenAnswer(i -> i.getArgument(0));
        when(collaboratorMapper.mapEntityToShortDto(any())).thenReturn(mock(CollaboratorShortResponseDto.class));

        service.inviteUser(inviteDto, projectId, user);

        verify(collaboratorRepository).save(argThat(c ->
                c.getAppUser().equals(userToInvite) &&
                        c.getProjectRolesSet().contains(ProjectRoles.MEMBER)));
    }

    @Test
    @DisplayName("inviteUser() should throw 403 Forbidden when inviter is not Admin or Owner")
    void inviteUser_Forbidden() {
        InviteRequestDto inviteDto = new InviteRequestDto("guest@test.com", ProjectRoles.MEMBER);

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId.toString()), anyCollection()))
                .thenThrow(new RestApiException(HttpStatus.FORBIDDEN, "No permission"));

        assertThrows(RestApiException.class, () -> service.inviteUser(inviteDto, projectId, user));
        verify(collaboratorRepository, never()).save(any());
    }

    @Test
    @DisplayName("inviteUser() should throw 409 Conflict when user is already in project")
    void inviteUser_Conflict() {
        InviteRequestDto inviteDto = new InviteRequestDto("existing@test.com", ProjectRoles.MEMBER);
        AppUser existingUser = new AppUser();

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectId.toString()), anyCollection()))
                .thenReturn(project);
        when(userRepository.findByEmailIgnoreCase("existing@test.com")).thenReturn(Optional.of(existingUser));

        when(collaboratorRepository.existsByProjectAndAppUser(project, existingUser)).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.inviteUser(inviteDto, projectId, user));

        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        verify(collaboratorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMarker() - Success: should create marker and link to project")
    void createMarker_Success() {
        String projectIdStr = projectId.toString();
        MarkerCreateDto dto = new MarkerCreateDto("Urgent", "#FF0000", projectIdStr);
        Marker markerEntity = new Marker();

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectIdStr), anyCollection()))
                .thenReturn(project);

        when(markerMapper.mapDtoToEntity(dto)).thenReturn(markerEntity);
        when(markerRepository.save(any(Marker.class))).thenAnswer(i -> i.getArgument(0));
        when(markerMapper.mapEntityToDto(any())).thenReturn(mock(MarkerResponseDto.class));

        service.createMarker(dto, projectId, user);

        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);
        verify(markerRepository).save(markerCaptor.capture());

        assertEquals(project, markerCaptor.getValue().getProject());
        verify(collaboratorService).checkAccessAndGetProject(eq(user), eq(projectIdStr), anyCollection());
    }

    @Test
    @DisplayName("createMarker() - Forbidden: should throw 403 when access is denied")
    void createMarker_Forbidden() {
        String projectIdStr = projectId.toString();
        MarkerCreateDto dto = new MarkerCreateDto("Urgent", "#FF0000", projectIdStr);

        when(collaboratorService.checkAccessAndGetProject(eq(user), eq(projectIdStr), anyCollection()))
                .thenThrow(new RestApiException(HttpStatus.FORBIDDEN, "No permission"));

        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.createMarker(dto, projectId, user));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(markerRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMarkersByProjectId() should return list of markers")
    void getMarkersByProjectId_shouldReturnMarkers() {

        Marker marker = mock(Marker.class);
        MarkerResponseDto dto = mock(MarkerResponseDto.class);

        when(markerRepository.findAllByProjectId(projectId))
                .thenReturn(List.of(marker));

        when(markerMapper.mapEntityToDto(marker))
                .thenReturn(dto);

        List<MarkerResponseDto> result =
                service.getMarkersByProjectId(projectId);

        assertEquals(1, result.size());

        verify(markerRepository).findAllByProjectId(projectId);
        verify(markerMapper).mapEntityToDto(marker);
    }

    @Test
    @DisplayName("getMarkersByProjectId() should return empty list when no markers exist")
    void getMarkersByProjectId_shouldReturnEmptyList() {

        when(markerRepository.findAllByProjectId(projectId))
                .thenReturn(Collections.emptyList());

        List<MarkerResponseDto> result =
                service.getMarkersByProjectId(projectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(markerRepository).findAllByProjectId(projectId);
        verifyNoInteractions(markerMapper);
    }

    @Test
    @DisplayName("getProjectLogs() should return list of project logs")
    void getProjectLogs_shouldReturnLogs() {

        UUID projectId = UUID.randomUUID();

        AuditLogEntity log = mock(AuditLogEntity.class);

        when(log.getEntity()).thenReturn("Task");
        when(log.getEntityName()).thenReturn("Test Task");
        when(log.getAction()).thenReturn("CREATE");
        when(log.getUserEmail()).thenReturn("test@mail.com");
        when(log.getUserFirstName()).thenReturn("John");
        when(log.getUserLastName()).thenReturn("Doe");
        when(log.getUserAvatar()).thenReturn("avatar.png");
        when(log.getDifference()).thenReturn("status changed");
        when(log.getCreatedAt()).thenReturn(Instant.now());

        when(auditLogRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId.toString()))
                .thenReturn(List.of(log));

        List<ProjectLogDto> result = service.getProjectLogs(projectId);

        assertEquals(1, result.size());
        assertEquals("Task", result.get(0).getEntity());
        assertEquals("Test Task", result.get(0).getEntityName());
        assertEquals("CREATE", result.get(0).getAction());
        assertEquals("test@mail.com", result.get(0).getUserEmail());
        assertEquals("John", result.get(0).getUserFirstName());
        assertEquals("Doe", result.get(0).getUserLastName());
        assertEquals("avatar.png", result.get(0).getUserAvatar());
        assertEquals("status changed", result.get(0).getDifference());
        assertNotNull(result.get(0).getCreatedAt());

        verify(auditLogRepository)
                .findAllByProjectIdOrderByCreatedAtDesc(projectId.toString());
    }}