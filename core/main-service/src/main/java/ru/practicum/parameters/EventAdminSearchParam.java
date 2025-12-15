package ru.practicum.parameters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.entity.EventState;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class EventAdminSearchParam {
    private List<Long> users;
    private List<EventState> states;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private List<Long> categories;
    private Integer from;
    private Integer size;

    public Pageable getPageable() {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
