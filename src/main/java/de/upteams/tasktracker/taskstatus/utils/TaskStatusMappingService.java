package de.upteams.tasktracker.taskstatus.utils;

import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.user.util.AppUserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusUpdateDto;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AppUserMapper.class, TaskMappingService.class},
nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE
)
public interface TaskStatusMappingService {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true) // якщо у вас є зв'язок зі списком тасок
    TaskStatus mapDtoToEntity(TaskStatusCreateDto dto);

    @Mapping(target = "projectId",  source = "project.id")
    TaskStatusResponseDto mapEntityToStatusDto(TaskStatus status);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateEntityFromDto(TaskStatusUpdateDto dto, @MappingTarget TaskStatus entity);
}
