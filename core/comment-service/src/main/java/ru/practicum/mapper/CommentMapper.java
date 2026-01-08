package ru.practicum.mapper;

import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.aop.ClientErrorHandler;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentWithEventDto;
import ru.practicum.dto.comment.CommentWithUserDto;
import ru.practicum.entity.Comment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserClient.class, EventClient.class})
public abstract class CommentMapper {
    @Autowired
    protected UserClient userClient;
    @Autowired
    protected EventClient eventClient;

    @Mapping(target = "event", source = "event")
    @Mapping(target = "author", source = "author")
    public abstract CommentDto toDto(Comment comment);

    @AnnotateWith(ClientErrorHandler.class)
    @Mapping(target = "event", source = "event")
    @Mapping(target = "author", expression = "java(this.userClient.getUserShortDroById(comment.getAuthor()))")
    public abstract CommentWithUserDto toWithUserDto(Comment comment);

    @AnnotateWith(ClientErrorHandler.class)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "event", expression = "java(this.eventClient.getEventShotCommentDtoById(comment.getEvent()))")
    public abstract CommentWithEventDto toWithEventDto(Comment comment);
}