package ru.practicum.dto.comment;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    Long author;
    Long event;
    String content;
    Boolean isAuthorParticipant;
    LocalDateTime created;
    LocalDateTime updated;
}
