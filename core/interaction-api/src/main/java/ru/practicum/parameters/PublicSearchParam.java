package ru.practicum.parameters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.dto.event.SortSearchParam;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class PublicSearchParam {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private SortSearchParam sort;
    private Integer from;
    private Integer size;

    public Pageable getPageable() {
        int page = from / size;
        if (sort == SortSearchParam.EVENT_DATE) {
            return PageRequest.of(page, size, Sort.by("eventDate"));
        } else {
            return PageRequest.of(page, size);
        }
    }
}
