package de.upteams.tasktracker.taskstatus.utils;

import de.upteams.tasktracker.task.utils.TaskMappingService;
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
    @Mapping(target = "project.projectTeam", ignore = true)
    @Mapping(target = "project.markers", ignore = true)
    TaskStatusResponseDto mapEntityToStatusDto(TaskStatus status);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateEntityFromDto(TaskStatusUpdateDto dto, @MappingTarget TaskStatus entity);
}
