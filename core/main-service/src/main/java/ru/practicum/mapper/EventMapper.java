package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.entity.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    EventShortDto toShortDto(Event event);

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "location.lat", source = "lat")
    @Mapping(target = "location.lon", source = "lon")
    EventFullDto toFullDto(Event event);

    @Mapping(target = "lat", source = "dto.location.lat")
    @Mapping(target = "lon", source = "dto.location.lon")
    @Mapping(target = "initiator.id", source = "userId")
    @Mapping(target = "category.id", source = "dto.category")
    @Mapping(target = "state", expression = "java(ru.practicum.entity.EventState.PENDING)")
    Event toEntity(NewEventDto dto, Long userId);

}
