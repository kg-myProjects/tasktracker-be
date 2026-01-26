package de.upteams.tasktracker.collaborator.utils;

import de.upteams.tasktracker.collaborator.dto.response.CollaboratorShortResponseDto;
import de.upteams.tasktracker.collaborator.entity.Collaborator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CollaboratorMapper {

    @Mapping(target = "userId", expression = "java(entity.getAppUser().getId().toString())")
    @Mapping(target = "email", source = "appUser.email")
    @Mapping(target = "roles", source = "projectRolesSet")
    CollaboratorShortResponseDto mapEntityToShortDto(Collaborator entity);
}
