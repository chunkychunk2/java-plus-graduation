package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.aop.ClientErrorHandler;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.*;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final UserClient userClient;
    private final CategoryMapper categoryMapper;

    @ClientErrorHandler
    public EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(userClient.getUserShortDroById(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();
    }

    @ClientErrorHandler
    public EventFullDto toFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(userClient.getUserShortDroById(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .location(Location.builder()
                        .lat(event.getLat())
                        .lon(event.getLon())
                        .build())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .state(event.getState())
                .requestModeration(event.getRequestModeration())
                .build();
    }

    public Event toEntity(NewEventDto dto, Long userId) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(Category.builder().id(dto.getCategory()).build())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .lat(dto.getLocation().getLat())
                .lon(dto.getLocation().getLon())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .title(dto.getTitle())
                .initiator(userId)
                .state(EventState.PENDING)
                .build();
    }

    public EventShotCommentDto toEventShotCommentDto(Event event) {
        return EventShotCommentDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .build();
    }

}
