package ru.practicum.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.practicum.dto.event.EventState;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@SuperBuilder
@Getter
public class EventAdminSearchParam extends PageableParam {
    private List<Long> users;
    private List<EventState> states;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private List<Long> categories;
}