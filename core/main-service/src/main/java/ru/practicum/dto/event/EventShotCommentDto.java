package ru.practicum.dto.event;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.category.CategoryDto;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShotCommentDto {
    Long id;
    String title;
    String annotation;
    CategoryDto category;
    LocalDateTime eventDate;
}
