package ru.practicum.dto.event;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotNull
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    Long category;
    @NotNull
    @NotBlank
    @Size(min = 20, max = 7000)
    String description;
    @Future
    LocalDateTime eventDate;
    Location location;
    Boolean paid = false;
    @PositiveOrZero
    Integer participantLimit = 0;
    Boolean requestModeration = true;
    @Size(min = 3, max = 120)
    String title;
}
