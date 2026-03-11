package de.upteams.tasktracker.task.utils;

import de.upteams.tasktracker.collaborator.utils.CollaboratorMapper;
import de.upteams.tasktracker.comment.utils.CommentMapper;
import de.upteams.tasktracker.marker.utils.MarkerMapper;
import de.upteams.tasktracker.task.dto.request.ChecklistItemDto;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Attachment;
import de.upteams.tasktracker.task.entity.ChecklistItem;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.util.AppUserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AppUserMapper.class, CollaboratorMapper.class, MarkerMapper.class, CommentMapper.class}
)
public interface TaskMappingService {


    @Mapping(target = "projectId", source = "entity.project.id")
    @Mapping(target = "statusId", source = "entity.status.id")
    @Mapping(target = "dueDate", source = "dueDate")
    TaskResponseDto mapEntityToDto(Task entity);

    @Mapping(target = "id", expression = "java(item.getId() != null ? item.getId().toString() : null)")
    ChecklistItemDto mapChecklistItemToDto(ChecklistItem item);

    @Mapping(target = "id", expression = "java(attachment.getId() != null ? attachment.getId().toString() : null)")
    AttachmentResponseDto mapAttachmentToDto(Attachment attachment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ChecklistItem mapDtoToChecklistItem(ChecklistItemDto dto);



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "executors", ignore = true)
    @Mapping(target = "markers", ignore = true)
    @Mapping(target = "checklist", ignore = true)
    @Mapping(target = "dueDate", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Task mapDtoToEntity(TaskCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateChecklistEntityFromDto(ChecklistItemDto dto, @MappingTarget ChecklistItem entity);
}



