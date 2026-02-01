package de.upteams.tasktracker.marker.utils;

import de.upteams.tasktracker.marker.dto.request.MarkerCreateDto;
import de.upteams.tasktracker.marker.dto.response.MarkerResponseDto;
import de.upteams.tasktracker.marker.entity.Marker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MarkerMapper {

    @Mapping(target = "id", expression = "java(entity.getId() != null ? entity.getId().toString() : null)")
    @Mapping(target = "projectId", expression = "java(entity.getProject() != null ? entity.getProject().getId().toString() : null)")
    MarkerResponseDto mapEntityToDto(Marker entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Marker mapDtoToEntity(MarkerCreateDto dto);
}
