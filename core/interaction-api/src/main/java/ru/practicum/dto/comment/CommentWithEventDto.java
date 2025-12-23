package ru.practicum.dto.comment;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.event.EventShotCommentDto;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentWithEventDto {
    Long id;
    Long author;
    String content;
    EventShotCommentDto event;
    LocalDateTime created;
    LocalDateTime updated;
}
