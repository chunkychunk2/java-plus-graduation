package ru.practicum.dto.comment;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentWithUserDto {
    Long id;
    Long event;
    UserShortDto author;
    String content;
    LocalDateTime created;
    LocalDateTime updated;
}
