package de.upteams.tasktracker.task.utils;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.utils.CollaboratorMapper;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.dto.EmployeeDto;
import de.upteams.tasktracker.user.util.AppUserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AppUserMapper.class, CollaboratorMapper.class}
)
public interface TaskMappingService {

//     @Mapping(target = "id", ignore = true)
    @Mapping(target = "project.owner", ignore = true)
    TaskResponseDto mapEntityToDto(Task entity);

    default EmployeeDto mapCollaboratorToEmployeeDto(Collaborator collaborator) {
        if (collaborator == null || collaborator.getAppUser() == null) return null;

        AppUserMapper mapper = org.mapstruct.factory.Mappers.getMapper(AppUserMapper.class);
        return mapper.mapEntityToEmployeeDto(collaborator.getAppUser());
    }
        }
//    @Mapping(target = "project", ignore = true)
//    @Mapping(target = "executors", ignore = true)
//    Task mapDtoToEntity(TaskDto dto);


