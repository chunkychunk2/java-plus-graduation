package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentWithEventDto;
import ru.practicum.dto.comment.CommentWithUserDto;
import ru.practicum.entity.Comment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "author", source = "author.id")
    CommentDto toDto(Comment comment);

    @Mapping(target = "event", source = "event.id")
    CommentWithUserDto toWithUserDto(Comment comment);

    @Mapping(target = "author", source = "author.id")
    CommentWithEventDto toWithEventDto(Comment comment);
}