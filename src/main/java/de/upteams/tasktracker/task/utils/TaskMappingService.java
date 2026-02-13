package de.upteams.tasktracker.task.utils;

import de.upteams.tasktracker.collaborator.utils.CollaboratorMapper;
import de.upteams.tasktracker.marker.utils.MarkerMapper;
import de.upteams.tasktracker.task.dto.request.ChecklistItemDto;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.ChecklistItem;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.util.AppUserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AppUserMapper.class, CollaboratorMapper.class, MarkerMapper.class}
)
public interface TaskMappingService {

    //     @Mapping(target = "id", ignore = true)
    @Mapping(target = "project.owner", ignore = true)
    @Mapping(target = "dueDate", source = "dueDate")
    TaskResponseDto mapEntityToDto(Task entity);

    @Mapping(target = "id", expression = "java(item.getId() != null ? item.getId().toString() : null)")
    ChecklistItemDto mapChecklistItemToDto(ChecklistItem item);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ChecklistItem mapDtoToChecklistItem(ChecklistItemDto dto);


    @Mapping(target = "project", ignore = true)
    @Mapping(target = "executors", ignore = true)
    @Mapping(target = "markers", ignore = true)
    @Mapping(target = "checklist", ignore = true)
    Task mapDtoToEntity(TaskCreateDto dto);
}



