package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.HitDto;
import ru.practicum.entity.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HitMapper {
    @Mapping(target = "timestamp", expression = "java(this.createdAtToTimestamp(hit.getCreatedAt()))")
    HitDto toDto(Hit hit);

    @Mapping(target = "createdAt", expression = "java(this.timestampToLocalDateTime(dto.getTimestamp()))")
    Hit toEntity(HitDto dto);

    default LocalDateTime timestampToLocalDateTime(String timestamp) {
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    default String createdAtToTimestamp(LocalDateTime createdAt) {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
