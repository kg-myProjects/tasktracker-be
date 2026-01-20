package de.upteams.tasktracker.taskstatus.utils;

import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.user.util.AppUserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AppUserMapper.class, TaskMappingService.class}
)
public interface TaskStatusMappingService {
    TaskStatusResponseDto mapEntityToStatusDto(TaskStatus status);

}
