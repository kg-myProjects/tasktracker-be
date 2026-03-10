package de.upteams.tasktracker.comment.utils;

import de.upteams.tasktracker.comment.dto.request.CommentRequestDto;
import de.upteams.tasktracker.comment.dto.response.CommentResponseDto;
import de.upteams.tasktracker.comment.entity.Comment;
import de.upteams.tasktracker.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "authorName", source = "author", qualifiedByName = "authorToDisplayName")
    @Mapping(target = "authorAvatarUrl", source = "author.avatarUrl")
    CommentResponseDto mapEntityToDto(Comment comment);

    @Named("authorToDisplayName")
    default String getDisplayName(AppUser author) {
        if (author.getFirstName() != null && !author.getFirstName().isBlank()) {
            String last = (author.getLastName() != null && !author.getLastName().isBlank())
                    ? " " + author.getLastName() : "";
            return author.getFirstName() + last;
        }
        return author.getEmail();
    }
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Comment mapDtoToEntity(CommentRequestDto dto);
}
